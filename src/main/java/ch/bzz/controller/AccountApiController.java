package ch.bzz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountApiController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello World!";
    }
}
