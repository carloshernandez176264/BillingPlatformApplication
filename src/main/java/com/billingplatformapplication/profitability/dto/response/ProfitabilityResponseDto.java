package com.billingplatformapplication.profitability.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProfitabilityResponseDto(
        // Por desarrollador
        UUID       developerId,
        String     developerName,
        String     profileName,
        UUID       clientId,
        String     clientName,

        // Salario y carga prestacional
        BigDecimal baseSalary,
        BigDecimal socialCharges,      // 51.83% del salario
        BigDecimal totalCost,          // salario + cargas

        // Desglose cargas (Colombia)
        BigDecimal prima,              // 8.33%
        BigDecimal cesantias,          // 8.33%
        BigDecimal interesesCesantias, // 1.00%
        BigDecimal vacaciones,         // 4.17%
        BigDecimal saludEmpleador,     // 8.50%
        BigDecimal pensionEmpleador,   // 12.00%
        BigDecimal arl,                // 0.52%
        BigDecimal cajaCompensacion,   // 4.00%
        BigDecimal icbf,               // 3.00%
        BigDecimal sena,               // 2.00%

        // Ingreso (tarifa cliente)
        BigDecimal clientRate,
        BigDecimal baseRate,           // tarifa base del perfil
        BigDecimal discountAmount,     // baseRate - clientRate
        BigDecimal discountPct,        // % de descuento al cliente

        // Rentabilidad
        BigDecimal margin,             // clientRate - totalCost
        BigDecimal marginPct           // margin / clientRate * 100
) {}