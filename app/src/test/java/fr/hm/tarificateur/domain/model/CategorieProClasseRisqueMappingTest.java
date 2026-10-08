package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CategorieProClasseRisqueMappingTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference categorieProfessionnelle = new Reference("CAT001", "Categorie Pro 1");
        Reference classeRisque = new Reference("CR001", "Classe Risque 1");
        List<CriterePro> criteres = new ArrayList<>();

        CategorieProClasseRisqueMapping result = new CategorieProClasseRisqueMapping(categorieProfessionnelle, classeRisque, criteres);

        assertThat(result).isNotNull();
        assertThat(result.categorieProfessionnelle()).isEqualTo(categorieProfessionnelle);
        assertThat(result.classeRisqueDC()).isEqualTo(classeRisque);
        assertThat(result.classeRisqueAT()).isEqualTo(classeRisque);
        assertThat(result.criteres()).isEmpty();
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference cat = new Reference("CAT002", "Categorie Pro 2");
        Reference cr = new Reference("CR002", "Classe Risque 2");
        List<CriterePro> criteres = new ArrayList<>();
        criteres.add(new CriterePro(new Reference("CRIT001", "Criterion 1"), true));
        
        CategorieProClasseRisqueMapping cpcrm = new CategorieProClasseRisqueMapping(cat, cr, criteres);

        assertThat(cpcrm.categorieProfessionnelle()).isEqualTo(cat);
        assertThat(cpcrm.classeRisqueDC()).isEqualTo(cr);
        assertThat(cpcrm.classeRisqueAT()).isEqualTo(cr);
        assertThat(cpcrm.criteres()).hasSize(1);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference cat = new Reference("CAT001", "Categorie Pro 1");
        Reference cr = new Reference("CR001", "Classe Risque 1");
        CategorieProClasseRisqueMapping cpcrm1 = new CategorieProClasseRisqueMapping(cat, cr, new ArrayList<>());
        CategorieProClasseRisqueMapping cpcrm2 = new CategorieProClasseRisqueMapping(cat, cr, new ArrayList<>());

        assertThat(cpcrm1).isEqualTo(cpcrm2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference cat1 = new Reference("CAT001", "Categorie Pro 1");
        Reference cat2 = new Reference("CAT002", "Categorie Pro 2");
        Reference cr = new Reference("CR001", "Classe Risque 1");
        CategorieProClasseRisqueMapping cpcrm1 = new CategorieProClasseRisqueMapping(cat1, cr, new ArrayList<>());
        CategorieProClasseRisqueMapping cpcrm2 = new CategorieProClasseRisqueMapping(cat2, cr, new ArrayList<>());

        assertThat(cpcrm1).isNotEqualTo(cpcrm2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference cat = new Reference("CAT001", "Categorie Pro 1");
        Reference cr = new Reference("CR001", "Classe Risque 1");
        CategorieProClasseRisqueMapping cpcrm1 = new CategorieProClasseRisqueMapping(cat, cr, new ArrayList<>());
        CategorieProClasseRisqueMapping cpcrm2 = new CategorieProClasseRisqueMapping(cat, cr, new ArrayList<>());

        assertThat(cpcrm1).hasSameHashCodeAs(cpcrm2);
    }

    @Test
    void shouldHandleNullValues() {
        CategorieProClasseRisqueMapping cpcrm =
            new CategorieProClasseRisqueMapping(null, null, null, null);

        assertThat(cpcrm.categorieProfessionnelle()).isNull();
        assertThat(cpcrm.classeRisqueDC()).isNull();
        assertThat(cpcrm.classeRisqueAT()).isNull();
        assertThat(cpcrm.criteres()).isNull();
    }

    @Test
    void shouldHandlePopulatedCriteresList() {
        Reference cat = new Reference("CAT001", "Categorie Pro 1");
        Reference cr = new Reference("CR001", "Classe Risque 1");
        List<CriterePro> criteres = new ArrayList<>();
        criteres.add(new CriterePro(new Reference("CRIT001", "Criterion 1"), true));
        criteres.add(new CriterePro(new Reference("CRIT002", "Criterion 2"), false));
        
        CategorieProClasseRisqueMapping cpcrm = new CategorieProClasseRisqueMapping(cat, cr, criteres);

        assertThat(cpcrm.criteres()).hasSize(2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference cat = new Reference("CAT003", "Categorie Pro 3");
        Reference cr = new Reference("CR003", "Classe Risque 3");
        CategorieProClasseRisqueMapping cpcrm = new CategorieProClasseRisqueMapping(cat, cr, new ArrayList<>());
        String toStringResult = cpcrm.toString();

        assertThat(toStringResult).contains("CategorieProClasseRisqueMapping");
    }
}
