package com.employeerating.serviceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.employeerating.dto.VerifyOtpRequest;
import com.employeerating.repository.EmployeeRepo;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmployeeRepo employeeRepo;

    private final Map<String, OtpData> otpStore = new HashMap<>();

    private static final long OTP_VALIDITY_DURATION = 5 * 60 * 1000;

    public void generateAndSendOtp(String employeeId, String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        otpStore.put(employeeId, new OtpData(otp, System.currentTimeMillis() + OTP_VALIDITY_DURATION));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for Password Reset");
        message.setText("Your OTP is: " + otp + "\n\nThis OTP is valid for 5 minutes.");
        mailSender.send(message);
    }

    public boolean verifyOtp(VerifyOtpRequest request) {
        employeeRepo.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        OtpData otpData = otpStore.get(request.getEmployeeId());

        if (otpData == null) {
            throw new RuntimeException("OTP not found or expired");
        }

        if (System.currentTimeMillis() > otpData.getExpiryTime()) {
            otpStore.remove(request.getEmployeeId());
            throw new RuntimeException("OTP has expired");
        }

        boolean isValid = otpData.getOtp().equals(request.getOtp());
        if (isValid) {
            otpStore.remove(request.getEmployeeId()); // clear after success
        }
        return isValid;
    }

    public void clearOtp(String employeeId) {
        otpStore.remove(employeeId);
    }

    private static class OtpData {
        private final String otp;
        private final long expiryTime;

        public OtpData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }
}
 