package com.scheduler.distributed_job_scheduler_backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("test/error")
    public String triggerError(){
        throw new RuntimeException("Testing global exception handler");
    }
}
