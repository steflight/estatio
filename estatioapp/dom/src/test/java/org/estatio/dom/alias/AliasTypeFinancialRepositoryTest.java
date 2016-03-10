package org.estatio.dom.alias;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import org.incode.module.alias.dom.api.aliasable.AliasType;

import org.estatio.dom.asset.Unit;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.party.Person;
import org.estatio.dom.tax.Tax;
import org.estatio.dom.tax.TaxRate;

public class AliasTypeFinancialRepositoryTest {

    AliasTypeFinancialRepository repo;

    @Before
    public void setUp() {
        repo = new AliasTypeFinancialRepository();
    }

    @Test
    public void aliasTypesForParty() throws Exception {

        // given
        Person person = new Person();

        // when
        List<AliasType> result = repo.aliasTypesFor(person, null);

        // then
        Assertions.assertThat(result.size()).isEqualTo(2);
        Assertions.assertThat(result.get(0).getId()).isEqualTo("CODA_EL6_DEBITOR");
        Assertions.assertThat(result.get(1).getId()).isEqualTo("CODA_EL6_CREDITOR");

    }

    @Test
    public void aliasTypesForCharge() throws Exception {

        // given
        Charge charge = new Charge();

        // when
        List<AliasType> result = repo.aliasTypesFor(charge, null);

        // then
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getId()).isEqualTo("CODA_EL5");

    }

    @Test
    public void aliasTypesForTax() throws Exception {

        // given
        Tax tax = new Tax();

        // when
        List<AliasType> result = repo.aliasTypesFor(tax, null);

        // then
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getId()).isEqualTo("CODA_EL6");

    }

    @Test
    public void aliasTypesForTaxRate() throws Exception {

        // given
        TaxRate taxRate = new TaxRate();

        // when
        List<AliasType> result = repo.aliasTypesFor(taxRate, null);

        // then
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getId()).isEqualTo("CODA_EL6");

    }

    @Test
    public void aliasTypesForFixedAsset() throws Exception {

        // given
        Unit unit = new Unit();

        // when
        List<AliasType> result = repo.aliasTypesFor(unit, null);

        // then
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getId()).isEqualTo("CODA_EL3");

    }

}
