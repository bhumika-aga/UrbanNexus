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

package com.urbannexus.repository;

import com.urbannexus.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {
    
    // Case-insensitive: the caller passes namePattern already lower-cased with wildcards ("%john%")
    // and block upper-cased. Functions are applied to the columns only, never to a parameter: a null
    // parameter has no type on PostgreSQL, so e.g. UPPER(:block) fails with "upper(bytea) does not exist".
    @Query("SELECT r FROM Resident r WHERE " +
               "(:namePattern IS NULL OR LOWER(r.name) LIKE :namePattern) AND " +
               "(:block IS NULL OR UPPER(r.houseBlock) = :block) AND " +
               "(:floor IS NULL OR r.houseFloor = :floor) AND " +
               "(:unit IS NULL OR r.houseUnit = :unit)")
    List<Resident> searchResidents(@Param("namePattern") String namePattern,
                                   @Param("block") String block,
                                   @Param("floor") String floor,
                                   @Param("unit") String unit);
}
