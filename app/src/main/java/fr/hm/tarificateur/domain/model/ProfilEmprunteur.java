package fr.hm.tarificateur.domain.model;

/**
 * Profil de l'emprunteur - regroupe tous les attributs personnels et statuts
 * relatifs à l'emprunteur (couple, lemoine, fumeur, âge d'adhésion).
 *
 * @param couple              Indique si l'emprunteur est marié/PACS
 * @param profilLemoine       Indique si l'emprunteur bénéficie du régime Lemoine
 * @param profilFumeur        Indique si l'emprunteur est fumeur
 * @param ageAdhesion         Âge de l'emprunteur à la date d'adhésion
 */
public record ProfilEmprunteur(
    boolean couple,
    boolean profilLemoine,
    boolean profilFumeur,
    Integer ageAdhesion
) {}

