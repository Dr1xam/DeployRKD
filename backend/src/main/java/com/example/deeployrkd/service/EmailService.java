package com.example.deeployrkd.service;

import com.example.deeployrkd.dto.EmailReportRequest;
import com.example.deeployrkd.dto.EmailReportResponse;
import com.example.deeployrkd.dto.ReportSummaryDto;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final SaleService saleService;
    private final PdfGeneratorService pdfGeneratorService;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.reports.default-emails:ceo@example.com}")
    private String defaultEmails;

    public EmailService(JavaMailSender mailSender, SaleService saleService, PdfGeneratorService pdfGeneratorService) {
        this.mailSender = mailSender;
        this.saleService = saleService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    public EmailReportResponse sendReportEmail(EmailReportRequest request) {
        List<String> recipients = resolveRecipients(request);
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Список отримувачів не може бути порожнім. Вкажіть хоча б один email.");
        }

        ReportSummaryDto summary = saleService.getReportSummary(
                request.getMonth(),
                request.getFrom(),
                request.getTo(),
                request.getRegion()
        );

        byte[] pdfBytes = pdfGeneratorService.generatePdfReport(summary);

        String periodSlug = summary.getPeriodTitle().replaceAll("[^a-zA-Z0-9а-яА-ЯіїєґІЇЄҐ-]", "_");
        String attachmentFilename = "sales-report-" + (request.getMonth() != null ? request.getMonth() : periodSlug) + ".pdf";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            } else {
                helper.setFrom("sales-reports@deeployrkd.com");
            }

            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("Звіт про продажі компанії: " + summary.getPeriodTitle());

            String emailBody = buildHtmlEmailBody(summary);
            helper.setText(emailBody, true);

            // Add PDF Attachment
            helper.addAttachment(attachmentFilename, new ByteArrayResource(pdfBytes), "application/pdf");

            log.info("Відправка email-звіту на адресу(и): {} з вкладенням: {}", recipients, attachmentFilename);
            mailSender.send(message);

            return EmailReportResponse.builder()
                    .success(true)
                    .message("PDF-звіт успішно надіслано на " + recipients.size() + " адрес(и)")
                    .recipients(recipients)
                    .salesCount((int) summary.getTotalSalesCount())
                    .totalAmount(summary.getTotalAmount())
                    .period(summary.getPeriodTitle())
                    .build();

        } catch (Exception e) {
            log.error("Помилка при відправці email: {}", e.getMessage());
            return EmailReportResponse.builder()
                    .success(false)
                    .message("Помилка SMTP-сервера (" + e.getClass().getSimpleName() + "): " + e.getMessage() + ". Перевірте налаштування в .env.")
                    .recipients(recipients)
                    .salesCount((int) summary.getTotalSalesCount())
                    .totalAmount(summary.getTotalAmount())
                    .period(summary.getPeriodTitle())
                    .build();
        }
    }

    private List<String> resolveRecipients(EmailReportRequest request) {
        if (request != null && request.getEmails() != null && !request.getEmails().isEmpty()) {
            return request.getEmails().stream()
                    .filter(e -> e != null && !e.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        if (defaultEmails != null && !defaultEmails.isBlank()) {
            return Arrays.stream(defaultEmails.split(","))
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private String buildHtmlEmailBody(ReportSummaryDto summary) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='font-family: Arial, sans-serif; color: #1e293b; background-color: #f8fafc; margin: 0; padding: 20px;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: #ffffff; padding: 24px; border-radius: 8px; border: 1px solid #e2e8f0;'>"
                + "<h2 style='color: #1e3a8a; margin-top: 0;'>Звіт про продажі компанії</h2>"
                + "<p style='color: #64748b; font-size: 14px;'><strong>Період:</strong> " + summary.getPeriodTitle() + "</p>"
                + "<div style='background: #f1f5f9; padding: 16px; border-radius: 6px; margin: 20px 0;'>"
                + "<p style='margin: 4px 0;'>💰 <strong>Загальний виторг:</strong> " + summary.getTotalAmount() + " ₴</p>"
                + "<p style='margin: 4px 0;'>📊 <strong>Кількість угод:</strong> " + summary.getTotalSalesCount() + "</p>"
                + "<p style='margin: 4px 0;'>🏷️ <strong>Середній чек:</strong> " + summary.getAverageCheck() + " ₴</p>"
                + "</div>"
                + "<p>Детальний звіт із розбивкою по регіонах, топ-продавцях та графіком прикріплено у форматі <strong>PDF</strong> до цього листа.</p>"
                + "<hr style='border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;'/>"
                + "<p style='font-size: 12px; color: #94a3b8;'>Це автоматичне повідомлення від системи DeeployRKD. Не відповідайте на нього.</p>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
}
