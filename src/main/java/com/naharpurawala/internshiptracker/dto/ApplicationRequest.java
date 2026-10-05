package com.naharpurawala.internshiptracker.dto;
import com.naharpurawala.internshiptracker.entity.ApplicationStage;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import java.time.LocalDate;
@Getter @Setter
public class ApplicationRequest {
 @NotBlank(message="Company name is required") @Size(max=160) private String company;
 @NotBlank(message="Role is required") @Size(max=160) private String role;
 @Size(max=160) private String location;
 @URL(message="Enter a valid URL including http:// or https://") @Size(max=500) private String jobUrl;
 private LocalDate appliedDate;
 @NotNull(message="Stage is required") private ApplicationStage stage=ApplicationStage.APPLIED;
 @Size(max=2000) private String notes;
}
