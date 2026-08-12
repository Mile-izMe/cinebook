package com.cinebook.module.payment.mapper;

import com.cinebook.module.payment.dto.response.PaymentResponse;
import com.cinebook.module.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "url", target = "paymentUrl")
    @Mapping(source = "payment.id", target = "paymentId")
    @Mapping(target = "bookingId", source = "payment.booking.id")
    PaymentResponse toResponse(Payment payment, String url);
}
