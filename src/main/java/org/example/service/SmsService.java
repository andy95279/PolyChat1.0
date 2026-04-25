package org.example.service;

public interface SmsService {
    /**
     * Sends an SMS message to the specified phone number.
     * @param to The recipient's phone number.
     * @param message The message content.
     * @return The SMS ID if the message was sent successfully, null otherwise.
     */
    String sendSms(String to, String message);
}
