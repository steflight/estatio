package org.estatio.dom.budgetassignment.override;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.timestamp.Timestampable;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.estatio.dom.UdoDomainObject2;
import org.estatio.dom.apptenancy.WithApplicationTenancyProperty;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;

import lombok.Getter;
import lombok.Setter;


@PersistenceCapable(
        identityType = IdentityType.DATASTORE
        ,schema = "dbo" // Isis' ObjectSpecId inferred from @DomainObject#objectType
)
@DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE,
        column = "id")
@Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")

@DomainObject(
        objectType = "org.estatio.dom.budgetassignment.override.BudgetOverrideCalculation"
)
public class BudgetOverrideCalculation extends UdoDomainObject2<BudgetOverrideCalculation>
        implements WithApplicationTenancyProperty, Timestampable {

    public BudgetOverrideCalculation() {
        super("budgetOverride, value");
    }

    @Getter @Setter
    @Column(name = "budgetOverrideId", allowsNull = "false")
    private BudgetOverride budgetOverride;

    @Getter @Setter
    @Column(scale = 2, allowsNull = "false")
    private BigDecimal value;

    @Getter @Setter
    @Column(allowsNull = "false")
    private BudgetCalculationType type;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(hidden = Where.ALL_TABLES)
    private Timestamp updatedAt;

    @Getter @Setter
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @Column(allowsNull = "true")
    private String updatedBy;

    @Override
    public ApplicationTenancy getApplicationTenancy() {
        return budgetOverride.getApplicationTenancy();
    }
}
