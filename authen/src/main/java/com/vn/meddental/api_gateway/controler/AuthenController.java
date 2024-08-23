package com.vn.meddental.api_gateway.controler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenController {
    @GetMapping("/test/{id}")
    Boolean authen(@PathVariable Long id) {
        if (id == 1) {
            return true;
        }
        return false;
    }
}
