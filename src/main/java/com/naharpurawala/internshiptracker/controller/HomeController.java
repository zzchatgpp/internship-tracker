package com.naharpurawala.internshiptracker.controller;
import com.naharpurawala.internshiptracker.entity.User;
import com.naharpurawala.internshiptracker.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller @RequiredArgsConstructor
public class HomeController{
 private final CurrentUserService currentUserService; private final InternshipApplicationService internshipApplicationService;
 @GetMapping("/") public String home(Authentication authentication,Model model){
  User user=currentUserService.requireCurrentUser(authentication); model.addAttribute("user",user); model.addAttribute("stats",internshipApplicationService.getDashboardStats(user)); return "home";
 }
}
