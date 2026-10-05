package com.naharpurawala.internshiptracker.security;

import com.naharpurawala.internshiptracker.entity.User;
import com.naharpurawala.internshiptracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override public UserDetails loadUserByUsername(String email)throws UsernameNotFoundException{
        User user=userRepository.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("No account found for that email"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getPassword()).roles("USER").build();
    }
}
