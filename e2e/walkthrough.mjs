/*
 * Copyright (c) 2026 Bhumika Agarwal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// Browser walkthrough of the whole app as admin, resident and technician, in headless Chrome.
//
// Needs a FRESHLY SEEDED backend (the local H2 profile reseeds on every start: it relies on the
// data.sql accounts still having their seed passwords) and the frontend at APP_URL. It changes
// data (passwords, bookings, a deleted resident), so restart the backend before running it again.
//
//   APP_URL          frontend address           (default http://localhost:5173)
//   SCREENSHOT_DIR   where screenshots go        (default ./screenshots)
//   BROWSER_CHANNEL  installed browser to drive  (default "chrome"; e.g. "msedge")
//
// Exits non-zero if any check fails, any API call returns 5xx, or the page throws.
import { chromium } from "playwright-core";
import fs from "node:fs";
import path from "node:path";

const APP = process.env.APP_URL || "http://localhost:5173";
const OUT =
  path.resolve(process.env.SCREENSHOT_DIR || "screenshots") + path.sep;
fs.rmSync(OUT, { recursive: true, force: true });
fs.mkdirSync(OUT, { recursive: true });

const results = [];
const consoleErrors = [];
const failedCalls = [];
const dialogs = [];
let shotNo = 0;

const browser = await chromium.launch({
  channel: process.env.BROWSER_CHANNEL || "chrome",
  headless: true,
});
const context = await browser.newContext({
  viewport: { width: 1440, height: 900 },
});
const page = await context.newPage();
page.setDefaultTimeout(15000);

page.on("console", (m) => m.type() === "error" && consoleErrors.push(m.text()));
page.on("pageerror", (e) => consoleErrors.push("pageerror: " + e.message));
page.on("response", (r) => {
  if (r.url().includes("/api/") && r.status() >= 500)
    failedCalls.push(`${r.status()} ${r.request().method()} ${r.url()}`);
});
// The app uses window.confirm/alert; accept them all and keep the text for the report.
page.on("dialog", async (d) => {
  dialogs.push(
    `${d.type()}: ${d.message().replace(/\s+/g, " ").slice(0, 140)}`,
  );
  await d.accept();
});

const shot = async (name) => {
  const file = `${String(++shotNo).padStart(2, "0")}-${name}.png`;
  await page.screenshot({ path: OUT + file, fullPage: true });
  return file;
};

async function step(area, name, fn) {
  try {
    const note = await fn();
    results.push({ area, name, ok: true, note: note || "" });
  } catch (e) {
    const file = await shot(
      "FAIL-" + name.replace(/[^a-z0-9]+/gi, "-").slice(0, 40),
    ).catch(() => "");
    results.push({
      area,
      name,
      ok: false,
      note: e.message.split("\n")[0].slice(0, 200) + (file ? ` [${file}]` : ""),
    });
  }
}

const lastDialog = () => dialogs[dialogs.length - 1] || "";
const future = (days) => {
  const d = new Date(Date.now() + days * 86400000);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};

async function login(username, password) {
  await page.goto(APP + "/login");
  await page.getByLabel("Username").fill(username);
  await page.getByPlaceholder("••••••••").fill(password);
  await page.getByRole("button", { name: "Sign In" }).click();
}

async function forcedChange(current, next) {
  await page.getByText("Set your own password").waitFor();
  await page.getByLabel("Current Password").fill(current);
  await page.getByLabel("New Password", { exact: true }).fill(next);
  await page.getByLabel("Confirm New Password").fill(next);
  await page.getByRole("button", { name: "Update Password" }).click();
}

async function logout() {
  try {
    await page.getByRole("button", { name: "Logout" }).click({ timeout: 5000 });
    await page.waitForURL("**/login");
  } catch {
    // A failed step may have left the app elsewhere; start the next role from a clean session.
    await page.evaluate(() => localStorage.clear());
    await page.goto(APP + "/login");
  }
}

const tab = (name) => page.getByRole("tab", { name });

