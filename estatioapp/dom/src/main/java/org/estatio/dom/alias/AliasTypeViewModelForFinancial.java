package org.estatio.dom.alias;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.isis.applib.annotation.Title;

import org.incode.module.alias.dom.api.aliasable.AliasType;

@XmlRootElement
public class AliasTypeViewModelForFinancial implements AliasType {

    @XmlElement
    private AliasTypeFinancial aliasTypeFinancial;

    public AliasTypeViewModelForFinancial() {
    }

    public AliasTypeViewModelForFinancial(final AliasTypeFinancial aliasTypeFinancial) {
        this.aliasTypeFinancial = aliasTypeFinancial;
    }

    @Title
    @XmlTransient
    @Override public String getId() {
        return aliasTypeFinancial.getId();
    }

}
