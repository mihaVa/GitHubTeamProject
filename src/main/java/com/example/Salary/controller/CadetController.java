package com.example.Salary.controller;

import com.example.Salary.model.Cadet;
import com.example.Salary.repository.CadetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
public class CadetController {

    @Autowired
    private CadetRepository repository;


    @GetMapping("/")
    public String getAllCadet(Model model) {
        List<Cadet> cadets = repository.findAll();
        model.addAttribute("cadets",cadets);
        return "index";


    }


    public String saveBonus(@ModelAttribute ("cadet") Cadet cadet){
        repository.save(cadet);
        return "redirect:/";
    }

}
