package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import com.example.demo.Model.User;
import com.example.demo.Reponsitory.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    private final Map<String, String> otpStorage = new HashMap<>();

    public void sendOtp(String email) {
        String otp = generateOtp();
        otpStorage.put(email, otp);
        emailSenderService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);
    }

    public boolean verifyOtp(String email, String otp) {
        return otpStorage.containsKey(email) && otpStorage.get(email).equals(otp);
    }

    @Transactional
    public boolean registerUser(String username, String password, String email, String phone, String otp) {
        if (!verifyOtp(email, otp)) {
            throw new RuntimeException("Invalid OTP. Please try again.");
        }

        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setPhone(phone);

        userRepository.save(user);
        otpStorage.remove(email);
        return true;
    }

    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        throw new RuntimeException("Invalid credentials");
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}