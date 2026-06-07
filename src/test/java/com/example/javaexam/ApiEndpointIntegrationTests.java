package com.example.javaexam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.javaexam.models.Bill;
import com.example.javaexam.repositories.BillRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
class ApiEndpointIntegrationTests {

    private static final String ADMIN_EMAIL = "admin@utility.local";
    private static final String ADMIN_PASSWORD = "admin12345";
    private static final String DEFAULT_PASSWORD = "SecurePass123";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Autowired
    private BillRepository billRepository;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void authAndCurrentUserEndpointsCoverHappyPathAndInvalidCredentials() throws Exception {
        String customerEmail = uniqueEmail("signup");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", "Portal Customer",
                                "email", customerEmail,
                                "phoneNumber", uniquePhone(),
                                "nationalId", uniqueNationalId(),
                                "address", "Kigali, Gasabo, Remera",
                                "password", DEFAULT_PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.user.email").value(customerEmail))
                .andExpect(jsonPath("$.user.roles[0]").value("ROLE_CUSTOMER"))
                .andExpect(jsonPath("$.user.customerId").isNumber());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", "Invalid Signup",
                                "email", uniqueEmail("bad-signup"),
                                "phoneNumber", "12345",
                                "nationalId", uniqueNationalId(),
                                "address", "Kigali",
                                "password", DEFAULT_PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phoneNumber").exists());

        String token = login(customerEmail, DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/users/me")
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(customerEmail))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CUSTOMER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", customerEmail,
                                "password", "WrongPass123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        mockMvc.perform(post("/api/auth/logout")
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me")
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminUserAndCustomerEndpointsCoverHappyPathAndInvalidPayloads() throws Exception {
        String adminToken = loginAsAdmin();

        CreatedUser financeUser = createManagedUser(adminToken, "ROLE_FINANCE");
        CreatedUser operatorUser = createManagedUser(adminToken, "ROLE_OPERATOR");
        CreatedUser customerUser = createManagedCustomer(adminToken);

        mockMvc.perform(get("/api/users")
                        .header(AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        mockMvc.perform(patch("/api/users/{userId}/status", financeUser.userId())
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "INACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(financeUser.userId()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(get("/api/customers")
                        .header(AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/customers/{customerId}", customerUser.customerId())
                        .header(AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerUser.customerId()))
                .andExpect(jsonPath("$.email").value(customerUser.email()));

        String updatedCustomerEmail = uniqueEmail("updated-customer");
        String updatedPhone = uniquePhone();

        mockMvc.perform(put("/api/customers/{customerId}", customerUser.customerId())
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", "Updated Managed Customer",
                                "nationalId", uniqueNationalId(),
                                "email", updatedCustomerEmail.toUpperCase(),
                                "phoneNumber", updatedPhone,
                                "address", "Kigali, Kicukiro",
                                "status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerUser.customerId()))
                .andExpect(jsonPath("$.email").value(updatedCustomerEmail))
                .andExpect(jsonPath("$.phoneNumber").value(updatedPhone));

        mockMvc.perform(patch("/api/customers/{customerId}/status", customerUser.customerId())
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "INACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(post("/api/users")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", "Too Many Roles",
                                "email", uniqueEmail("multi-role"),
                                "phoneNumber", uniquePhone(),
                                "nationalId", "",
                                "address", "",
                                "password", DEFAULT_PASSWORD,
                                "status", "ACTIVE",
                                "roles", List.of("ROLE_OPERATOR", "ROLE_FINANCE")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A user must have exactly one role"));

        mockMvc.perform(put("/api/customers/{customerId}", customerUser.customerId())
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", "Still Invalid",
                                "nationalId", uniqueNationalId(),
                                "email", uniqueEmail("invalid-customer"),
                                "phoneNumber", "111",
                                "address", "Kigali",
                                "status", "ACTIVE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phoneNumber").exists());

        assertThat(operatorUser.userId()).isPositive();
    }

    @Test
    void tariffEndpointsCoverHappyPathAuthorizationAndDateSpecificValidation() throws Exception {
        String adminToken = loginAsAdmin();
        CreatedUser financeUser = createManagedUser(adminToken, "ROLE_FINANCE");
        String financeToken = login(financeUser.email(), DEFAULT_PASSWORD);
        LocalDate firstOfNextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1);

        mockMvc.perform(post("/api/tariffs")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(tariffPayload(
                                "WATER",
                                "TIERED",
                                firstOfNextMonth,
                                "25.00",
                                "0.1800",
                                "0.1000",
                                List.of(
                                        tier(0, 10, "12.50"),
                                        tier(10, 20, "15.00"),
                                        tier(20, null, "18.00"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.meterType").value("WATER"))
                .andExpect(jsonPath("$.tiers.length()").value(3));

        mockMvc.perform(get("/api/tariffs")
                        .header(AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/tariffs")
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/tariffs")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(tariffPayload(
                                "ELECTRICITY",
                                "FLAT",
                                firstOfNextMonth.plusDays(4),
                                "15.00",
                                "0.1800",
                                "0.1000",
                                List.of(tier(0, null, "22.00"))))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tariff effective date must be the first day of a month"));

        mockMvc.perform(post("/api/tariffs")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(tariffPayload(
                                "ELECTRICITY",
                                "TIERED",
                                firstOfNextMonth,
                                "15.00",
                                "0.1800",
                                "0.1000",
                                List.of(
                                        tier(0, 10, "20.00"),
                                        tier(12, null, "25.00"))))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tier ranges must be contiguous"));
    }

    @Test
    void meterEndpointsCoverHappyPathInvalidEnumsAndChronologicalDateRules() throws Exception {
        String adminToken = loginAsAdmin();
        CreatedUser operatorUser = createManagedUser(adminToken, "ROLE_OPERATOR");
        String operatorToken = login(operatorUser.email(), DEFAULT_PASSWORD);
        CreatedUser customerUser = createManagedCustomer(adminToken);

        LocalDate installationDate = LocalDate.now().minusDays(10);
        LocalDate firstReadingDate = LocalDate.now().minusMonths(1).withDayOfMonth(10);

        JsonNode meter = createMeter(adminToken, customerUser.customerId(), installationDate);
        long meterId = meter.path("id").asLong();

        mockMvc.perform(get("/api/meters")
                        .header(AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/meters/{meterId}", meterId)
                        .header(AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(meterId))
                .andExpect(jsonPath("$.customerId").value(customerUser.customerId()));

        mockMvc.perform(put("/api/meters/{meterId}", meterId)
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "billingMode", "POSTPAID",
                                "installationDate", installationDate.minusDays(3),
                                "status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installationDate").value(installationDate.minusDays(3).toString()));

        mockMvc.perform(post("/api/meters/readings")
                        .header(AUTHORIZATION, bearer(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "meterId", meterId,
                                "previousReading", new BigDecimal("0.00"),
                                "currentReading", new BigDecimal("25.50"),
                                "readingDate", firstReadingDate))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.meterId").value(meterId))
                .andExpect(jsonPath("$.consumption").value(25.50));

        mockMvc.perform(get("/api/meters/readings/all")
                        .header(AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(post("/api/meters")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "customerId", customerUser.customerId(),
                                "meterNumber", "MTR-INVALID-1",
                                "meterType", "WATER",
                                "billingMode", "PREPAID",
                                "installationDate", installationDate,
                                "status", "ACTIVE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body could not be parsed"));

        mockMvc.perform(post("/api/meters")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "customerId", customerUser.customerId(),
                                "meterNumber", "MTR-FUTURE-1",
                                "meterType", "WATER",
                                "billingMode", "POSTPAID",
                                "installationDate", LocalDate.now().plusDays(1),
                                "status", "ACTIVE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.installationDate").value("Installation date cannot be in the future"));

        mockMvc.perform(post("/api/meters/readings")
                        .header(AUTHORIZATION, bearer(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "meterId", meterId,
                                "previousReading", new BigDecimal("25.50"),
                                "currentReading", new BigDecimal("30.00"),
                                "readingDate", LocalDate.now().plusDays(1)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.readingDate").value("Reading date cannot be in the future"));

        mockMvc.perform(post("/api/meters/readings")
                        .header(AUTHORIZATION, bearer(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "meterId", meterId,
                                "previousReading", new BigDecimal("25.50"),
                                "currentReading", new BigDecimal("30.00"),
                                "readingDate", firstReadingDate.plusDays(1)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Meter readings must be captured in chronological monthly order"));
    }

    @Test
    void billingEndpointsCoverHappyPathDuplicateProtectionAndDateValidation() throws Exception {
        String adminToken = loginAsAdmin();
        CreatedUser financeUser = createManagedUser(adminToken, "ROLE_FINANCE");
        String financeToken = login(financeUser.email(), DEFAULT_PASSWORD);
        CreatedUser customerUser = createManagedCustomer(adminToken);

        LocalDate previousMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate firstReadingDate = LocalDate.now().minusMonths(1)
                .withDayOfMonth(Math.min(10, LocalDate.now().minusMonths(1).lengthOfMonth()));
        LocalDate secondReadingDate = LocalDate.now()
                .withDayOfMonth(Math.min(10, LocalDate.now().getDayOfMonth()));

        seedWaterTariff(adminToken, previousMonthStart);
        JsonNode meter = createMeter(adminToken, customerUser.customerId(), LocalDate.now().minusDays(45));
        long meterId = meter.path("id").asLong();
        JsonNode reading = captureReading(adminToken, meterId, "0.00", "42.00", firstReadingDate);
        long readingId = reading.path("id").asLong();

        JsonNode bill = createBill(financeToken, readingId, LocalDate.now().plusDays(15));
        long billId = bill.path("id").asLong();
        String billReference = bill.path("billReference").asText();

        mockMvc.perform(get("/api/bills")
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/bills/{billReference}", billReference)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billReference").value(billReference));

        mockMvc.perform(post("/api/bills/{billId}/approve", billId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        Bill overdueBill = billRepository.findById(billId).orElseThrow();
        overdueBill.setDueDate(LocalDate.now().minusDays(1));
        billRepository.save(overdueBill);

        mockMvc.perform(post("/api/bills/{billId}/late-penalty", billId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.penaltyAmount").isNumber())
                .andExpect(jsonPath("$.lineItems.length()").value(4));

        mockMvc.perform(post("/api/bills/generate")
                        .header(AUTHORIZATION, bearer(financeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "readingId", readingId,
                                "dueDate", LocalDate.now().plusDays(10)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A bill has already been generated for this reading"));

        JsonNode laterReading = captureReading(adminToken, meterId, "42.00", "55.00", secondReadingDate);

        mockMvc.perform(post("/api/bills/generate")
                        .header(AUTHORIZATION, bearer(financeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "readingId", laterReading.path("id").asLong(),
                                "dueDate", LocalDate.now()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dueDate").value("Bill due date must be in the future"));

        mockMvc.perform(get("/api/bills/{billReference}", "BAD"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/bills/{billReference}", "BAD")
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void paymentPortalAndNotificationEndpointsCoverHappyPathRoleChecksAndDateValidation() throws Exception {
        String adminToken = loginAsAdmin();
        CreatedUser financeUser = createManagedUser(adminToken, "ROLE_FINANCE");
        String financeToken = login(financeUser.email(), DEFAULT_PASSWORD);

        String customerEmail = uniqueEmail("portal-flow");
        signupCustomer(customerEmail);
        String customerToken = login(customerEmail, DEFAULT_PASSWORD);
        long customerId = currentUser(customerToken).path("customerId").asLong();

        seedWaterTariff(adminToken, LocalDate.now().withDayOfMonth(1));
        JsonNode meter = createMeter(adminToken, customerId, LocalDate.now().minusDays(20));
        JsonNode reading = captureReading(adminToken, meter.path("id").asLong(), "0.00", "30.00", LocalDate.now().minusDays(3));
        JsonNode bill = createBill(financeToken, reading.path("id").asLong(), LocalDate.now().plusDays(12));
        long billId = bill.path("id").asLong();
        String billReference = bill.path("billReference").asText();
        String fullPaymentAmount = bill.path("totalAmount").decimalValue().toPlainString();

        mockMvc.perform(get("/api/notifications/customer/{customerId}", customerId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/bills/{billId}/approve", billId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/notifications/customer/{customerId}", customerId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notificationType").value("BILL_GENERATED"));

        JsonNode payment = recordPayment(financeToken, billReference, fullPaymentAmount, LocalDate.now());
        long paymentId = payment.path("id").asLong();

        mockMvc.perform(get("/api/payments")
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(post("/api/payments/{paymentId}/approve", paymentId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/customer/bills")
                        .header(AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].billReference").value(billReference));

        mockMvc.perform(get("/api/customer/payments")
                        .header(AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].billReference").value(billReference));

        mockMvc.perform(get("/api/customer/notifications")
                        .header(AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notificationType").value("PAYMENT_COMPLETED"))
                .andExpect(jsonPath("$[1].notificationType").value("BILL_GENERATED"));

        mockMvc.perform(get("/api/notifications/customer/{customerId}", customerId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(post("/api/payments")
                        .header(AUTHORIZATION, bearer(financeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "billReference", billReference,
                                "amountPaid", new BigDecimal("5.00"),
                                "paymentMethod", "MOMO",
                                "paymentDate", "07/06/2026"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body could not be parsed"));

        mockMvc.perform(post("/api/payments")
                        .header(AUTHORIZATION, bearer(financeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "billReference", billReference,
                                "amountPaid", new BigDecimal("9999.00"),
                                "paymentMethod", "CASH",
                                "paymentDate", LocalDate.now()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payment amount cannot exceed the outstanding balance"));

        mockMvc.perform(get("/api/notifications/customer/{customerId}", customerId)
                        .header(AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/customer/bills")
                        .header(AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void partialPaymentApprovalCreatesCustomerNotification() throws Exception {
        String adminToken = loginAsAdmin();
        CreatedUser financeUser = createManagedUser(adminToken, "ROLE_FINANCE");
        String financeToken = login(financeUser.email(), DEFAULT_PASSWORD);

        String customerEmail = uniqueEmail("partial-payment");
        signupCustomer(customerEmail);
        String customerToken = login(customerEmail, DEFAULT_PASSWORD);
        long customerId = currentUser(customerToken).path("customerId").asLong();

        seedWaterTariff(adminToken, LocalDate.now().withDayOfMonth(1));
        JsonNode meter = createMeter(adminToken, customerId, LocalDate.now().minusDays(20));
        JsonNode reading = captureReading(adminToken, meter.path("id").asLong(), "0.00", "30.00", LocalDate.now().minusDays(3));
        JsonNode bill = createBill(financeToken, reading.path("id").asLong(), LocalDate.now().plusDays(12));
        long billId = bill.path("id").asLong();
        String billReference = bill.path("billReference").asText();

        mockMvc.perform(post("/api/bills/{billId}/approve", billId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        JsonNode payment = recordPayment(financeToken, billReference, "10.00", LocalDate.now());
        long paymentId = payment.path("id").asLong();

        mockMvc.perform(post("/api/payments/{paymentId}/approve", paymentId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/customer/notifications")
                        .header(AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notificationType").value("PARTIAL_PAYMENT_RECEIVED"))
                .andExpect(jsonPath("$[0].paymentReference").isNotEmpty())
                .andExpect(jsonPath("$[1].notificationType").value("BILL_GENERATED"));

        mockMvc.perform(get("/api/customer/bills")
                        .header(AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PARTIALLY_PAID"));

        mockMvc.perform(get("/api/notifications/customer/{customerId}", customerId)
                        .header(AUTHORIZATION, bearer(financeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notificationType").value("PARTIAL_PAYMENT_RECEIVED"));
    }

    private JsonNode currentUser(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me")
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result);
    }

    private void signupCustomer(String email) throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", "Portal Flow Customer",
                                "email", email,
                                "phoneNumber", uniquePhone(),
                                "nationalId", uniqueNationalId(),
                                "address", "Kigali, Nyarugenge",
                                "password", DEFAULT_PASSWORD))))
                .andExpect(status().isCreated());
    }

    private CreatedUser createManagedCustomer(String adminToken) throws Exception {
        return createUser(adminToken, "ROLE_CUSTOMER", true);
    }

    private CreatedUser createManagedUser(String adminToken, String role) throws Exception {
        return createUser(adminToken, role, false);
    }

    private CreatedUser createUser(String adminToken, String role, boolean customerRole) throws Exception {
        String email = uniqueEmail(role.toLowerCase());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fullName", customerRole ? "Managed Customer" : "Managed Staff");
        payload.put("email", email);
        payload.put("phoneNumber", uniquePhone());
        payload.put("nationalId", customerRole ? uniqueNationalId() : "");
        payload.put("address", customerRole ? "Kigali, Gasabo" : "");
        payload.put("password", DEFAULT_PASSWORD);
        payload.put("status", "ACTIVE");
        payload.put("roles", List.of(role));

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode user = readJson(result);
        return new CreatedUser(
                user.path("id").asLong(),
                user.hasNonNull("customerId") ? user.path("customerId").asLong() : null,
                user.path("email").asText());
    }

    private JsonNode createMeter(String adminToken, long customerId, LocalDate installationDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/meters")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "customerId", customerId,
                                "meterNumber", uniqueMeterNumber(),
                                "meterType", "WATER",
                                "billingMode", "POSTPAID",
                                "installationDate", installationDate,
                                "status", "ACTIVE"))))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result);
    }

    private JsonNode captureReading(
            String token,
            long meterId,
            String previousReading,
            String currentReading,
            LocalDate readingDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/meters/readings")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "meterId", meterId,
                                "previousReading", new BigDecimal(previousReading),
                                "currentReading", new BigDecimal(currentReading),
                                "readingDate", readingDate))))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result);
    }

    private void seedWaterTariff(String adminToken, LocalDate effectiveFrom) throws Exception {
        mockMvc.perform(post("/api/tariffs")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(tariffPayload(
                                "WATER",
                                "TIERED",
                                effectiveFrom,
                                "20.00",
                                "0.1800",
                                "0.1000",
                                List.of(
                                        tier(0, 10, "10.00"),
                                        tier(10, 25, "12.00"),
                                        tier(25, null, "15.00"))))))
                .andExpect(status().isCreated());
    }

    private JsonNode createBill(String financeToken, long readingId, LocalDate dueDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/bills/generate")
                        .header(AUTHORIZATION, bearer(financeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "readingId", readingId,
                                "dueDate", dueDate))))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result);
    }

    private JsonNode recordPayment(String financeToken, String billReference, String amount, LocalDate paymentDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/payments")
                        .header(AUTHORIZATION, bearer(financeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "billReference", billReference,
                                "amountPaid", new BigDecimal(amount),
                                "paymentMethod", "MOMO",
                                "paymentDate", paymentDate))))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result);
    }

    private Map<String, Object> tier(int fromUnit, Integer toUnit, String pricePerUnit) {
        Map<String, Object> tier = new LinkedHashMap<>();
        tier.put("fromUnit", fromUnit);
        tier.put("toUnit", toUnit);
        tier.put("pricePerUnit", new BigDecimal(pricePerUnit));
        return tier;
    }

    private Map<String, Object> tariffPayload(
            String meterType,
            String tariffType,
            LocalDate effectiveFrom,
            String fixedServiceCharge,
            String vatRate,
            String latePaymentPenaltyRate,
            List<Map<String, Object>> tiers) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("meterType", meterType);
        payload.put("tariffType", tariffType);
        payload.put("effectiveFrom", effectiveFrom);
        payload.put("fixedServiceCharge", new BigDecimal(fixedServiceCharge));
        payload.put("vatRate", new BigDecimal(vatRate));
        payload.put("latePaymentPenaltyRate", new BigDecimal(latePaymentPenaltyRate));
        payload.put("tiers", tiers);
        return payload;
    }

    private String loginAsAdmin() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result).path("accessToken").asText();
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + ThreadLocalRandom.current().nextInt(100_000, 999_999) + "@example.com";
    }

    private String uniquePhone() {
        return "+25078" + ThreadLocalRandom.current().nextInt(1_000_000, 9_999_999);
    }

    private String uniqueNationalId() {
        long value = ThreadLocalRandom.current().nextLong(0, 10_000_000_000_000_000L);
        return "%016d".formatted(value);
    }

    private String uniqueMeterNumber() {
        return "MTR-" + ThreadLocalRandom.current().nextInt(100_000, 999_999);
    }

    private record CreatedUser(Long userId, Long customerId, String email) {
    }
}
