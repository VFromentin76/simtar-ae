package fr.hm.tarificateur.domain.model;

public record CourbeDeformationCrd(
        Integer anciennetePretAnnees,
        Integer dureePretAnnees,
        Double coefficient
) {}