// ================================================================ login
await step("Login", "Login page renders", async () => {
  await page.goto(APP + "/login");
  await page.getByText("Sign in to manage your community grid").waitFor();
  return await shot("login");
});
await step("Login", "Wrong password shows an error", async () => {
  await login("admin", "wrong-password");
  await page.getByText("Invalid username or password").waitFor();
});

// ================================================================ admin
await step(
  "Admin",
  "Seed admin is forced to change password, then reaches the hub",
  async () => {
    await login("admin", "Bhumika@1234");
    await shot("forced-password-screen");
    await forcedChange("Bhumika@1234", "Admin-Secret1");
    await page.getByText("Administrator Hub").waitFor();
    await page.getByText("Payments to Review").waitFor();
    const flashes = await page
      .getByText("No residents found in the records.")
      .count();
    await page.getByText("John Doe").waitFor();
    if (flashes)
      throw new Error("directory showed 'No residents found' before loading");
    return await shot("admin-hub");
  },
);
await step(
  "Admin",
  "Resident Directory loads and search finds 'john'",
  async () => {
    await page.getByText("John Doe").waitFor();
    await page.getByPlaceholder("Search residents...").fill("john");
    await page.getByText("Michael Johnson").waitFor();
    await page.waitForTimeout(600);
    const rows = await page.locator("tbody tr").count();
    if (rows !== 2) throw new Error(`expected 2 matches, saw ${rows} rows`);
    await shot("resident-search");
    await page.getByPlaceholder("Search residents...").fill("");
  },
);
let newResidentPassword = "";
await step("Admin", "Add a resident shows one-time credentials", async () => {
  await page.getByRole("button", { name: "Sign New Resident" }).click();
  await page.getByText("Register New Resident").waitFor();
  await page.getByLabel("Full Name").fill("Lando Norris");
  await page.getByLabel("Block").fill("C");
  await page.getByLabel("Floor").fill("4");
  await page.getByLabel("Unit").fill("404");
  await page.getByLabel("Contact Number").fill("9000000404");
  await shot("add-resident-form");
  await page.getByRole("button", { name: "Add to Community" }).click();
  await page.getByText("Account Credentials").waitFor();
  const text = await page.getByRole("dialog").innerText();
  const m = text.match(/Temporary Password\s+([A-Za-z0-9]{12})/i);
  if (!m)
    throw new Error(
      "no 12-character temporary password shown: " + text.slice(0, 200),
    );
  newResidentPassword = m[1];
  await shot("credentials-dialog");
  await page.getByRole("button", { name: "Got it" }).click();
  await page
    .getByText("27", { exact: true })
    .first()
    .waitFor({ timeout: 5000 })
    .catch(() => {
      throw new Error("Total Residents card did not update to 27");
    });
  return "credentials shown once; Total Residents card updated to 27";
});
await step(
  "Admin",
  "Reset password from the directory issues new credentials",
  async () => {
    await page.getByPlaceholder("Search residents...").fill("Lando");
    await page.getByText("Lando Norris").waitFor();
    await page.waitForFunction(
      () => document.querySelectorAll("tbody tr").length === 1,
    );
    await page.getByRole("button", { name: "Reset password" }).first().click();
    if (!/Reset Lando Norris's password/.test(lastDialog()))
      throw new Error("reset prompt: " + lastDialog());
    await page.getByText("Account Credentials").waitFor();
    const text = await page.getByRole("dialog").innerText();
    const m = text.match(/Temporary Password\s+([A-Za-z0-9]{12})/i);
    if (!m || m[1] === newResidentPassword)
      throw new Error("no new temporary password");
    await page.getByRole("button", { name: "Got it" }).click();
    await page.getByPlaceholder("Search residents...").fill("");
  },
);
await step(
  "Admin",
  "Technical Crew: list and recruit a technician",
  async () => {
    await tab("Technical Crew").click();
    await page.getByText("Tom Kristensen").waitFor();
    await page.getByRole("button", { name: "Recruit Crew" }).click();
    await page.getByLabel("Staff ID").fill("88");
    await page.getByLabel("Full Name").fill("Oscar Piastri");
    await page.getByLabel("Contact").fill("9000000088");
    await page.getByRole("button", { name: "Deploy Staff" }).click();
    await page.getByText("Account Credentials").waitFor();
    await page.getByRole("button", { name: "Got it" }).click();
    await page.getByText("Oscar Piastri").waitFor();
    return await shot("technical-crew");
  },
);
await step(
  "Admin",
  "Edit a technician's skill from the Technical Crew tab",
  async () => {
    const row = page.locator("tr", { hasText: "Andre Lotterer" });
    await row.getByRole("button", { name: "Edit technician" }).click();
    await page.getByText("Edit Technician #8").waitFor();
    await page.getByRole("dialog").getByLabel("Primary Skill").click();
    await page.getByRole("option", { name: "HVAC" }).click();
    await shot("edit-technician");
    await page.getByRole("button", { name: "Save Changes" }).click();
    await row.getByText("HVAC").waitFor();
  },
);
await step("Admin", "Financial Ledger lists invoices", async () => {
  await tab("Financial Ledger").click();
  await page.getByText("TXN-101").waitFor();
  return await shot("ledger");
});
await step("Admin", "Run Overdue Scan", async () => {
  await page.getByRole("button", { name: "Run Overdue Scan" }).click();
  await page.waitForTimeout(800);
  if (
    !/Overdue scan complete/.test(lastDialog()) ||
    /cursor/.test(lastDialog())
  )
    throw new Error("alert: " + lastDialog());
});
for (const [name, marker] of [
  ["Open Assignments", "Live Technician Assignments"],
  ["Facility Bookings", "Facility Usage History"],
  ["Service History", "Complete Service Ticket History"],
  ["Audit Logs", "System Audit Trail"],
]) {
  await step("Admin", `${name} tab renders data`, async () => {
    await tab(name).click();
    await page.getByText(marker).waitFor();
    await page.locator("tbody tr").first().waitFor();
    if (await page.getByText(/No .* found/).count())
      throw new Error("table is empty");
    return await shot(name.toLowerCase().replace(/ /g, "-"));
  });
}
await step("Admin", "Audit log shows who made each change", async () => {
  await page.getByRole("columnheader", { name: "BY" }).waitFor();
  await page.locator("tbody tr").first().getByText("admin").waitFor();
});
await logout();

// ================================================================ stale session
await step(
  "Login",
  "Signing in again over a stale temporary-password session works",
  async () => {
    await login("jane_s", "pwd123#");
    await page.getByText("Set your own password").waitFor();
    // Leave without logging out; the temporary-password token stays in localStorage.
    await page.goto(APP + "/login");
    await login("admin", "Admin-Secret1");
    await page.getByText("Administrator Hub").waitFor();
    await logout();
  },
);

// ================================================================ resident
await step(
  "Resident",
  "jane_s forced password change, then Resident Portal",
  async () => {
    await login("jane_s", "pwd123#");
    await forcedChange("pwd123#", "Jane-Secret1");
    await page.getByText("Resident Portal").waitFor();
    return await shot("resident-portal");
  },
);
await step(
  "Resident",
  "My Dues: report a payment -> Under Review",
  async () => {
    await page.getByText("My Dues").click();
    await page.getByText("TXN-101").waitFor();
    await page.getByRole("button", { name: "I've Paid" }).first().click();
    await page.getByLabel("Payment reference").fill("UPI-77001");
    await shot("report-payment-dialog");
    await page.getByRole("button", { name: "Submit for review" }).click();
    await page.waitForTimeout(60);
    const closing = await page
      .locator(".MuiDialog-root")
      .innerText()
      .catch(() => "");
    if (closing && !/₹590 for TXN-101/.test(closing))
      throw new Error("dialog text while closing: " + closing.slice(0, 80));
    await page.getByText("Awaiting admin confirmation").waitFor();
    await shot("dues-under-review");
    await page
      .locator("button:has(svg[data-icon='chevron-left'])")
      .first()
      .click();
  },
);
await step("Resident", "Facilities: reserve the gym", async () => {
  await page.getByText("Facilities").click();
  await page.getByText("Parc Fermé Gym").click();
  await page.getByLabel("Select Date").fill(future(10));
  await page.getByLabel("Members Joining").fill("2");
  await shot("amenity-booker");
  await page.getByRole("button", { name: "Confirm Reservation" }).click();
  await page.getByText("Resident Portal").waitFor();
  if (!/confirmed/i.test(lastDialog()))
    throw new Error("unexpected alert: " + lastDialog());
});
await step(
  "Resident",
  "Staff Support: skills and prices come from the API; book a plumber",
  async () => {
    await page.getByText("Staff Support").click();
    await page.getByText("₹590").waitFor();
    await shot("tech-booker");
    await page.getByText("Plumber", { exact: true }).click();
    await page.getByLabel("Preferred Date").fill(future(12));
    await page.getByRole("button", { name: "Deploy Support Ticket" }).click();
    await page.getByText("Resident Portal").waitFor();
    if (!/ticket created/i.test(lastDialog()))
      throw new Error("unexpected alert: " + lastDialog());
  },
);
await step(
  "Resident",
  "My Activity shows the new reservation and ticket",
  async () => {
    await page.getByText("My Activity").click();
    await page.getByText("Facility Reservations").waitFor();
    await page.getByText("Parc Fermé Gym").first().waitFor();
    await page
      .getByText(/Plumber/)
      .first()
      .waitFor();
    return await shot("resident-history");
  },
);
await step(
  "Resident",
  "Profile: change family members and it persists",
  async () => {
    await page.getByRole("button", { name: "Profile" }).click();
    await page.getByRole("button", { name: "Edit Profile" }).click();
    await page.getByLabel("Family Members").fill("4");
    await page.getByRole("button", { name: "Save Changes" }).click();
    await page.getByText("Profile updated successfully!").waitFor();
    await page.reload();
    await page.getByRole("button", { name: "Profile" }).click();
    const v = await page.getByLabel("Family Members").inputValue();
    if (v !== "4") throw new Error("members after reload = " + v);
    return await shot("resident-profile");
  },
);
await logout();

// ================================================================ technician
await step(
  "Technician",
  "tech_tom forced password change, then Field Ops",
  async () => {
    await login("tech_tom", "pwd123#");
    await forcedChange("pwd123#", "Tom-Secret1");
    await page.getByText("Field Ops Control").waitFor();
    await page.getByText("Today", { exact: true }).waitFor();
    await page.getByText("Past Due", { exact: true }).waitFor();
    await page
      .getByText(/past due$/)
      .first()
      .waitFor();
    await page.getByText("09:00 - 12:00").first().waitFor();
    return await shot("technician-dashboard");
  },
);
await step("Technician", "Resolve a task", async () => {
  const before = await page
    .getByRole("button", { name: "Resolve Task" })
    .count();
  if (!before) throw new Error("no open tasks shown");
  await page.getByRole("button", { name: "Resolve Task" }).first().click();
  await page.waitForTimeout(800);
  const after = await page
    .getByRole("button", { name: "Resolve Task" })
    .count();
  if (after !== before - 1) throw new Error(`open tasks ${before} -> ${after}`);
  return `${before} -> ${after} open`;
});
await step("Technician", "Toggle availability", async () => {
  await page
    .getByRole("switch")
    .or(page.locator("input[type=checkbox]"))
    .first()
    .click();
  await page.getByText("System Paused").waitFor();
});
await step(
  "Technician",
  "Profile: skill is read-only with an explanation",
  async () => {
    await page.getByRole("button", { name: "Profile" }).click();
    await page.getByRole("button", { name: "Edit Profile" }).click();
    if (!(await page.getByLabel("Specialization / Skill").isDisabled()))
      throw new Error("skill is editable");
    await page
      .getByText("Only the administrator can change your skill.")
      .waitFor();
    await shot("technician-profile");
    await page.getByRole("button", { name: "Cancel" }).click();
  },
);
await logout();

// ================================================================ admin reviews and deletion
await step("Admin", "Ledger: confirm the reported payment", async () => {
  await login("admin", "Admin-Secret1");
  await page.getByText("Administrator Hub").waitFor();
  await tab("Financial Ledger").click();
  const row = page.locator("tr", { hasText: "TXN-101" });
  await row.getByText("Ref: UPI-77001").waitFor();
  await shot("ledger-under-review");
  const before = await page
    .getByText("Payments to Review")
    .locator("..")
    .innerText();
  await row.getByRole("button", { name: "Confirm" }).click();
  await row.getByText(/^on /).waitFor();
  await page.waitForTimeout(500);
  const after = await page
    .getByText("Payments to Review")
    .locator("..")
    .innerText();
  if (before === after)
    throw new Error(`review card unchanged: ${before} -> ${after}`);
  return await shot("ledger-paid");
});
await step("Admin", "Ledger: record an offline payment", async () => {
  const row = page.locator("tr", { hasText: "TXN-104" });
  await row.getByRole("button", { name: "Record Payment" }).click();
  await row.getByText("Paid", { exact: true }).waitFor();
});
await step(
  "Admin",
  "Delete a resident who owes money -> write-off prompt",
  async () => {
    await tab("Resident Directory").click();
    await page.getByPlaceholder("Search residents...").fill("Jane Smith");
    await page.getByText("Jane Smith").waitFor();
    await page.waitForTimeout(500);
    await page.getByRole("button", { name: "Delete resident" }).first().click();
    await page.getByText("No residents found in the records.").waitFor();
    const prompts = dialogs.slice(-2).join(" | ");
    if (!/still owes/.test(prompts))
      throw new Error("no write-off prompt: " + prompts);
    return prompts.slice(0, 160);
  },
);
await step(
  "Admin",
  "Ledger shows the former resident with Written Off invoices",
  async () => {
    await tab("Financial Ledger").click();
    await page.getByPlaceholder("Search by resident name...").fill("jane");
    await page.getByText("Former resident").first().waitFor();
    await page.getByText("Written Off").first().waitFor();
    return await shot("ledger-written-off");
  },
);

await browser.close();

// ================================================================ report
for (const r of results)
  console.log(
    `${r.ok ? "PASS" : "FAIL"}  ${r.area.padEnd(10)}  ${r.name}${r.note ? "   (" + r.note + ")" : ""}`,
  );
console.log(
  `\n${results.filter((r) => r.ok).length}/${results.length} UI checks passed`,
);
console.log(`\nAPI 5xx responses seen by the browser: ${failedCalls.length}`);
failedCalls.forEach((c) => console.log("  " + c));
console.log(`Browser console errors: ${consoleErrors.length}`);
[...new Set(consoleErrors)]
  .slice(0, 15)
  .forEach((e) => console.log("  " + e.slice(0, 220)));
console.log(`\nDialogs shown (${dialogs.length}):`);
dialogs.forEach((d) => console.log("  " + d));
console.log(`\nScreenshots: ${OUT}`);

// Expected browser errors: the deliberate wrong-password login (401) and refusing to delete a resident
// who still owes money (409, which triggers the write-off prompt). Anything else is a failure.
const unexpectedErrors = consoleErrors.filter(
  (e) => !/status of (401|409)/.test(e),
);
const failed = results.filter((r) => !r.ok).length;
if (failed || failedCalls.length || unexpectedErrors.length) {
  console.error(
    `\nFAILED: ${failed} check(s), ${failedCalls.length} server error(s), ${unexpectedErrors.length} unexpected browser error(s)`,
  );
  process.exit(1);
}
