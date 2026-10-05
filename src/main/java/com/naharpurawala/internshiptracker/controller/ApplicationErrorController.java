package com.naharpurawala.internshiptracker.controller;
import com.naharpurawala.internshiptracker.service.InternshipApplicationService.ApplicationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@ControllerAdvice
public class ApplicationErrorController{
 @ExceptionHandler(ApplicationNotFoundException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
 public String handleNotFound(ApplicationNotFoundException exception,Model model){model.addAttribute("message","That internship application does not exist or does not belong to your account.");return "errors/404";}
}
