package com.bj.ilji_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api")
@RestController
public class APIController {

    @GetMapping("/test")
    public String test(){
        log.info("test");
        return "tttt";
    }
}
