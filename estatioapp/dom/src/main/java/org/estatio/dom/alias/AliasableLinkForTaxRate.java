package org.estatio.dom.alias;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import com.google.common.eventbus.Subscribe;

import org.apache.isis.applib.AbstractSubscriber;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

import org.incode.module.alias.dom.api.aliasable.Aliasable;
import org.incode.module.alias.dom.impl.aliaslink.AliasableLink;

import org.estatio.dom.tax.TaxRate;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType= IdentityType.DATASTORE)
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@DomainObject()
public class AliasableLinkForTaxRate extends AliasableLink {

    //region > instantiationSubscriber, setPolymorphicReference
    @DomainService(nature = NatureOfService.DOMAIN)
    public static class InstantiationSubscriber extends AbstractSubscriber {
        @Programmatic
        @Subscribe
        public void on(final InstantiateEvent ev) {
            if(ev.getPolymorphicReference() instanceof TaxRate) {
                ev.setSubtype(AliasableLinkForTaxRate.class);
            }
        }
    }

    @Override
    public void setPolymorphicReference(final Aliasable polymorphicReference) {
        super.setPolymorphicReference(polymorphicReference);
        setTaxRate((TaxRate) polymorphicReference);
    }

    @Column(
            allowsNull = "false",
            name = "taxRateId"
    )
    @Getter @Setter
    private TaxRate taxRate;

}