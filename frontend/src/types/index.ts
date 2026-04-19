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

export type Role = 'SuperAdmin' | 'Resident' | 'Technician';

export interface User {
  id: number;
  username: string;
  role: Role;
  residentId?: number;
  techId?: number;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isLoading: boolean;
}

export interface LoginResponse {
  message: string;
  token: string;
  admin: {
    username: string;
    role: Role;
  };
}

export interface Resident {
  residentId: number;
  name: string;
  houseBlock: string;
  houseFloor: string;
  houseUnit: string;
  ownershipStatus: string;
  contact: string;
  noOfMembers: number;
}

export interface Technician {
  techId: number;
  name: string;
  skill: string;
  contact: string;
}

export interface Amenity {
  amenityId: number;
  name: string;
  capacity: number;
}

export interface Transaction {
  trans_no: string;
  resident_name: string;
  house_block: string;
  house_unit: string;
  status: string;
  cost: number;
  service_type: string;
}

export interface Booking {
  booking_id?: number;
  assignment_id?: number;
  amenity?: string;
  technician?: string;
  skill?: string;
  date?: string;
  assign_date?: string;
  slot: number;
  status: string;
  house_block?: string;
  house_unit?: string;
  resident_name?: string;
}

export interface DecodedToken {
  id?: number;
  userId?: number;
  adminId?: number;
  sub?: string;
  username?: string;
  role: Role;
  residentId?: number;
  techId?: number;
  iat?: number;
  exp?: number;
}
