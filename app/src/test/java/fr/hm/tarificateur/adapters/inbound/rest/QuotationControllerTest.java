package fr.hm.tarificateur.adapters.inbound.rest;

import fr.hm.tarificateur.adapters.inbound.rest.dto.*;
import fr.hm.tarificateur.domain.model.Quotation;
import fr.hm.tarificateur.domain.model.QuotationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import fr.hm.tarificateur.ports.inbound.QuotationInboundPort;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuotationController Tests")
class QuotationControllerTest {

    @Mock
    private QuotationInboundPort quotationInboundPort;

    @InjectMocks
    private QuotationController quotationController;

    private QuotationRequest validQuotationRequest;
    private List<QuotationResult> mockQuotationResults;

    @BeforeEach
    void setUp() {
        validQuotationRequest = createValidQuotationRequest();
        mockQuotationResults = List.of(createMockQuotationResult());
    }

    // ==================== VALID REQUEST TESTS ====================

    @Test
    @DisplayName("Should create quotation with valid request and return 200 OK")
    void testCreateQuotationValidRequest() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getQuotationId()).isEqualTo("QT-2025-001");
        verify(quotationInboundPort, times(1)).calculateQuotation(any(Quotation.class));
    }

    @Test
    @DisplayName("Should return one response for each calculated product quote")
    void testCreateQuotationReturnsAllProductQuotes() {
        QuotationResult ciResult = mockQuotationResults.get(0);
        QuotationResult crdResult = new QuotationResult(
                ciResult.status(),
                ciResult.creationDate(),
                "QT-2025-CRD",
                ciResult.customers(),
                ciResult.products(),
                ciResult.totals(),
                ciResult.warnings(),
                ciResult.returnResults(),
                ciResult.schedule()
        );
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(List.of(ciResult, crdResult));

        ResponseEntity<List<QuotationResponse>> response =
                quotationController.createQuotation(validQuotationRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(QuotationResponse::getQuotationId)
                .containsExactly("QT-2025-001", "QT-2025-CRD");
    }

    @Test
    @DisplayName("Should map DTO to domain models correctly")
    void testMapDtoToDomain() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        verify(quotationInboundPort).calculateQuotation(argThat(quotation -> 
                quotation.creationDate() != null &&
                quotation.customers() != null &&
                quotation.customers().size() > 0 &&
                quotation.options() != null &&
                quotation.source() != null
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should correctly parse LocalDate to String in domain model")
    void testDateFormatting() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        verify(quotationInboundPort).calculateQuotation(argThat(quotation ->
                quotation.creationDate().equals("2025-02-15")
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==================== NULL HANDLING TESTS ====================

    @Test
    @DisplayName("Should handle null customers gracefully")
    void testHandleNullCustomers() {
        // Arrange
        QuotationRequest requestWithNullCustomers = createQuotationRequestWithNullCustomers();
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(requestWithNullCustomers);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationInboundPort).calculateQuotation(argThat(quotation ->
                quotation.customers() == null || quotation.customers().isEmpty()
        ));
    }

    @Test
    @DisplayName("Should handle null loans in quotation options")
    void testHandleNullLoans() {
        // Arrange
        QuotationRequest requestWithNullLoans = createQuotationRequestWithNullLoans();
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(requestWithNullLoans);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationInboundPort).calculateQuotation(argThat(quotation ->
                quotation.options() == null
                        || quotation.options().loans() == null
                        || quotation.options().loans().isEmpty()
        ));
    }

    @Test
    @DisplayName("Should handle null warranties in loans")
    void testHandleNullWarranties() {
        // Arrange
        QuotationRequest requestWithNullWarranties = createQuotationRequestWithNullWarranties();
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(requestWithNullWarranties);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationInboundPort).calculateQuotation(any(Quotation.class));
    }

    @Test
    @DisplayName("Should handle null beneficiaries")
    void testHandleNullBeneficiaries() {
        // Arrange
        QuotationRequest requestWithNullBeneficiaries = createQuotationRequestWithNullBeneficiaries();
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(requestWithNullBeneficiaries);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationInboundPort).calculateQuotation(argThat(quotation ->
                quotation.customers().values().stream()
                        .allMatch(c -> c.beneficiaries() == null)
        ));
    }

    @Test
    @DisplayName("Should handle null next address")
    void testHandleNullNextAddress() {
        // Arrange
        QuotationRequest requestWithNullAddress = createQuotationRequestWithNullNextAddress();
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(requestWithNullAddress);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationInboundPort).calculateQuotation(argThat(quotation ->
                quotation.customers().values().stream()
                        .allMatch(c -> c.nextAddress() == null)
        ));
    }

    // ==================== NESTED OBJECT MAPPING TESTS ====================

    @Test
    @DisplayName("Should correctly map nested Address objects")
    void testMapNestedAddressObjects() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        verify(quotationInboundPort).calculateQuotation(argThat(quotation ->
                quotation.customers().values().stream()
                        .filter(c -> c.nextAddress() != null)
                        .allMatch(c -> c.nextAddress().address() != null)
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should correctly map beneficiary data")
    void testMapBeneficiaryData() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        verify(quotationInboundPort).calculateQuotation(argThat(quotation ->
                quotation.customers().values().stream()
                        .filter(c -> c.beneficiaries() != null)
                        .allMatch(c -> c.beneficiaries().firstname() != null)
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should correctly map warranty data in loans")
    void testMapWarrantyData() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        verify(quotationInboundPort).calculateQuotation(argThat(quotation ->
                quotation.options() != null &&
                quotation.options().loans() != null &&
                quotation.options().loans().values().stream()
                        .allMatch(loan -> loan.warranties() != null)
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==================== RESPONSE MAPPING TESTS ====================

    @Test
    @DisplayName("Should map domain QuotationResult to response DTO correctly")
    void testMapDomainResultToResponseDto() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        QuotationResponse body = response.getBody().get(0);
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo("SUCCESS");
        assertThat(body.getQuotationId()).isEqualTo("QT-2025-001");
        assertThat(body.getCreationDate()).isEqualTo(LocalDate.parse("2025-02-15"));
        assertThat(body.getTotals()).isNotNull();
        assertThat(body.getCustomers()).isNotEmpty();
        assertThat(body.getSchedule()).singleElement().satisfies(line -> {
            assertThat(line.getIp()).isEqualTo(8.50);
            assertThat(line.getItp()).isEqualTo(6.20);
            assertThat(line.getDos()).isEqualTo(3.10);
            assertThat(line.getPsy()).isEqualTo(2.40);
            assertThat(line.getPe()).isEqualTo(1.80);
        });
    }

    @Test
    @DisplayName("Should map response with totals correctly")
    void testMapResponseWithTotals() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        QuotationResponse body = response.getBody().get(0);
        assertThat(body.getTotals()).isNotNull();
        assertThat(body.getTotals().getMonthly()).isEqualTo(150.0);
        assertThat(body.getTotals().getAnnual()).isEqualTo(1800.0);
        assertThat(body.getTotals().getCurrency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Should map customer quotes with premiums")
    void testMapCustomerQuotesWithPremiums() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        QuotationResponse body = response.getBody().get(0);
        assertThat(body.getCustomers()).isNotEmpty();
        CustomerQuote customerQuote = body.getCustomers().get(0);
        assertThat(customerQuote.getPremium()).isNotNull();
        assertThat(customerQuote.getPremium().getMonthly()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should map products in response")
    void testMapProductsInResponse() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        QuotationResponse body = response.getBody().get(0);
        assertThat(body.getProducts()).isNotNull().isNotEmpty();
        assertThat(body.getProducts().get(0).getCode()).isEqualTo("PROD-001");
    }

    @Test
    @DisplayName("Should handle null totals in response")
    void testHandleNullTotalsInResponse() {
        // Arrange
        QuotationResult resultWithNullTotals = createMockQuotationResultWithNullTotals();
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(List.of(resultWithNullTotals));

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        QuotationResponse body = response.getBody().get(0);
        assertThat(body.getTotals()).isNull();
    }

    @Test
    @DisplayName("Should handle null customers in response")
    void testHandleNullCustomersInResponse() {
        // Arrange
        QuotationResult resultWithNullCustomers = createMockQuotationResultWithNullCustomers();
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(List.of(resultWithNullCustomers));

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        QuotationResponse body = response.getBody().get(0);
        assertThat(body.getCustomers()).isEmpty();
    }

    @Test
    @DisplayName("Should handle loan quotes with covers mapping")
    void testMapLoanQuotesWithCovers() {
        // Arrange
        when(quotationInboundPort.calculateQuotation(any(Quotation.class)))
                .thenReturn(mockQuotationResults);

        // Act
        ResponseEntity<List<QuotationResponse>> response = quotationController.createQuotation(validQuotationRequest);

        // Assert
        QuotationResponse body = response.getBody().get(0);
        assertThat(body.getCustomers()).isNotEmpty();
        List<CustomerLoanQuote> loans = body.getCustomers().get(0).getLoans();
        assertThat(loans).isNotEmpty();
        assertThat(loans.get(0).getCovers()).isNotNull();
    }

    // ==================== HELPER METHODS ====================

    private QuotationRequest createValidQuotationRequest() {
        QuotationRequest request = new QuotationRequest();
        request.setCreationDate(LocalDate.parse("2025-02-15"));

        Map<String, Customer> customers = new HashMap<>();
        Customer customer = new Customer();
        customer.setFirstname("Jean");
        customer.setLastname("Dupont");
        customer.setEmail("jean@example.com");
        customer.setBirthDate(LocalDate.parse("1980-05-15"));
        customer.setCity("Paris");
        customer.setZipCode("75001");

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setFirstname("Marie");
        beneficiary.setLastname("Dupont");
        beneficiary.setBirthDate("2020-03-20");
        beneficiary.setBirthCity("Paris");
        customer.setBeneficiaries(beneficiary);

        Address nextAddr = new Address();
        nextAddr.setAddress("123 Rue de la Paix");
        nextAddr.setCity("Lyon");
        nextAddr.setZipCode("69001");
        nextAddr.setMoveDate(LocalDate.parse("2025-06-01"));
        customer.setNextAddress(nextAddr);

        customers.put("1", customer);
        request.setCustomers(customers);

        QuotationOptions options = new QuotationOptions();
        options.setBank("BankXYZ");
        options.setCouple(false);
        options.setEffectiveDate(LocalDate.parse("2025-03-01"));

        Map<String, Loan> loans = new HashMap<>();
        Loan loan = new Loan();
        loan.setAmount("100000.0");
        loan.setDuration("20");
        loan.setDelayType("MONTHS");
        loan.setDelay("0");
        loan.setRate("2.5");
        loan.setRateType(1);
        loan.setType(1);

        Map<String, Warranty> warranties = new HashMap<>();
        Warranty warranty = new Warranty();
        warranty.setIp(true);
        warranty.setIpp(true);
        warranty.setIptSortieCapital(false);
        warranty.setDrom(true);
        warranty.setCorse(false);
        warranty.setQuotityVie("1");
        warranty.setQuotityNonVie("1");
        warranties.put("1", warranty);
        loan.setWaranties(warranties);

        loans.put("1", loan);
        options.setLoans(loans);
        options.setProjectQualification(1);
        options.setProjectState(7);
        request.setOptions(options);

        request.setReturnResults("true");
        request.setSource("ELOIS");
        request.setPaymentFrequency(12);
        return request;
    }

    private QuotationRequest createQuotationRequestWithNullCustomers() {
        QuotationRequest request = new QuotationRequest();
        request.setCreationDate(LocalDate.parse("2025-02-15"));
        request.setCustomers(null);
        request.setOptions(new QuotationOptions());
        request.setReturnResults("true");
        request.setSource("ELOIS");
        return request;
    }

    private QuotationRequest createQuotationRequestWithNullLoans() {
        QuotationRequest request = validQuotationRequest;
        request.getOptions().setLoans(null);
        return request;
    }

    private QuotationRequest createQuotationRequestWithNullWarranties() {
        QuotationRequest request = validQuotationRequest;
        request.getOptions().getLoans().get("1").setWaranties(null);
        return request;
    }

    private QuotationRequest createQuotationRequestWithNullBeneficiaries() {
        QuotationRequest request = validQuotationRequest;
        request.getCustomers().get("1").setBeneficiaries(null);
        return request;
    }

    private QuotationRequest createQuotationRequestWithNullNextAddress() {
        QuotationRequest request = validQuotationRequest;
        request.getCustomers().get("1").setNextAddress(null);
        return request;
    }

    private QuotationResult createMockQuotationResult() {
        fr.hm.tarificateur.domain.model.QuoteTotals totals = new fr.hm.tarificateur.domain.model.QuoteTotals(150.0, 1800.0, "EUR");

        fr.hm.tarificateur.domain.model.Premium premium = new fr.hm.tarificateur.domain.model.Premium(100.0, 1200.0, "EUR");
        fr.hm.tarificateur.domain.model.CustomerLoanQuote loanQuote = new fr.hm.tarificateur.domain.model.CustomerLoanQuote(
                "LOAN-001",
                "100000.0",
                "2.5",
                75.0,
                new fr.hm.tarificateur.domain.model.Warranty(true, true, false, false, false, false, false, false, "1", "1")
        );

        fr.hm.tarificateur.domain.model.CustomerQuote customerQuote = new fr.hm.tarificateur.domain.model.CustomerQuote(
                "CUST-001",
                "ACCEPTED",
                premium,
                Arrays.asList(loanQuote),
                null
        );

        fr.hm.tarificateur.domain.model.ProductQuote productQuote = new fr.hm.tarificateur.domain.model.ProductQuote(
                "PROD-001",
                "Product Name",
                new fr.hm.tarificateur.domain.model.Premium(50.0, 600.0, "EUR"),
                "ACTIVE"
        );

        return new QuotationResult(
                "SUCCESS",
                "2025-02-15",
                "QT-2025-001",
                Arrays.asList(customerQuote),
                Arrays.asList(productQuote),
                totals,
                null,
                true,
                List.of(new fr.hm.tarificateur.domain.model.ScheduleLine(
                    2025, 91.90, 38.90, 5.99, 12.57, 8.50, 6.20, 3.10, 2.40, 1.80, 171.36, null))
        );
    }

    private QuotationResult createMockQuotationResultWithNullTotals() {
        fr.hm.tarificateur.domain.model.CustomerQuote customerQuote = new fr.hm.tarificateur.domain.model.CustomerQuote(
                "CUST-001",
                "ACCEPTED",
                null,
                null,
                null
        );

        return new QuotationResult(
                "SUCCESS",
                "2025-02-15",
                "QT-2025-001",
                Arrays.asList(customerQuote),
                null,
                null,
                null,
                true
        );
    }

    private QuotationResult createMockQuotationResultWithNullCustomers() {
        fr.hm.tarificateur.domain.model.QuoteTotals totals = new fr.hm.tarificateur.domain.model.QuoteTotals(150.0, 1800.0, "EUR");

        return new QuotationResult(
                "SUCCESS",
                "2025-02-15",
                "QT-2025-001",
                null,
                null,
                totals,
                null,
                true
        );
    }
}
