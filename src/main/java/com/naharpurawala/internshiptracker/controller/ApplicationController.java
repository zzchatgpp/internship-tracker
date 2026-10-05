package com.naharpurawala.internshiptracker.controller;

import com.naharpurawala.internshiptracker.dto.ApplicationRequest;
import com.naharpurawala.internshiptracker.entity.*;
import com.naharpurawala.internshiptracker.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final CurrentUserService currentUserService;
    private final InternshipApplicationService applicationService;

    @GetMapping
    public String list(@RequestParam(required=false,defaultValue="")String q,@RequestParam(required=false)ApplicationStage stage,@RequestParam(required=false,defaultValue="0")int page,Authentication authentication,Model model){
        User owner=currentUserService.requireCurrentUser(authentication); int safePage=Math.max(page,0);
        Page<InternshipApplication> applicationPage=applicationService.searchForOwner(owner,q,stage,safePage);
        if(applicationPage.getTotalPages()>0&&safePage>=applicationPage.getTotalPages()){safePage=applicationPage.getTotalPages()-1;applicationPage=applicationService.searchForOwner(owner,q,stage,safePage);}
        model.addAttribute("applicationPage",applicationPage); model.addAttribute("applications",applicationPage.getContent());
        model.addAttribute("currentUser",owner); model.addAttribute("stages",ApplicationStage.values()); model.addAttribute("q",q==null?"":q.trim()); model.addAttribute("selectedStage",stage);
        return "applications/list";
    }

    @GetMapping("/new")
    public String newForm(Model model){
        if(!model.containsAttribute("applicationRequest"))model.addAttribute("applicationRequest",new ApplicationRequest());
        model.addAttribute("stages",ApplicationStage.values()); model.addAttribute("pageTitle","Add application"); model.addAttribute("formAction","/applications"); return "applications/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("applicationRequest")ApplicationRequest request,BindingResult bindingResult,Authentication authentication,Model model,RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()){model.addAttribute("stages",ApplicationStage.values());model.addAttribute("pageTitle","Add application");model.addAttribute("formAction","/applications");return "applications/form";}
        User owner=currentUserService.requireCurrentUser(authentication); InternshipApplication created=applicationService.create(request,owner);
        redirectAttributes.addFlashAttribute("successMessage","Application added successfully."); return "redirect:/applications/"+created.getId();
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id,Authentication authentication,Model model){
        User owner=currentUserService.requireCurrentUser(authentication); InternshipApplication application=applicationService.requireOwnedApplication(id,owner);
        model.addAttribute("application",application); model.addAttribute("stageHistory",applicationService.getStageHistory(id,owner)); return "applications/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,Authentication authentication,Model model){
        User owner=currentUserService.requireCurrentUser(authentication); InternshipApplication application=applicationService.requireOwnedApplication(id,owner);
        if(!model.containsAttribute("applicationRequest"))model.addAttribute("applicationRequest",applicationService.toRequest(application));
        model.addAttribute("application",application);model.addAttribute("stages",ApplicationStage.values());model.addAttribute("pageTitle","Edit application");model.addAttribute("formAction","/applications/"+id);
        return "applications/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,@Valid @ModelAttribute("applicationRequest")ApplicationRequest request,BindingResult bindingResult,Authentication authentication,Model model,RedirectAttributes redirectAttributes){
        User owner=currentUserService.requireCurrentUser(authentication);
        if(bindingResult.hasErrors()){model.addAttribute("application",applicationService.requireOwnedApplication(id,owner));model.addAttribute("stages",ApplicationStage.values());model.addAttribute("pageTitle","Edit application");model.addAttribute("formAction","/applications/"+id);return "applications/form";}
        applicationService.update(id,request,owner);redirectAttributes.addFlashAttribute("successMessage","Application updated successfully.");return "redirect:/applications/"+id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,Authentication authentication,RedirectAttributes redirectAttributes){
        User owner=currentUserService.requireCurrentUser(authentication);applicationService.delete(id,owner);redirectAttributes.addFlashAttribute("successMessage","Application deleted.");return "redirect:/applications";
    }
}
