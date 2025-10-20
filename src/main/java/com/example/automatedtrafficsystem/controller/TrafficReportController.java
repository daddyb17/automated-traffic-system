package com.example.automatedtrafficsystem.controller;

import com.example.automatedtrafficsystem.service.TrafficReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class TrafficReportController {

    private final TrafficReportService trafficReportService;

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getTextReport() {
        return ResponseEntity.ok(trafficReportService.generateReport());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getJsonReport() {
        return ResponseEntity.ok(trafficReportService.getReportAsJson());
    }
}
