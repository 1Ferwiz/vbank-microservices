package com.ejada.vbank.bffservice.controller;

import com.ejada.vbank.bffservice.dto.DashboardResponse;
import com.ejada.vbank.bffservice.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/bff")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/{userId}")
    public Mono<ResponseEntity<DashboardResponse>> getDashboard(@PathVariable UUID userId) {
        return dashboardService.getDashboard(userId)
                .map(ResponseEntity::ok);
    }
}
