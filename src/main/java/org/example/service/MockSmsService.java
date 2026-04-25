package org.example.service;

/**
 * Mock implementation of SmsService for development and testing.
 * Instead of sending a real SMS, it prints the verification code to the console.
 * This is used when Twilio credentials are not configured.
 */
public class MockSmsService implements SmsService {

    @Override
    public String sendSms(String to, String message) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  📱 MOCK SMS SERVICE");
        System.out.println("  To:      " + to);
        System.out.println("  Message: " + message);
        System.out.println("═══════════════════════════════════════════");
        
        // Simulate network delay (500ms)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "MOCK-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
