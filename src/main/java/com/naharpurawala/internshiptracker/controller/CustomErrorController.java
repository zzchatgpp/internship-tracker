package com.naharpurawala.internshiptracker.controller;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class CustomErrorController implements ErrorController{
 @GetMapping("/access-denied") @ResponseStatus(HttpStatus.FORBIDDEN) public String accessDenied(){return "errors/403";}
 @RequestMapping("/error") public String handleError(HttpServletRequest request,Model model){
  Object statusObject=request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE); int statusCode=statusObject instanceof Integer?(Integer)statusObject:500;
  if(statusCode==403){model.addAttribute("status",403);model.addAttribute("title","Access denied");model.addAttribute("message","You do not have permission to access this page.");return "errors/403";}
  if(statusCode==404){model.addAttribute("status",404);model.addAttribute("title","Page not found");model.addAttribute("message","The page you requested could not be found.");return "errors/404";}
  model.addAttribute("status",statusCode);model.addAttribute("title","Something went wrong");model.addAttribute("message","An unexpected error occurred. Please return to the dashboard and try again.");return "errors/500";
 }
}
