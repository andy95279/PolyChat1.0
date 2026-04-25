package org.example.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Twilio implementation of SmsService.
 * Sends real SMS messages using the Twilio API.
 * 
 * Required environment variables:
 *   TWILIO_ACCOUNT_SID  - Your Twilio Account SID
 *   TWILIO_AUTH_TOKEN   - Your Twilio Auth Token
 *   TWILIO_PHONE_NUMBER - Your Twilio phone number (e.g. +1234567890)
 */
public class TwilioSmsService implements SmsService {

    private final String fromNumber;

    public TwilioSmsService() {
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken = System.getenv("TWILIO_AUTH_TOKEN");
        this.fromNumber = System.getenv("TWILIO_PHONE_NUMBER");

        if (accountSid == null || authToken == null || fromNumber == null) {
            throw new IllegalStateException(
                "Twilio credentials not configured. Set TWILIO_ACCOUNT_SID, " +
                "TWILIO_AUTH_TOKEN, and TWILIO_PHONE_NUMBER environment variables."
            );
        }

        Twilio.init(accountSid, authToken);
        System.out.println("✅ Twilio SMS Service initialized successfully.");
    }

    @Override
    public String sendSms(String to, String message) {
        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            System.out.println("SMS sent! SID: " + twilioMessage.getSid() +
                             " | Status: " + twilioMessage.getStatus());
            return twilioMessage.getSid();
        } catch (Exception e) {
            System.err.println("❌ Failed to send SMS via Twilio: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
