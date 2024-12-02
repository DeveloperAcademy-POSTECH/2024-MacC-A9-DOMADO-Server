package com.onemorethink.domadosever.global.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DatabaseInitializeController {

    private final DatabaseInitializeService databaseInitializeService;

    @PostMapping("/reset-db")
    @ResponseStatus(HttpStatus.OK)
    public String resetDatabase() {
        databaseInitializeService.initializeDatabase();
        return "Database reset completed successfully";
    }
}