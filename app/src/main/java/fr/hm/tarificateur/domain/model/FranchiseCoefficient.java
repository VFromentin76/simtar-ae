package fr.hm.tarificateur.domain.model;

import java.util.List;

public record FranchiseCoefficient(
        Reference franchise,
        Boolean regimeLemoine,
        Double coefficient,
        List<Reference> territorialiteExclusions,
        Boolean exclusionLemoine
) {
    public FranchiseCoefficient(Reference franchise, Boolean regimeLemoine, Double coefficient) {
        this(franchise, regimeLemoine, coefficient, null, null);
    }
}
