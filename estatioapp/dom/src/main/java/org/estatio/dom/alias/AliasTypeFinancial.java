package org.estatio.dom.alias;

import org.apache.isis.applib.annotation.Title;

import org.incode.module.alias.dom.api.aliasable.AliasType;

import org.estatio.dom.asset.FixedAsset;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.party.Party;
import org.estatio.dom.tax.Tax;
import org.estatio.dom.tax.TaxRate;

public enum AliasTypeFinancial implements AliasType {

    CODA_EL3(FixedAsset.class),
    CODA_EL5(Charge.class),
    CODA_EL6(Tax.class, TaxRate.class),
    CODA_EL6_DEBITOR(Party.class),
    CODA_EL6_CREDITOR(Party.class);


    @Title
    @Override
    public String getId() {
        return name();
    }

    public Class[] getClassAssociated() {
        return this.clss;
    }

    private final Class[] clss;

    private AliasTypeFinancial(
            final Class... clss) {
        this.clss = clss;
    }

}
