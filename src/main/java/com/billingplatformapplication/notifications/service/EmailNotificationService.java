package com.billingplatformapplication.notifications.service;


import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service

public class EmailNotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Async
    public void sendPreInvoice(PreInvoiceResponseDto invoice) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(invoice.getClientBillingEmail());
            msg.setSubject("Pre-Invoice " + invoice.getInvoiceNumber()
                    + " — " + invoice.getPeriodDescription());
            msg.setText(buildBody(invoice));
            mailSender.send(msg);
            log.info("Pre-invoice email sent: {} to {}",
                    invoice.getInvoiceNumber(), invoice.getClientBillingEmail());
        } catch (Exception e) {
            log.error("Failed to send pre-invoice email {}: {}",
                    invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetNotification(String email, String tempPassword) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Billing Platform — Password Reset");
            msg.setText("Your temporary password is: " + tempPassword +
                    "\nPlease change it immediately after login.");
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
        }
    }

    private String buildBody(PreInvoiceResponseDto inv) {
        return String.format(
                "Dear %s,\n\n" +
                        "Please find attached pre-invoice %s for period %s.\n\n" +
                        "Total Amount: %s %s\n\n" +
                        "Regards,\nBilling Platform",
                inv.getClientName(),
                inv.getInvoiceNumber(),
                inv.getPeriodDescription(),
                inv.getCurrencyCode(),
                inv.getTotalAmount() != null ? inv.getTotalAmount().toPlainString() : "0.00");
    }
}

