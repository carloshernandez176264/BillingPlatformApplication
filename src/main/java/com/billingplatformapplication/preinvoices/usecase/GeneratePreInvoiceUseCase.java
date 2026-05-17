package com.billingplatformapplication.preinvoices.usecase;


import com.billingplatformapplication.preinvoices.dto.request.GeneratePreInvoiceRequestDto;
import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceResponseDto;
import com.billingplatformapplication.preinvoices.service.PreInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneratePreInvoiceUseCase {

    private final PreInvoiceService preInvoiceService;

    public PreInvoiceResponseDto execute(GeneratePreInvoiceRequestDto request) {
        return preInvoiceService.generate(request);
    }
}
