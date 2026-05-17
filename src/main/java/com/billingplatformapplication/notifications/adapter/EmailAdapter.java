package com.billingplatformapplication.notifications.adapter;




import com.billingplatformapplication.notifications.service.EmailNotificationService;
import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter that decouples the domain from the email infrastructure.
 * If the email provider changes, only this adapter needs updating.
 */
@Component
@RequiredArgsConstructor
public class EmailAdapter {

    private final EmailNotificationService emailNotificationService;

    public void notifyPreInvoice(PreInvoiceResponseDto invoice) {
        emailNotificationService.sendPreInvoice(invoice);
    }

    public void notifyPasswordReset(String email, String tempPassword) {
        emailNotificationService.sendPasswordResetNotification(email, tempPassword);
    }
}
