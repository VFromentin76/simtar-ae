package fr.hm.tarificateur.domain.model;

public record TypePretCourbeDeformationCrd(
    Reference typePret,
    Boolean booCourbeCrdApplicable
) {
}
