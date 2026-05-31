package com.example.irp.controller;

import com.example.irp.entity.Resource;
import com.example.irp.entity.User;
import com.example.irp.repository.ResourceRepository;
import com.example.irp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @GetMapping("/home")
    public String home() {
        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "index";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String user_name,
                               @RequestParam String user_email,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {

        // 1. 🟢 Unique Email Validation Check
        if (userRepository.existsByUserEmail(user_email)) {
            // Agar email pehle se exist karta hai, toh register na karein aur error message bhein
            redirectAttributes.addFlashAttribute("error", "Email already registered! Try another one.");
            return "redirect:/?error=email_exists";
        }

        // 2. Agar email unique hai, toh naya user save karein (Password same ho sakta hai)
        User user = new User();
        user.setUserName(user_name);
        user.setUserEmail(user_email);
        user.setUserPassword(password);
        user.setRole("USER");
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Registration Successful!");
        return "redirect:/?registered=true";
    }

    @PostMapping("/login")
    public String login(@RequestParam String user_email,
                        @RequestParam String password,
                        HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        session = request.getSession(true);

        Optional<User> user = userRepository.findByUserEmailAndPassword(user_email, password);

        if (user.isEmpty()) {
            return "index";
        }

        if (user.get().getRole().equalsIgnoreCase("admin")) {
            session.setAttribute("userId", user.get().getUserId());
            session.setAttribute("username", user.get().getUserName());
            return "redirect:/admin/dashboard";
        }

        session.setAttribute("userId", user.get().getUserId());
        session.setAttribute("username", user.get().getUserName());
        return "redirect:/user/dashboard";
    }
}