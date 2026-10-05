package com.naharpurawala.internshiptracker.service;
import com.naharpurawala.internshiptracker.dto.SignupRequest;
import com.naharpurawala.internshiptracker.entity.User;
import com.naharpurawala.internshiptracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor
public class UserService{
 private final UserRepository userRepository; private final PasswordEncoder passwordEncoder;
 @Transactional public User register(SignupRequest request){
  String normalizedEmail=request.getEmail().trim().toLowerCase(Locale.ROOT);
  if(!request.getPassword().equals(request.getConfirmPassword()))throw new IllegalArgumentException("Passwords do not match");
  if(userRepository.existsByEmailIgnoreCase(normalizedEmail))throw new IllegalArgumentException("An account with that email already exists");
  return userRepository.save(User.builder().name(request.getName().trim()).email(normalizedEmail).password(passwordEncoder.encode(request.getPassword())).build());
 }
 @Transactional(readOnly=true) public Optional<User> findByEmail(String email){return userRepository.findByEmailIgnoreCase(email);}
 @Transactional(readOnly=true) public User requireByEmail(String email){return findByEmail(email).orElseThrow(()->new IllegalStateException("Authenticated user was not found"));}
}
