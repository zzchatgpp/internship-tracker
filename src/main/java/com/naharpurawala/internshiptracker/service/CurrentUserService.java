package com.naharpurawala.internshiptracker.service;
import com.naharpurawala.internshiptracker.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class CurrentUserService{
 private final UserService userService;
 public User requireCurrentUser(Authentication authentication){
  if(authentication==null||!authentication.isAuthenticated())throw new IllegalStateException("No authenticated user");
  return userService.requireByEmail(authentication.getName());
 }
}
