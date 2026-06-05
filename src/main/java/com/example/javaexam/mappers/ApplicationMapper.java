package com.example.javaexam.mappers;

import com.example.javaexam.dtos.billing.BillLineItemResponse;
import com.example.javaexam.dtos.billing.BillResponse;
import com.example.javaexam.dtos.customer.CustomerResponse;
import com.example.javaexam.dtos.meter.MeterReadingResponse;
import com.example.javaexam.dtos.meter.MeterResponse;
import com.example.javaexam.dtos.notification.NotificationResponse;
import com.example.javaexam.dtos.payment.PaymentResponse;
import com.example.javaexam.dtos.tariff.TariffTierResponse;
import com.example.javaexam.dtos.tariff.TariffVersionResponse;
import com.example.javaexam.dtos.user.UserResponse;
import com.example.javaexam.models.Bill;
import com.example.javaexam.models.BillLineItem;
import com.example.javaexam.models.Customer;
import com.example.javaexam.models.Meter;
import com.example.javaexam.models.MeterReading;
import com.example.javaexam.models.Notification;
import com.example.javaexam.models.Payment;
import com.example.javaexam.models.Role;
import com.example.javaexam.models.TariffTier;
import com.example.javaexam.models.TariffVersion;
import com.example.javaexam.models.User;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapStructConfig.class)
public interface ApplicationMapper {

    @Mapping(target = "profileId", source = "user.profile.id")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "fullName", source = "user.profile.fullName")
    @Mapping(target = "email", source = "user.profile.email")
    @Mapping(target = "phoneNumber", source = "user.profile.phoneNumber")
    @Mapping(target = "nationalId", source = "user.profile.nationalId")
    @Mapping(target = "address", source = "user.profile.address")
    @Mapping(target = "status", source = "user.profile.status")
    @Mapping(target = "roles", source = "user.roles", qualifiedByName = "roleNames")
    UserResponse toUserResponse(User user, Long customerId);

    @Mapping(target = "profileId", source = "profile.id")
    @Mapping(target = "fullName", source = "profile.fullName")
    @Mapping(target = "nationalId", source = "profile.nationalId")
    @Mapping(target = "email", source = "profile.email")
    @Mapping(target = "phoneNumber", source = "profile.phoneNumber")
    @Mapping(target = "address", source = "profile.address")
    @Mapping(target = "status", source = "profile.status")
    CustomerResponse toCustomerResponse(Customer customer);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.profile.fullName")
    MeterResponse toMeterResponse(Meter meter);

    @Mapping(target = "meterId", source = "meter.id")
    @Mapping(target = "meterNumber", source = "meter.meterNumber")
    @Mapping(target = "customerName", source = "meter.customer.profile.fullName")
    @Mapping(target = "consumption", expression = "java(reading.getConsumption())")
    MeterReadingResponse toMeterReadingResponse(MeterReading reading);

    TariffVersionResponse toTariffVersionResponse(TariffVersion tariffVersion);

    TariffTierResponse toTariffTierResponse(TariffTier tier);

    List<TariffTierResponse> toTariffTierResponses(List<TariffTier> tiers);

    @Mapping(target = "customerId", source = "bill.customer.id")
    @Mapping(target = "customerName", source = "bill.customer.profile.fullName")
    @Mapping(target = "meterId", source = "bill.meter.id")
    @Mapping(target = "meterNumber", source = "bill.meter.meterNumber")
    @Mapping(target = "lineItems", source = "lineItems")
    BillResponse toBillResponse(Bill bill, List<BillLineItem> lineItems);

    BillLineItemResponse toBillLineItemResponse(BillLineItem billLineItem);

    List<BillLineItemResponse> toBillLineItemResponses(List<BillLineItem> billLineItems);

    @Mapping(target = "billReference", source = "bill.billReference")
    @Mapping(target = "customerName", source = "bill.customer.profile.fullName")
    PaymentResponse toPaymentResponse(Payment payment);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.profile.fullName")
    @Mapping(target = "billReference", source = "bill.billReference")
    @Mapping(target = "paymentReference", source = "payment.paymentReference")
    NotificationResponse toNotificationResponse(Notification notification);

    @Named("roleNames")
    default List<String> toRoleNames(Set<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
    }
}
