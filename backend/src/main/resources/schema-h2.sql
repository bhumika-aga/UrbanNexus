-- UrbanNexus H2 Database Logic (Java Alias Bridge)
-- This file is executed only when the 'h2' platform is active.

CREATE ALIAS IF NOT EXISTS AutoBookTechnician FOR "com.urbannexus.util.H2DatabaseFunctions.autoBookTechnician";
CREATE ALIAS IF NOT EXISTS AutoBookAmenity FOR "com.urbannexus.util.H2DatabaseFunctions.autoBookAmenity";
CREATE ALIAS IF NOT EXISTS GetResidentPendingDues FOR "com.urbannexus.util.H2DatabaseFunctions.getResidentPendingDues";
CREATE ALIAS IF NOT EXISTS ProcessOverduePayments FOR "com.urbannexus.util.H2DatabaseFunctions.processOverduePayments";
