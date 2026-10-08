package fr.hm.tarificateur.domain.model;

public record ExclusionCategoriePro(
    Reference categorieProfessionnelle,
    Boolean exclueLemoine,
    Boolean exclueGarantieNonVie
) {
}
