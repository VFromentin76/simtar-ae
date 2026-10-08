package fr.hm.tarificateur.domain.model;

public record ScheduleLine(
        Integer year,
        Double dcPtia,
        Double itt,
        Double ipt,
        Double ipp,
        Double ip,
        Double itp,
        Double dos,
        Double psy,
        Double pe,
        Double total,
        String comment
) {}
