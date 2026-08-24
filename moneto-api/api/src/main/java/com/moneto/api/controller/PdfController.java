package com.moneto.api.controller;

import com.moneto.api.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PdfController {

    private final PdfService pdfService;

    @GetMapping
    public ResponseEntity<byte[]> downloadReport() {
        byte[] pdf = pdfService.generateReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=moneto-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}