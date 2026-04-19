package com.urbannexus.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urbannexus.repository.BookingRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public Map<String, Object> bookTechnician(Long residentId, String skill, Integer slot, String assignDate) {
        return bookingRepository.autoBookTechnician(residentId, skill, slot, assignDate);
    }

    @Transactional
    public Map<String, Object> bookAmenity(Long residentId, Long amenityId, String date, Integer slot,
            Integer capacityBooked) {
        return bookingRepository.autoBookAmenity(residentId, amenityId, date, slot, capacityBooked);
    }
}
