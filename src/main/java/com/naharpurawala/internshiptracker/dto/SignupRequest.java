package com.naharpurawala.internshiptracker.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class SignupRequest {
 @NotBlank(message="Name is required") @Size(min=2,max=100) private String name;
 @NotBlank(message="Email is required") @Email(message="Enter a valid email address") @Size(max=190) private String email;
 @NotBlank(message="Password is required") @Size(min=8,max=72) private String password;
 @NotBlank(message="Please confirm your password") private String confirmPassword;
}
