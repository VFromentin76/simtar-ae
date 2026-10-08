package fr.hm.tarificateur.ports.inbound;

import fr.hm.tarificateur.domain.model.Quotation;
import fr.hm.tarificateur.domain.model.QuotationResult;
import java.util.List;

public interface QuotationInboundPort {
    List<QuotationResult> calculateQuotation(Quotation quotation);
}
