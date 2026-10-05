package com.naharpurawala.internshiptracker.controller;
import com.naharpurawala.internshiptracker.dto.SignupRequest;
import com.naharpurawala.internshiptracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
@Controller @RequiredArgsConstructor
public class AuthController{
 private final UserService userService;
 @GetMapping("/login") public String login(){return "auth/login";}
 @GetMapping("/signup") public String signupForm(Model model){model.addAttribute("signupRequest",new SignupRequest());return "auth/signup";}
 @PostMapping("/signup") public String signup(@Valid @ModelAttribute("signupRequest") SignupRequest request,BindingResult bindingResult){
  if(!request.getPassword().equals(request.getConfirmPassword()))bindingResult.rejectValue("confirmPassword","password.mismatch","Passwords do not match");
  if(bindingResult.hasErrors())return "auth/signup";
  try{userService.register(request);}catch(IllegalArgumentException ex){bindingResult.reject("signup.failed",ex.getMessage());return "auth/signup";}
  return "redirect:/login?registered";
 }
}
