package fr.hm.tarificateur.adapters.outbound.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.hm.tarificateur.domain.exception.ProductConfigurationNotFoundException;
import fr.hm.tarificateur.domain.exception.ProductConfigurationRetrievalException;
import fr.hm.tarificateur.domain.model.CategorieProClasseRisqueMapping;
import fr.hm.tarificateur.domain.model.ClasseRisqueCoefficient;
import fr.hm.tarificateur.domain.model.CoefficientAerasCi;
import fr.hm.tarificateur.domain.model.CoefficientAerasCrd;
import fr.hm.tarificateur.domain.model.CoefficientPassageFumeurCi;
import fr.hm.tarificateur.domain.model.CoefficientPassageFumeurCrd;
import fr.hm.tarificateur.domain.model.CoefficientPerimetreLemoineCi;
import fr.hm.tarificateur.domain.model.CoefficientPerimetreLemoineCrd;
import fr.hm.tarificateur.domain.model.ConfigGarantie;
import fr.hm.tarificateur.domain.model.CourbeDeformationCrd;
import fr.hm.tarificateur.domain.model.CoverageEndCoefficient;
import fr.hm.tarificateur.domain.model.CriterePro;
import fr.hm.tarificateur.domain.model.Dependance;
import fr.hm.tarificateur.domain.model.DetailConfig;
import fr.hm.tarificateur.domain.model.ExclusionCategoriePro;
import fr.hm.tarificateur.domain.model.ExonerationCotisations;
import fr.hm.tarificateur.domain.model.ExonerationCotisationsCoefficient;
import fr.hm.tarificateur.domain.model.FranchiseCoefficient;
import fr.hm.tarificateur.domain.model.ObjetPretCoefficient;
import fr.hm.tarificateur.domain.model.ObjetPretEligibilite;
import fr.hm.tarificateur.domain.model.OptionCoefficient;
import fr.hm.tarificateur.domain.model.PrimePureCi;
import fr.hm.tarificateur.domain.model.PrimePureCrd;
import fr.hm.tarificateur.domain.model.ProductConfiguration;
import fr.hm.tarificateur.domain.model.ProfessionClasseRisqueMapping;
import fr.hm.tarificateur.domain.model.Reference;
import fr.hm.tarificateur.domain.model.TerritorialiteCoefficient;
import fr.hm.tarificateur.domain.model.TypePretCoefficient;
import fr.hm.tarificateur.domain.model.TypePretCourbeDeformationCrd;
import fr.hm.tarificateur.domain.model.TypePretEligibilite;
import fr.hm.tarificateur.ports.outbound.ProductConfiguratorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ProductConfiguratorClientAdapter implements ProductConfiguratorPort {

    private static final Logger logger = LoggerFactory.getLogger(ProductConfiguratorClientAdapter.class);

    private final RestClient sopranoRestClient;
    private final Supplier<String> sopranoTokenSupplier;

    public ProductConfiguratorClientAdapter(
        @Qualifier("productConfiguratorRestClient") RestClient sopranoRestClient,
        @Qualifier("productConfiguratorTokenSupplier") Supplier<String> sopranoTokenSupplier
    ) {
        this.sopranoRestClient = sopranoRestClient;
        this.sopranoTokenSupplier = sopranoTokenSupplier;
    }

    @Override
    public Map<String, ProductConfiguration> getConfigurations() {
        Map<String, ProductConfiguration> configurations = new HashMap<>();
        fetchProductConfigurations().stream()
            .map(ProductConfigV2Dto::toDomain)
            .filter(configuration -> configuration.codeProduit() != null)
            .forEach(configuration -> configurations.put(configuration.codeProduit(), configuration));
        if (configurations.isEmpty()) {
            throw new ProductConfigurationNotFoundException();
        }
        return configurations;
    }

    private List<ProductConfigV2Dto> fetchProductConfigurations() {
        try {
            ProductConfigurationResponseDto response = executeRequest();
            if (response == null || response.configurations().isEmpty()) {
                throw new ProductConfigurationNotFoundException();
            }
            return response.configurations();
        } catch (HttpClientErrorException.NotFound ex) {
            logger.warn("Product configuration not found", ex);
            throw new ProductConfigurationNotFoundException();
        } catch (ResourceAccessException ex) {
            throw mapResourceAccessException(ex);
        } catch (RestClientException ex) {
            logger.error("Failed to retrieve product configuration", ex);
            throw new ProductConfigurationRetrievalException(ex);
        }
    }

    private ProductConfigurationResponseDto executeRequest() {
        logger.info("Fetching active PROTEMP product configurations");
        RestClient.RequestHeadersSpec<?> request = sopranoRestClient.get()
            .uri("/v2/assurance-pret/versions-produit/offres/PROTEMP/actives");
        String token = sopranoTokenSupplier.get();
        if (token != null && !token.isBlank()) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request.accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(ProductConfigurationResponseDto.class);
    }

    private ProductConfigurationRetrievalException mapResourceAccessException(ResourceAccessException exception) {
        if (isReadTimeout(exception)) {
            logger.error("Timed out while retrieving product configuration", exception);
            return new ProductConfigurationRetrievalException(
                "Le service configurateur produit n'a pas repondu dans le delai imparti",
                exception
            );
        }
        logger.error("Resource access failure while retrieving product configuration", exception);
        return new ProductConfigurationRetrievalException(exception);
    }

    private boolean isReadTimeout(ResourceAccessException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof SocketTimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static Reference toReference(ReferenceDto reference) {
        return reference == null ? null : new Reference(reference.code(), reference.libelle());
    }

    private static Boolean toBooleanRegimeLemoine(String regimeLemoine) {
        return "LEMOINE".equals(regimeLemoine);
    }

    private static <T> List<T> emptyIfNull(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private static <T> T firstOrNull(List<T> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private static List<Reference> mapReferences(List<ReferenceDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(ProductConfiguratorClientAdapter::toReference)
            .toList();
    }

    public record ProductConfigurationResponseDto(
        @JsonProperty("CI") ProductConfigV2Dto ci,
        @JsonProperty("CRD") ProductConfigV2Dto crd
    ) {
        List<ProductConfigV2Dto> configurations() {
            return Stream.of(ci, crd).filter(Objects::nonNull).toList();
        }
    }

    public record ProductConfigV2Dto(
        String codeProduit,
        String code,
        String libelle,
        String dateEffetDebut,
        String dateEffetFin,
        String statut,
        String modeCalcul,
        String eligibiliteLemoine,
        DetailVersionV2Dto detailVersion,
        List<GarantieV2Dto> garanties,
        List<TypePretEligibiliteDto> typePretEligibilites,
        List<ObjetPretEligibiliteDto> objetPretEligibilites,
        List<TypePretCoefficientDto> typePretCoefficients,
        List<ObjetPretCoefficientDto> objetPretCoefficients,
        List<ClasseRisqueCoefficientDto> classeRisqueCoefficients,
        List<TerritorialiteCoefficientDto> territorialiteCoefficientsNonVie,
        List<CourbeDeformationCrdDto> courbeDeformationCrd,
        List<TypePretCourbeDeformationCrdDto> typePretCourbesDeformationCrd,
        List<CoefficientPassageFumeurCiDto> coefficientsPassageFumeurCi,
        List<CoefficientPassageFumeurCrdDto> coefficientsPassageFumeurCrd,
        List<CoefficientPerimetreLemoineCiDto> coefficientsPerimetreLemoineCi,
        List<CoefficientPerimetreLemoineCrdDto> coefficientsPerimetreLemoineCrd,
        List<CoefficientAerasCiDto> coefficientsAerasRefusCi,
        List<CoefficientAerasCiDto> coefficientsAerasExclusionCi,
        List<CoefficientAerasCrdDto> coefficientsAerasRefusCrd,
        List<CoefficientAerasCrdDto> coefficientsAerasExclusionCrd,
        List<CoverageEndCoefficientDto> coefficientsFinCouvertureAtCi,
        List<CategorieProClasseRisqueMappingDto> mappingsCategoriePro,
        List<ExclusionCategorieProDto> exclusionsCategoriePro,
        List<ProfessionClasseRisqueMappingDto> mappingsProfession,
        List<Map<String, Object>> racEligibilites,
        List<Map<String, Object>> racCoefficients
    ) {
        public ProductConfiguration toDomain() {
            return new ProductConfiguration(
                codeProduit != null ? codeProduit : code,
                code,
                libelle != null ? libelle : codeProduit,
                parseLocalDate(dateEffetDebut),
                parseLocalDate(dateEffetFin),
                statut,
                modeCalcul != null ? modeCalcul : "CI",
                eligibiliteLemoine,
                mapDetail(detailVersion),
                mapGaranties(garanties),
                mapTypePretEligibilites(typePretEligibilites),
                mapObjetPretEligibilites(objetPretEligibilites),
                mapTypePretCoefficients(typePretCoefficients),
                mapObjetPretCoefficients(objetPretCoefficients),
                mapClasseRisqueCoefficients(classeRisqueCoefficients),
                mapCourbeDeformationCrd(courbeDeformationCrd),
                mapCoefficientsPassageFumeurCi(coefficientsPassageFumeurCi),
                mapCoefficientsPassageFumeurCrd(coefficientsPassageFumeurCrd),
                mapMappingsCategoriePro(mappingsCategoriePro),
                mapMappingsProfession(mappingsProfession),
                mapCoverageEndCoefficients(coefficientsFinCouvertureAtCi),
                mapTerritorialiteCoefficients(territorialiteCoefficientsNonVie),
                mapCoefficientsPerimetreLemoineCi(coefficientsPerimetreLemoineCi),
                mapTypePretCourbesDeformationCrd(typePretCourbesDeformationCrd),
                mapCoefficientsPerimetreLemoineCrd(coefficientsPerimetreLemoineCrd),
                mapCoefficientsAerasCi(coefficientsAerasRefusCi),
                mapCoefficientsAerasCi(coefficientsAerasExclusionCi),
                mapCoefficientsAerasCrd(coefficientsAerasRefusCrd),
                mapCoefficientsAerasCrd(coefficientsAerasExclusionCrd),
                mapExclusionsCategoriePro(exclusionsCategoriePro),
                emptyIfNull(racEligibilites),
                emptyIfNull(racCoefficients)
            );
        }

        private static LocalDate parseLocalDate(String value) {
            return value == null || value.isBlank() ? null : LocalDate.parse(value);
        }
    }

    private static DetailConfig mapDetail(DetailVersionV2Dto detail) {
        if (detail == null) {
            return null;
        }
        return new DetailConfig(
            detail.typeContrat(), detail.associationSouscriptrice(),
            toReference(firstOrNull(detail.territorialitesAdherent())),
            toReference(firstOrNull(detail.territorialitesBienFinance())),
            toReference(firstOrNull(detail.territorialitesPrestations())),
            detail.echeanceAnniversaireContrat(), detail.echeanceAnniversaireAdhesion(),
            detail.booIndexationGarantiesPrimes(), toReference(firstOrNull(detail.modesFractionnement())),
            detail.tauxTaxEnsembleGaranties(), detail.tauxChargementFraisGestion(),
            detail.tauxChargementFraisAcquisition(), detail.tauxChargementFraisAssureur(),
            detail.fraisAssociationEur(), detail.fraisDossierEur(),
            detail.coefficientExonerationCotisationsEnsembleGaranties(),
            detail.seuilCoefficientPassageGrosCapital(), detail.coefficientPassageGrosCapital(),
            detail.coefficientPassageCouple(), detail.lemoineSeuilAgeTermePret(),
            detail.lemoineSeuilCapitalAssureEur(), detail.plafondCapitalGarantiesMinEur(),
            detail.plafondCapitalGarantiesMaxEur(),
            mapReferences(detail.territorialitesAdherent()),
            mapReferences(detail.territorialitesBienFinance()),
            mapReferences(detail.territorialitesPrestations()),
            mapReferences(detail.modesFractionnement())
        );
    }

    private static List<ConfigGarantie> mapGaranties(List<GarantieV2Dto> garanties) {
        return emptyIfNull(garanties).stream()
            .filter(Objects::nonNull)
            .map(ProductConfiguratorClientAdapter::mapGarantie)
            .toList();
    }

    private static ConfigGarantie mapGarantie(GarantieV2Dto garantie) {
        return new ConfigGarantie(
            toReference(garantie.garantie()), garantie.booObligatoire(), garantie.booGafChoisie(),
            garantie.ageAdhesionMin(), garantie.ageAdhesionMax(), garantie.ageFinCouvertureVie(),
            toReference(garantie.territorialite()), garantie.montantMensuelMaxIndemnisableEur(),
            garantie.plafondCapitalMinEur(), garantie.plafondCapitalMaxEur(),
            mapDependances(garantie.dependances()), mapFranchiseCoefficients(garantie.franchises()),
            mapOptions(garantie.options()), mapPrimesPuresCi(garantie.primesPuresCi()),
            mapPrimesPuresCrd(garantie.primesPuresCrd()),
            mapExonerationCotisations(garantie.exonerationCotisations())
        );
    }

    private static List<Dependance> mapDependances(List<DependanceDto> dependances) {
        return emptyIfNull(dependances).stream()
            .filter(Objects::nonNull)
            .map(value -> new Dependance(toReference(value.garantieRequise())))
            .toList();
    }

    private static List<FranchiseCoefficient> mapFranchiseCoefficients(
        List<FranchiseCoefficientDto> franchises
    ) {
        return emptyIfNull(franchises).stream()
            .filter(Objects::nonNull)
            .flatMap(ProductConfiguratorClientAdapter::mapFranchiseCoefficient)
            .toList();
    }

    private static Stream<FranchiseCoefficient> mapFranchiseCoefficient(FranchiseCoefficientDto franchise) {
        if (franchise.coefficients() == null || franchise.coefficients().isEmpty()) {
            return Stream.of(new FranchiseCoefficient(
                toReference(franchise.franchise()),
                toBooleanRegimeLemoine(franchise.regimeLemoine()),
                franchise.coefficient(),
                franchise.territorialiteExclusions() == null
                    ? null : mapReferences(franchise.territorialiteExclusions()),
                franchise.exclusionLemoine()
            ));
        }
        return franchise.coefficients().stream()
            .filter(Objects::nonNull)
            .map(value -> new FranchiseCoefficient(
                toReference(franchise.franchise()),
                toBooleanRegimeLemoine(value.regimeLemoine()),
                value.coefficient(),
                franchise.territorialiteExclusions() == null
                    ? null : mapReferences(franchise.territorialiteExclusions()),
                franchise.exclusionLemoine()
            ));
    }

    private static List<OptionCoefficient> mapOptions(List<OptionCoefficientDto> options) {
        return emptyIfNull(options).stream()
            .filter(Objects::nonNull)
            .map(value -> new OptionCoefficient(
                toReference(value.option()),
                toBooleanRegimeLemoine(value.regimeLemoine()),
                value.coefficient()
            ))
            .toList();
    }

    private static List<PrimePureCi> mapPrimesPuresCi(List<CoefficientPrimePureCiDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new PrimePureCi(
                value.ageAdhesion(), value.dureePretAnnees(), value.coefficient()))
            .toList();
    }

    private static List<PrimePureCrd> mapPrimesPuresCrd(List<CoefficientPrimePureCrdDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new PrimePureCrd(value.ageAtteint(), value.coefficient()))
            .toList();
    }

    private static ExonerationCotisations mapExonerationCotisations(ExonerationCotisationsDto value) {
        if (value == null) {
            return null;
        }
        List<ExonerationCotisationsCoefficient> coefficients = emptyIfNull(value.coefficients()).stream()
            .filter(Objects::nonNull)
            .map(coefficient -> new ExonerationCotisationsCoefficient(
                toBooleanRegimeLemoine(coefficient.regimeLemoine()), coefficient.coefficient()))
            .toList();
        return new ExonerationCotisations(value.exclueLemoine(), coefficients);
    }

    private static List<TypePretEligibilite> mapTypePretEligibilites(List<TypePretEligibiliteDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new TypePretEligibilite(
                toReference(value.typePret()),
                toBooleanRegimeLemoine(value.regimeLemoine()),
                value.booEligible()
            ))
            .toList();
    }

    private static List<ObjetPretEligibilite> mapObjetPretEligibilites(List<ObjetPretEligibiliteDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new ObjetPretEligibilite(
                toReference(value.objetPret()),
                toBooleanRegimeLemoine(value.regimeLemoine()),
                value.booEligible()
            ))
            .toList();
    }

    private static List<TypePretCoefficient> mapTypePretCoefficients(List<TypePretCoefficientDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new TypePretCoefficient(
                toReference(value.typePret()), value.branche(),
                toBooleanRegimeLemoine(value.regimeLemoine()), value.coefficient()
            ))
            .toList();
    }

    private static List<ObjetPretCoefficient> mapObjetPretCoefficients(List<ObjetPretCoefficientDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new ObjetPretCoefficient(
                toReference(value.objetPret()),
                toBooleanRegimeLemoine(value.regimeLemoine()),
                value.coefficient()
            ))
            .toList();
    }

    private static List<ClasseRisqueCoefficient> mapClasseRisqueCoefficients(
        List<ClasseRisqueCoefficientDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new ClasseRisqueCoefficient(
                toReference(value.classeRisque()), value.branche(),
                toBooleanRegimeLemoine(value.regimeLemoine()), value.coeffPassage()
            ))
            .toList();
    }

    private static List<CourbeDeformationCrd> mapCourbeDeformationCrd(
        List<CourbeDeformationCrdDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CourbeDeformationCrd(
                value.anciennetePretAnnees(), value.dureePretAnnees(), value.coefficient()))
            .toList();
    }

    private static List<TypePretCourbeDeformationCrd> mapTypePretCourbesDeformationCrd(
        List<TypePretCourbeDeformationCrdDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new TypePretCourbeDeformationCrd(
                toReference(value.typePret()), value.booCourbeCrdApplicable()))
            .toList();
    }

    private static List<CoefficientPassageFumeurCi> mapCoefficientsPassageFumeurCi(
        List<CoefficientPassageFumeurCiDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CoefficientPassageFumeurCi(
                value.ageAdhesion(), value.branche(), value.coefficient()))
            .toList();
    }

    private static List<CoefficientPassageFumeurCrd> mapCoefficientsPassageFumeurCrd(
        List<CoefficientPassageFumeurCrdDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CoefficientPassageFumeurCrd(
                value.ageAtteint(), value.branche(), value.coefficient()))
            .toList();
    }

    private static List<CoefficientPerimetreLemoineCi> mapCoefficientsPerimetreLemoineCi(
        List<CoefficientPerimetreLemoineCiDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CoefficientPerimetreLemoineCi(
                value.ageAdhesion(), value.branche(), value.coefficient()))
            .toList();
    }

    private static List<CoefficientPerimetreLemoineCrd> mapCoefficientsPerimetreLemoineCrd(
        List<CoefficientPerimetreLemoineCrdDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CoefficientPerimetreLemoineCrd(
                value.ageAdhesion(), value.branche(), value.coefficient()))
            .toList();
    }

    private static List<CoefficientAerasCi> mapCoefficientsAerasCi(List<CoefficientAerasCiDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CoefficientAerasCi(
                value.ageAdhesion(), value.dureePretAnnees(), value.coefficient()))
            .toList();
    }

    private static List<CoefficientAerasCrd> mapCoefficientsAerasCrd(List<CoefficientAerasCrdDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CoefficientAerasCrd(value.ageAtteint(), value.coefficient()))
            .toList();
    }

    private static List<CoverageEndCoefficient> mapCoverageEndCoefficients(
        List<CoverageEndCoefficientDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CoverageEndCoefficient(
                value.ageAdhesion(), parseCoverageEndAge(value.ageFinCouverture()), value.coefficient()))
            .toList();
    }

    private static Integer parseCoverageEndAge(ReferenceDto ageFinCouverture) {
        if (ageFinCouverture == null || ageFinCouverture.code() == null) {
            return null;
        }
        String digits = ageFinCouverture.code().replaceAll("\\D+", "");
        return digits.isEmpty() ? null : Integer.valueOf(digits);
    }

    private static List<TerritorialiteCoefficient> mapTerritorialiteCoefficients(
        List<TerritorialiteCoefficientDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new TerritorialiteCoefficient(
                toReference(value.territorialite()), value.coefficient()))
            .toList();
    }

    private static List<CategorieProClasseRisqueMapping> mapMappingsCategoriePro(
        List<CategorieProClasseRisqueMappingDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CategorieProClasseRisqueMapping(
                toReference(value.categorieProfessionnelle()),
                toReference(value.classeRisqueDC()),
                toReference(value.classeRisqueAT()),
                mapCriteres(value.criteres())
            ))
            .toList();
    }

    private static List<CriterePro> mapCriteres(List<CriteresProDto> values) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new CriterePro(
                toReference(value.critereProfessionnel()), value.valeurAttendue()))
            .toList();
    }

    private static List<ProfessionClasseRisqueMapping> mapMappingsProfession(
        List<ProfessionClasseRisqueMappingDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new ProfessionClasseRisqueMapping(
                toReference(value.profession()), value.branche(), toReference(value.classeRisque())))
            .toList();
    }

    private static List<ExclusionCategoriePro> mapExclusionsCategoriePro(
        List<ExclusionCategorieProDto> values
    ) {
        return emptyIfNull(values).stream()
            .filter(Objects::nonNull)
            .map(value -> new ExclusionCategoriePro(
                toReference(value.categorieProfessionnelle()),
                value.exclueLemoine(),
                value.exclueGarantieNonVie()
            ))
            .toList();
    }

    public record DetailVersionV2Dto(
        String typeContrat,
        String associationSouscriptrice,
        List<ReferenceDto> territorialitesAdherent,
        List<ReferenceDto> territorialitesBienFinance,
        List<ReferenceDto> territorialitesPrestations,
        String echeanceAnniversaireContrat,
        String echeanceAnniversaireAdhesion,
        Boolean booIndexationGarantiesPrimes,
        List<ReferenceDto> modesFractionnement,
        Double tauxTaxEnsembleGaranties,
        Double tauxChargementFraisGestion,
        Double tauxChargementFraisAcquisition,
        Double tauxChargementFraisAssureur,
        Double fraisAssociationEur,
        Double fraisDossierEur,
        Double coefficientExonerationCotisationsEnsembleGaranties,
        Double seuilCoefficientPassageGrosCapital,
        Double coefficientPassageGrosCapital,
        Double coefficientPassageCouple,
        Integer lemoineSeuilAgeTermePret,
        Double lemoineSeuilCapitalAssureEur,
        Double plafondCapitalGarantiesMinEur,
        Double plafondCapitalGarantiesMaxEur
    ) {
    }

    public record GarantieV2Dto(
        ReferenceDto garantie,
        Boolean booObligatoire,
        Boolean booGafChoisie,
        Integer ageAdhesionMin,
        Integer ageAdhesionMax,
        Integer ageFinCouvertureVie,
        ReferenceDto territorialite,
        Double montantMensuelMaxIndemnisableEur,
        Double plafondCapitalMinEur,
        Double plafondCapitalMaxEur,
        List<DependanceDto> dependances,
        List<FranchiseCoefficientDto> franchises,
        List<OptionCoefficientDto> options,
        List<CoefficientPrimePureCiDto> primesPuresCi,
        List<CoefficientPrimePureCrdDto> primesPuresCrd,
        ExonerationCotisationsDto exonerationCotisations
    ) {
    }

    public record ReferenceDto(String code, String libelle) {
    }

    public record DependanceDto(ReferenceDto garantieRequise) {
    }

    public record FranchiseCoefficientDto(
        ReferenceDto franchise,
        String regimeLemoine,
        Double coefficient,
        List<FranchiseCoefficientValueDto> coefficients,
        List<ReferenceDto> territorialiteExclusions,
        Boolean exclusionLemoine
    ) {
        public FranchiseCoefficientDto(
            ReferenceDto franchise, String regimeLemoine, Double coefficient,
            List<FranchiseCoefficientValueDto> coefficients
        ) {
            this(franchise, regimeLemoine, coefficient, coefficients, null, null);
        }

        public FranchiseCoefficientDto(ReferenceDto franchise, String regimeLemoine, Double coefficient) {
            this(franchise, regimeLemoine, coefficient, Collections.emptyList(), null, null);
        }
    }

    public record FranchiseCoefficientValueDto(String regimeLemoine, Double coefficient) {
    }

    public record OptionCoefficientDto(
        ReferenceDto option,
        String regimeLemoine,
        Double coefficient
    ) {
    }

    public record CoefficientPrimePureCiDto(
        Integer ageAdhesion,
        Integer dureePretAnnees,
        Double coefficient
    ) {
    }

    public record CoefficientPrimePureCrdDto(Integer ageAtteint, Double coefficient) {
    }

    public record ExonerationCotisationsDto(
        Boolean exclueLemoine,
        List<ExonerationCotisationsCoefficientDto> coefficients
    ) {
    }

    public record ExonerationCotisationsCoefficientDto(String regimeLemoine, Double coefficient) {
    }

    public record TypePretEligibiliteDto(
        ReferenceDto typePret,
        String regimeLemoine,
        Boolean booEligible
    ) {
    }

    public record ObjetPretEligibiliteDto(
        ReferenceDto objetPret,
        String regimeLemoine,
        Boolean booEligible
    ) {
    }

    public record TypePretCoefficientDto(
        ReferenceDto typePret,
        String branche,
        String regimeLemoine,
        Double coefficient
    ) {
    }

    public record ObjetPretCoefficientDto(
        ReferenceDto objetPret,
        String regimeLemoine,
        Double coefficient
    ) {
    }

    public record ClasseRisqueCoefficientDto(
        ReferenceDto classeRisque,
        String branche,
        String regimeLemoine,
        Double coeffPassage
    ) {
    }

    public record TerritorialiteCoefficientDto(ReferenceDto territorialite, Double coefficient) {
    }

    public record CourbeDeformationCrdDto(
        Integer anciennetePretAnnees,
        Integer dureePretAnnees,
        Double coefficient
    ) {
    }

    public record TypePretCourbeDeformationCrdDto(
        ReferenceDto typePret,
        Boolean booCourbeCrdApplicable
    ) {
    }

    public record CoefficientPassageFumeurCiDto(
        Integer ageAdhesion,
        String branche,
        Double coefficient
    ) {
    }

    public record CoefficientPassageFumeurCrdDto(
        Integer ageAtteint,
        String branche,
        Double coefficient
    ) {
    }

    public record CoefficientPerimetreLemoineCiDto(
        Integer ageAdhesion,
        String branche,
        Double coefficient
    ) {
    }

    public record CoefficientPerimetreLemoineCrdDto(
        Integer ageAdhesion,
        String branche,
        Double coefficient
    ) {
    }

    public record CoefficientAerasCiDto(
        Integer ageAdhesion,
        Integer dureePretAnnees,
        Double coefficient
    ) {
    }

    public record CoefficientAerasCrdDto(Integer ageAtteint, Double coefficient) {
    }

    public record CoverageEndCoefficientDto(
        ReferenceDto ageFinCouverture,
        Integer ageAdhesion,
        Double coefficient
    ) {
    }

    public record CategorieProClasseRisqueMappingDto(
        ReferenceDto categorieProfessionnelle,
        ReferenceDto classeRisqueDC,
        ReferenceDto classeRisqueAT,
        List<CriteresProDto> criteres
    ) {
    }

    public record CriteresProDto(
        ReferenceDto critereProfessionnel,
        Boolean valeurAttendue
    ) {
    }

    public record ExclusionCategorieProDto(
        ReferenceDto categorieProfessionnelle,
        Boolean exclueLemoine,
        Boolean exclueGarantieNonVie
    ) {
    }

    public record ProfessionClasseRisqueMappingDto(
        ReferenceDto profession,
        String branche,
        ReferenceDto classeRisque
    ) {
    }
}
