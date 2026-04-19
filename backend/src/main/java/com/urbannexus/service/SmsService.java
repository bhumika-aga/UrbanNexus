package com.urbannexus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

@Service
public class SmsService {

    @Value("${app.twilio.sid}")
    private String twilioSid;

    @Value("${app.twilio.token}")
    private String twilioToken;

    @Value("${app.twilio.phone}")
    private String twilioPhone;

    @PostConstruct
    public void init() {
        Twilio.init(twilioSid, twilioToken);
    }

    public void sendConfirmationMessage(String to, String msg) {
        if (to == null || to.isEmpty()) {
            System.err.println("[SMS Error] No phone number found.");
            return;
        }

        try {
            String cleanNumber = to.replaceAll("\\s+", "");
            String formattedNumber = cleanNumber.startsWith("+") ? cleanNumber : "+91" + cleanNumber;

            System.out.println("[Attempting SMS] Sending to: " + formattedNumber);

            Message message = Message.creator(
                    new PhoneNumber(formattedNumber),
                    new PhoneNumber(twilioPhone),
                    "UrbanNexus: " + msg).create();

            System.out.println("[SMS Sent] to " + formattedNumber + " SID: " + message.getSid());
        } catch (Exception ex) {
            System.err.println("[Twilio Error] " + ex.getMessage());
        }
    }
}
