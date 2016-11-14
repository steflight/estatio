package org.estatio.dom.budgetassignment.override;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.VersionStrategy;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;

import org.estatio.dom.UdoDomainObject2;
import org.estatio.dom.budgeting.budget.BudgetRepository;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.lease.Lease;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE
        ,schema = "dbo"     // Isis' ObjectSpecId inferred from @Discriminator
)
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.Discriminator(
        strategy = DiscriminatorStrategy.VALUE_MAP,
        column = "discriminator",
        value = "org.estatio.dom.budgetassignment.override.BudgetOverride"
)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.IDENTITY,
        column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Queries({
        @Query(
                name = "findByLease", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.budgetassignment.override.BudgetOverride " +
                        "WHERE lease == :lease"),
        @Query(
                name = "findByLeaseAndInvoiceChargeAndType", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.budgetassignment.override.BudgetOverride " +
                        "WHERE lease == :lease && "
                        + "invoiceCharge == :invoiceCharge && "
                        + "type == :type")
})

@DomainObject()
public abstract class BudgetOverride extends UdoDomainObject2<BudgetOverride> {

    public BudgetOverride() {
        super("lease, invoiceCharge, type, incomingCharge, startDate, endDate, reason");
    }

    @Getter @Setter
    @Column(name = "leaseId", allowsNull = "false")
    @PropertyLayout(hidden = Where.PARENTED_TABLES)
    private Lease lease;

    @Getter @Setter
    @Column(allowsNull = "true")
    private LocalDate startDate;

    @Getter @Setter
    @Column(allowsNull = "true")
    private LocalDate endDate;

    @Getter @Setter
    @Column(name = "chargeId", allowsNull = "false")
    private Charge invoiceCharge;

    @Getter @Setter
    @Column(name = "incomingChargeId", allowsNull = "true")
    private Charge incomingCharge;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BudgetCalculationType type;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String reason;

    @Getter @Setter
    @Persistent(mappedBy = "budgetOverride", dependentElement = "true")
    private SortedSet<BudgetOverrideCalculation> calculations = new TreeSet<>();

    public abstract void calculate(final LocalDate calculationDate);

    public boolean isActiveOnCalculationDate(final LocalDate calculationDate) {
        if (getStartDate()!=null && calculationDate.isBefore(getStartDate())){
            return false;
        }
        if (getEndDate()!=null && calculationDate.isAfter(getEndDate())){
            return false;
        }
        return true;
    }

    @Inject
    BudgetRepository budgetRepository;

}
