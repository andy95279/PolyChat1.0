package org.example.provider;

import org.example.db.UserDao;
import org.example.db.VerificationDao;
import org.example.model.User;
import org.example.service.SmsService;
import org.example.service.TwilioSmsService;
import org.example.service.TextbeltSmsService;
import java.util.Random;

public class AuthProvider {
    private static AuthProvider instance;
    private User currentUser;
    private String pendingPhone;
    private String pendingName;
    private String pendingSurnames;
    private String pendingEmail;
    private int pendingAge;
    private String pendingLanguage;
    private final SmsService smsService;

    private AuthProvider() {
        this.smsService = createSmsService();
    }

    /**
     * Auto-detects the SMS provider: uses Twilio if credentials are set,
     * otherwise uses Textbelt (free API, sends real SMS).
     */
    private SmsService createSmsService() {
        String sid = System.getenv("TWILIO_ACCOUNT_SID");
        String token = System.getenv("TWILIO_AUTH_TOKEN");
        String from = System.getenv("TWILIO_PHONE_NUMBER");

        if (sid != null && token != null && from != null) {
            try {
                System.out.println("🔑 Twilio credentials detected. Using TwilioSmsService.");
                return new TwilioSmsService();
            } catch (Exception e) {
                System.err.println("⚠ Failed to init Twilio, falling back to Textbelt: " + e.getMessage());
            }
        } else {
            System.out.println("📱 Using TextbeltSmsService (free API - sends real SMS).");
        }
        return new TextbeltSmsService();
    }

    public static AuthProvider getInstance() {
        if (instance == null)
            instance = new AuthProvider();
        return instance;
    }

    public boolean requestCode(String phone, String name, String surnames, String email, int age, String language) {
        if (phone == null || phone.trim().isEmpty())
            return false;
        
        this.pendingPhone = phone;
        this.pendingName = name;
        this.pendingSurnames = surnames;
        this.pendingEmail = email;
        this.pendingAge = age;
        this.pendingLanguage = language;
        
        // 1. Generate random 6-digit code
        String code = String.format("%06d", new Random().nextInt(1000000));
        System.out.println("DEBUG: Generated code for " + phone + " is " + code);
        
        // 2. Format the SMS message
        String message = "Tu código de verificación de PolyChat es: " + code + ". Expira en 5 minutos.";

        // 3. Save it to DB in public schema (including the message text)
        VerificationDao.saveCode(phone, code, message);
        
        // 4. Send SMS with the verification code
        String smsId = smsService.sendSms(phone, message);
        if (smsId != null) {
            // 5. Log the SMS in our tracking table (including the code)
            VerificationDao.saveSmsLog(smsId, phone, code, message);
        } else {
            System.err.println("⚠ SMS could not be sent to " + phone + ". Check Textbelt quota or connection.");
            System.err.println("👉 DEVELOPMENT TIP: Use the verification code printed above to proceed.");
        }
        
        // We return true anyway so the user can reach the VerifyScreen and use the code from console/logs
        return true;
    }

    public boolean verifyCode(String code) {
        // 1. Query the 'verificacion' table to retrieve the latest code sent to this phone
        String latestCodeInDb = VerificationDao.getLatestCode(pendingPhone);
        System.out.println("🔍 [AuthProvider] Code entered by user: " + code);
        System.out.println("🔍 [AuthProvider] Latest code in 'verificacion' table for " + pendingPhone + ": " + latestCodeInDb);

        // 2. Check against DB (codigos_verificacion first, then verificacion as fallback)
        if (VerificationDao.checkCode(pendingPhone, code)) {
            if (pendingName != null && !pendingName.trim().isEmpty()) {
                // REGISTRATION: create new user
                String userId = java.util.UUID.randomUUID().toString();
                String email = pendingEmail != null ? pendingEmail : (pendingName.toLowerCase() + "@polychat.app");
                String language = pendingLanguage != null ? pendingLanguage : "Español";
                int age = pendingAge != 0 ? pendingAge : 25;
                String surnames = pendingSurnames != null ? pendingSurnames : "";

                User newUser = new User(userId, pendingName, surnames, email, pendingPhone, language, age);
                UserDao.saveUser(newUser);
                this.currentUser = newUser;
                System.out.println("✅ New user registered: " + userId);
            } else {
                // LOGIN: fetch existing user from DB by phone
                User existingUser = UserDao.getUserByPhone(pendingPhone);
                if (existingUser != null) {
                    this.currentUser = existingUser;
                    System.out.println("✅ User logged in: " + existingUser.getId());
                } else {
                    // AUTO-REGISTER on login if user does not exist
                    System.out.println("ℹ User not found on login. Auto-registering...");
                    String userId = java.util.UUID.randomUUID().toString();
                    User newUser = new User(userId, "Usuario", "", "usuario@polychat.app", pendingPhone, "Español", 18);
                    UserDao.saveUser(newUser);
                    this.currentUser = newUser;
                    System.out.println("✅ New user auto-registered: " + userId);
                }
            }
            return true;
        }

        System.err.println("❌ [AuthProvider] Verification failed. Code entered: " + code
                + " | Expected (from DB): " + latestCodeInDb);
        return false;
    }

    public boolean resendCode() {
        if (pendingPhone != null) {
            return requestCode(pendingPhone, pendingName, pendingSurnames, pendingEmail, pendingAge, pendingLanguage);
        }
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getPendingPhone() {
        return pendingPhone;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void updateUser(String name, String email, String phone, String language, int age) {
        if (currentUser != null) {
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            currentUser.setLanguage(language);
            currentUser.setAge(age);
            // Persist to database
            UserDao.updateUser(currentUser);
        }
    }

    public void logout() {
        currentUser = null;
        pendingPhone = null;
        pendingName = null;
        // Reset ChatProvider so the next login loads the new user's chats from scratch
        ChatProvider.reset();
    }
}
