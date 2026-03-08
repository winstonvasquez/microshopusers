package com.microshop.users.infrastructure.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/swagger-ui/index.htm")
    public String redirectSwaggerHtm() {
        return "redirect:/swagger-ui/index.html";
    }
}
