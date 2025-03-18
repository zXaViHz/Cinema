package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Service.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/sendOtp")
    @ResponseBody
    public String sendOtp(@RequestParam String email) {
        try {
            userService.sendOtp(email);
            return "OTP has been sent to your email.";
        } catch (RuntimeException e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String otp,
                           Model model) {
        try {
            if (!userService.verifyOtp(email, otp)) {
                model.addAttribute("error", "Invalid OTP. Please try again.");
                return "register";
            }
            userService.registerUser(username, password, email, phone, otp);
            model.addAttribute("message", "Registration successful! Please log in.");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            userService.loginUser(username, password);
            model.addAttribute("message", "Login successful!");
            return "home";  // Chuyển đến trang chính sau khi login thành công
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }
}
