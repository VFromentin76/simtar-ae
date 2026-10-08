package fr.hm.tarificateur.adapters.inbound.rest;

import fr.hm.tarificateur.adapters.inbound.rest.api.QuotationApi;
import fr.hm.tarificateur.adapters.inbound.rest.dto.Customer;
import fr.hm.tarificateur.adapters.inbound.rest.dto.CustomerLoanQuote;
import fr.hm.tarificateur.adapters.inbound.rest.dto.CustomerQuote;
import fr.hm.tarificateur.adapters.inbound.rest.dto.Loan;
import fr.hm.tarificateur.adapters.inbound.rest.dto.Premium;
import fr.hm.tarificateur.adapters.inbound.rest.dto.ProductQuote;
import fr.hm.tarificateur.adapters.inbound.rest.dto.QuotationRequest;
import fr.hm.tarificateur.adapters.inbound.rest.dto.QuotationResponse;
import fr.hm.tarificateur.adapters.inbound.rest.dto.QuoteTotals;
import fr.hm.tarificateur.adapters.inbound.rest.dto.ScheduleLine;
import fr.hm.tarificateur.adapters.inbound.rest.dto.Warranty;
import fr.hm.tarificateur.domain.model.Address;
import fr.hm.tarificateur.domain.model.Beneficiary;
import fr.hm.tarificateur.domain.model.QuotationResult;
import fr.hm.tarificateur.domain.model.QuotationOptions;
import fr.hm.tarificateur.ports.inbound.QuotationInboundPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class QuotationController implements QuotationApi {

    private final QuotationInboundPort quotationInboundPort;

    public QuotationController(QuotationInboundPort quotationInboundPort) {
        this.quotationInboundPort = quotationInboundPort;
    }

    @Override
    public ResponseEntity<List<QuotationResponse>> createQuotation(QuotationRequest quotationRequest) {
        fr.hm.tarificateur.domain.model.Quotation domainRequest = mapToDomain(quotationRequest);
        List<QuotationResponse> response = quotationInboundPort.calculateQuotation(domainRequest)
            .stream().map(this::mapToResponse).toList();
        return ResponseEntity.ok(response);
    }

    private fr.hm.tarificateur.domain.model.Quotation mapToDomain(QuotationRequest request) {
        if (request == null) return null;

        Map<String, fr.hm.tarificateur.domain.model.Customer> customers = new HashMap<>();
        if (request.getCustomers() != null) {
            for (Map.Entry<String, Customer> entry : request.getCustomers().entrySet()) {
                Customer c = entry.getValue();

                Beneficiary b = null;
                if (c.getBeneficiaries() != null) {
                    b = new Beneficiary(
                            c.getBeneficiaries().getBirthCity(),
                            c.getBeneficiaries().getBirthDate(),
                            c.getBeneficiaries().getFirstname(),
                            c.getBeneficiaries().getLastname(),
                            c.getBeneficiaries().getOrder()
                    );
                }

                Address nextAddr = null;
                if (c.getNextAddress() != null) {
                    nextAddr = new Address(
                            c.getNextAddress().getAddress(),
                            c.getNextAddress().getCity(),
                            c.getNextAddress().getMoveDate() != null ? c.getNextAddress().getMoveDate().toString() : null,
                            c.getNextAddress().getZipCode()
                    );
                }

                Integer beneficiaryClause = c.getBeneficiaryClause();
                Integer beneficiaryType = c.getBeneficiaryType();

                customers.put(entry.getKey(), new fr.hm.tarificateur.domain.model.Customer(
                        c.getPartnerCustomerRef(),
                        c.getAddress(),
                        b,
                        beneficiaryClause,
                        beneficiaryType,
                        c.getCity(),
                        c.getZipCode(),
                        c.getCountry(),
                        c.getCivility(),
                        c.getFirstname(),
                        c.getLastname(),
                        c.getMaidenName(),
                        c.getBirthDate() != null ? c.getBirthDate().toString() : null,
                        c.getBirthZipcode(),
                        c.getBirthCity(),
                        c.getBirthCountry(),
                        c.getFamilySituation(),
                        c.getCellPhone(),
                        c.getHomePhone(),
                        c.getOfficePhone(),
                        c.getEmail(),
                        c.getProfession(),
                        c.getExactProfession(),
                        c.getFranchise(),
                        c.getHandling(),
                        c.getHeight(),
                        c.getBusinessTrip(),
                        c.getIsMainCustomer(),
                        c.getPartTime(),
                        c.getRiskyProfession(),
                        c.getRiskySport(),
                        c.getRiskyProfessionId(),
                        c.getSmoker(),
                        c.getDisclosedOverLemoineLimit(),
                        c.getTravelingAbroad(),
                        c.getModulation(),
                        nextAddr,
                        c.getFees(),
                        c.getBrokerFees(),
                        c.getIsBrokerFeesSpread()
                ));
            }
        }

        QuotationOptions options = null;
        if (request.getOptions() != null) {
            Map<String, fr.hm.tarificateur.domain.model.Loan> loans = new HashMap<>();
            if (request.getOptions().getLoans() != null) {
                for (Map.Entry<String, Loan> loanEntry : request.getOptions().getLoans().entrySet()) {
                    Loan l = loanEntry.getValue();

                    Map<String, fr.hm.tarificateur.domain.model.Warranty> warranties = new HashMap<>();
                    if (l.getWaranties() != null) {
                        for (Map.Entry<String, Warranty> wEntry : l.getWaranties().entrySet()) {
                            Warranty w = wEntry.getValue();
                            warranties.put(wEntry.getKey(), new fr.hm.tarificateur.domain.model.Warranty(
                                    w.getIp(),
                                    w.getIpp(),
                                    w.getIpt(),
                                    w.getItp(),
                                    w.getItt(),
                                    w.getDos(),
                                    w.getPsy(),
                                    w.getPe(),
                                    w.getQuotityVie(),
                                    w.getQuotityNonVie(),
                                    w.getMno(),
                                    w.getMnoOption() == null ? null : w.getMnoOption().getValue(),
                                    w.getDrom(),
                                    w.getCorse(),
                                    w.getIptSortieCapital(),
                                    w.getAgeFinCouverture() == null ? null : w.getAgeFinCouverture().getValue(),
                                    w.getExonerationCotisations(),
                                    w.getIppOption() == null ? null : w.getIppOption().getValue()
                            ));
                        }
                    }

                    loans.put(loanEntry.getKey(), new fr.hm.tarificateur.domain.model.Loan(
                            l.getAmount(),
                            l.getDuration(),
                            l.getDelayType(),
                            l.getDelay(),
                            l.getPartnerLoanRef(),
                            l.getRate(),
                            l.getRateType(),
                            l.getType(),
                            warranties
                    ));
                }
            }

            options = new QuotationOptions(
                    request.getOptions().getBank(),
                    request.getOptions().getCouple(),
                    request.getOptions().getEffectiveDate() != null ? request.getOptions().getEffectiveDate().toString() : null,
                    loans,
                    request.getOptions().getProjectQualification(),
                    request.getOptions().getProjectState()
            );
        }

        return new fr.hm.tarificateur.domain.model.Quotation(
                request.getCreationDate() != null ? request.getCreationDate().toString() : null,
                customers,
                options,
                request.getReturnResults(),
                request.getSource(),
                request.getPaymentFrequency()
        );
    }

    private QuotationResponse mapToResponse(QuotationResult domain) {
        if (domain == null) return null;

        QuotationResponse response = new QuotationResponse();
        response.setStatus(domain.status());
        if (domain.creationDate() != null) {
            response.setCreationDate(java.time.LocalDate.parse(domain.creationDate()));
        }
        response.setQuotationId(domain.quotationId());
        response.setReturnResults(domain.returnResults());
        response.setWarnings(domain.warnings());
        if (domain.schedule() != null) {
            response.setSchedule(domain.schedule().stream().map(line -> {
                ScheduleLine dto = new ScheduleLine();
                dto.setYear(line.year());
                dto.setDcPtia(line.dcPtia());
                dto.setItt(line.itt());
                dto.setIpt(line.ipt());
                dto.setIpp(line.ipp());
                dto.setIp(line.ip());
                dto.setItp(line.itp());
                dto.setDos(line.dos());
                dto.setPsy(line.psy());
                dto.setPe(line.pe());
                dto.setTotal(line.total());
                dto.setComment(line.comment());
                return dto;
            }).collect(Collectors.toList()));
        }

        if (domain.totals() != null) {
            QuoteTotals totals = new QuoteTotals();
            totals.setMonthly(domain.totals().monthly());
            totals.setAnnual(domain.totals().annual());
            totals.setCurrency(domain.totals().currency());
            response.setTotals(totals);
        }

        if (domain.customers() != null) {
            List<CustomerQuote> custQuotes = domain.customers().stream().map(c -> {
                CustomerQuote cq = new CustomerQuote();
                cq.setCustomerRef(c.customerRef());
                cq.setStatus(c.status());
                cq.setWarnings(c.warnings());

                if (c.premium() != null) {
                    Premium p = new Premium();
                    p.setMonthly(c.premium().monthly());
                    p.setAnnual(c.premium().annual());
                    p.setCurrency(c.premium().currency());
                    cq.setPremium(p);
                }

                if (c.loans() != null) {
                    List<CustomerLoanQuote> loanList = c.loans().stream().map(l -> {
                        CustomerLoanQuote clq = new CustomerLoanQuote();
                        clq.setLoanId(l.loanId());
                        clq.setAmount(l.amount());
                        clq.setRate(l.rate());
                        clq.setPremium(l.premium());
                        if (l.covers() != null) {
                            Warranty w = new Warranty();
                            w.setIp(l.covers().ip());
                            w.setIpp(l.covers().ipp());
                            w.setIpt(l.covers().ipt());
                            w.setItp(l.covers().itp());
                            w.setItt(l.covers().itt());
                            w.setDos(l.covers().dos());
                            w.setPsy(l.covers().psy());
                            w.setPe(l.covers().pe());
                            w.setQuotityVie(l.covers().quotityVie());
                            w.setQuotityNonVie(l.covers().quotityNonVie());
                            clq.setCovers(w);
                        }
                        return clq;
                    }).collect(Collectors.toList());
                    cq.setLoans(loanList);
                }
                return cq;
            }).collect(Collectors.toList());
            response.setCustomers(custQuotes);
        }

        if (domain.products() != null) {
            List<ProductQuote> prodQuotes = domain.products().stream().map(p -> {
                ProductQuote pq = new ProductQuote();
                pq.setCode(p.code());
                pq.setName(p.name());
                pq.setStatus(p.status());
                if (p.premium() != null) {
                    Premium prem = new Premium();
                    prem.setMonthly(p.premium().monthly());
                    prem.setAnnual(p.premium().annual());
                    prem.setCurrency(p.premium().currency());
                    pq.setPremium(prem);
                }
                return pq;
            }).collect(Collectors.toList());
            response.setProducts(prodQuotes);
        }

        return response;
    }

}
