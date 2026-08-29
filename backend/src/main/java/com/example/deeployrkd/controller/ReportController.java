package com.example.deeployrkd.controller;

import com.example.deeployrkd.dto.EmailReportRequest;
import com.example.deeployrkd.dto.EmailReportResponse;
import com.example.deeployrkd.dto.ReportSummaryDto;
import com.example.deeployrkd.service.EmailService;
import com.example.deeployrkd.service.ExcelGeneratorService;
import com.example.deeployrkd.service.PdfGeneratorService;
import com.example.deeployrkd.service.SaleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final SaleService saleService;
    private final PdfGeneratorService pdfGeneratorService;
    private final ExcelGeneratorService excelGeneratorService;
    private final EmailService emailService;

    public ReportController(SaleService saleService, PdfGeneratorService pdfGeneratorService, ExcelGeneratorService excelGeneratorService, EmailService emailService) {
        this.saleService = saleService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.excelGeneratorService = excelGeneratorService;
        this.emailService = emailService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDto> getSummary(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String region
    ) {
        ReportSummaryDto summary = saleService.getReportSummary(month, from, to, region);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/sales.pdf")
    public ResponseEntity<byte[]> getPdfReport(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String region
    ) {
        ReportSummaryDto summary = saleService.getReportSummary(month, from, to, region);
        byte[] pdfBytes = pdfGeneratorService.generatePdfReport(summary);

        String filename = "sales-report-" + (month != null ? month : "period") + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    @GetMapping("/sales.xlsx")
    public ResponseEntity<byte[]> getExcelReport(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String region
    ) {
        ReportSummaryDto summary = saleService.getReportSummary(month, from, to, region);
        byte[] excelBytes = excelGeneratorService.generateExcelReport(summary);

        String filename = "sales-report-" + (month != null ? month : "period") + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelBytes.length)
                .body(excelBytes);
    }

    @PostMapping("/send")
    public ResponseEntity<EmailReportResponse> sendReport(
            @RequestBody(required = false) EmailReportRequest bodyRequest,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) List<String> emails
    ) {
        EmailReportRequest request = bodyRequest != null ? bodyRequest : new EmailReportRequest();

        if (month != null && request.getMonth() == null) {
            request.setMonth(month);
        }
        if (from != null && request.getFrom() == null) {
            request.setFrom(from);
        }
        if (to != null && request.getTo() == null) {
            request.setTo(to);
        }
        if (region != null && request.getRegion() == null) {
            request.setRegion(region);
        }
        if (emails != null && !emails.isEmpty() && (request.getEmails() == null || request.getEmails().isEmpty())) {
            request.setEmails(emails);
        }

        EmailReportResponse response = emailService.sendReportEmail(request);
        return ResponseEntity.ok(response);
    }
}
