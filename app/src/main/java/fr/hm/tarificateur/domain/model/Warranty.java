package fr.hm.tarificateur.domain.model;

public record Warranty(
        Boolean ip,
        Boolean ipp,
        Boolean ipt,
        Boolean itp,
        Boolean itt,
        Boolean dos,
        Boolean psy,
        Boolean pe,
        String quotityVie,
        String quotityNonVie,
        Boolean mno,
        String mnoOption,
        Boolean drom,
        Boolean corse,
        Boolean iptSortieCapital,
        String ageFinCouverture,
        Boolean exonerationCotisations,
        String ippOption
) {
    public Warranty(
        Boolean ip, Boolean ipp, Boolean ipt, Boolean itp, Boolean itt,
        Boolean dos, Boolean psy, Boolean pe, String quotityVie, String quotityNonVie) {
        this(ip, ipp, ipt, itp, itt, dos, psy, pe, quotityVie, quotityNonVie,
            null, null, null, null, null, null, null, null);
    }

    public Warranty(
        Boolean ip, Boolean ipp, Boolean ipt, Boolean itp, Boolean itt,
        Boolean dos, Boolean psy, Boolean pe, String quotityVie, String quotityNonVie,
        Boolean mno, String mnoOption) {
        this(ip, ipp, ipt, itp, itt, dos, psy, pe, quotityVie, quotityNonVie,
            mno, mnoOption, null, null, null, null, null, null);
    }

    public Warranty(
        Boolean ip, Boolean ipp, Boolean ipt, Boolean itp, Boolean itt,
        Boolean dos, Boolean psy, Boolean pe, String quotityVie, String quotityNonVie,
        Boolean mno, String mnoOption, Boolean drom, Boolean corse) {
        this(ip, ipp, ipt, itp, itt, dos, psy, pe, quotityVie, quotityNonVie,
            mno, mnoOption, drom, corse, null, null, null, null);
    }

    public Warranty(
        Boolean ip, Boolean ipp, Boolean ipt, Boolean itp, Boolean itt,
        Boolean dos, Boolean psy, Boolean pe, String quotityVie, String quotityNonVie,
        Boolean mno, String mnoOption, Boolean drom, Boolean corse, Boolean iptSortieCapital) {
        this(ip, ipp, ipt, itp, itt, dos, psy, pe, quotityVie, quotityNonVie,
            mno, mnoOption, drom, corse, iptSortieCapital, null, null, null);
    }

    public Warranty(
        Boolean ip, Boolean ipp, Boolean ipt, Boolean itp, Boolean itt,
        Boolean dos, Boolean psy, Boolean pe, String quotityVie, String quotityNonVie,
        Boolean mno, String mnoOption, Boolean drom, Boolean corse, Boolean iptSortieCapital,
        String ageFinCouverture) {
        this(ip, ipp, ipt, itp, itt, dos, psy, pe, quotityVie, quotityNonVie,
            mno, mnoOption, drom, corse, iptSortieCapital, ageFinCouverture, null, null);
    }

    public Warranty(
        Boolean ip, Boolean ipp, Boolean ipt, Boolean itp, Boolean itt,
        Boolean dos, Boolean psy, Boolean pe, String quotityVie, String quotityNonVie,
        Boolean mno, String mnoOption, Boolean drom, Boolean corse, Boolean iptSortieCapital,
        String ageFinCouverture, Boolean exonerationCotisations) {
        this(ip, ipp, ipt, itp, itt, dos, psy, pe, quotityVie, quotityNonVie,
            mno, mnoOption, drom, corse, iptSortieCapital, ageFinCouverture,
            exonerationCotisations, null);
    }
}
