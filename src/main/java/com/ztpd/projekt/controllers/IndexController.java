package com.ztpd.projekt.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
    @RequestMapping("/welcome")
    String index() {
        return "index";
    }
}
