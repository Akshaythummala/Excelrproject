package com.Akshay.Excelrproject.Controller;

import com.Akshay.Excelrproject.DTO.SignInDTO;
import com.Akshay.Excelrproject.DTO.UserDTO;
import com.Akshay.Excelrproject.Model.User;
import com.Akshay.Excelrproject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    // Register a new user
    @PostMapping("/register")
    public String registerUser(@RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO);
        return "User registered successfully! Please check your email to verify.";
    }

    // Sign in an existing user
    @PostMapping("/signin")
    public String signInUser(@RequestBody SignInDTO signInDTO) {
        User user = userService.signIn(signInDTO.getEmail(), signInDTO.getPassword());
        return user != null ? "Sign-in successful!" : "Invalid credentials!";
    }

    // Verify user's email using the token
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);
        return "Email verified successfully!";
    }

    // Add user details (like address, certifications, etc.)
    @PostMapping("/addDetails/{userId}")
    public String addUserDetails(@PathVariable Long userId, @RequestBody UserDTO userDTO) {
        userService.addUserDetails(userId, userDTO);
        return "User details added successfully!";

    }

    // Get user details by ID
    @GetMapping("/user/{userId}")
    public User getUserDetails(@PathVariable Long userId) {
        return userService.getUserDetails(userId);
    }

    // Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }



    // Delete user by ID
    @DeleteMapping("/user/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return "User deleted successfully!";
    }
}