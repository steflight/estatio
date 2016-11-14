package org.estatio.dom.budgetassignment.calculationresult;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.estatio.dom.UdoDomainObject2;
import org.estatio.dom.budgetassignment.override.BudgetOverride;
import org.estatio.dom.budgetassignment.override.BudgetOverrideCalculation;
import org.estatio.dom.budgetassignment.override.BudgetOverrideRepository;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculation;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationRepository;
import org.estatio.dom.budgeting.partioning.PartitionItemRepository;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.lease.Occupancy;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE
        ,schema = "dbo" // Isis' ObjectSpecId inferred from @DomainObject#objectType
)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE,
        column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@Unique(name = "BudgetCalculationResult_budgetCalculationRun_invoiceCharge_UNQ", members = { "budgetCalculationRun", "invoiceCharge" })

@DomainObject(
        objectType = "org.estatio.dom.budgetassignment.calculationresult.BudgetCalculationResult"
)
public class BudgetCalculationResult extends UdoDomainObject2<BudgetCalculationResult> {

    public BudgetCalculationResult() {
        super("budgetCalculationRun, invoiceCharge");
    }

    @Getter @Setter
    @Column(name = "budgetCalculationRunId", allowsNull = "false")
    private BudgetCalculationRun budgetCalculationRun;

    @Getter @Setter
    @Column(name = "chargeId", allowsNull = "false")
    private Charge invoiceCharge;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ASSOCIATION)
    public List<BudgetOverrideCalculation> overrideCalculations(){
        List<BudgetOverrideCalculation> results = new ArrayList<>();
        for (BudgetOverride override : budgetOverrideRepository.findByLeaseAndInvoiceChargeAndType(getBudgetCalculationRun().getLease(), getInvoiceCharge(), getBudgetCalculationRun().getType())){
            results.addAll(override.getCalculations());
        }
        return results;
    }

    public List<BudgetCalculation> budgetCalculations(){
        List<BudgetCalculation> results = new ArrayList<>();
        for (Occupancy occupancy : getBudgetCalculationRun().getLease().getOccupancies()) {
            results.addAll(budgetCalculationRepository.findByBudgetAndUnitAndInvoiceChargeAndType(getBudgetCalculationRun().getBudget(), occupancy.getUnit(), getInvoiceCharge(), getBudgetCalculationRun().getType()));
        }
        return results;
    }


    @Override
    public ApplicationTenancy getApplicationTenancy() {
        return getBudgetCalculationRun().getApplicationTenancy();
    }

    @Inject
    private BudgetOverrideRepository budgetOverrideRepository;

    @Inject
    private PartitionItemRepository partitionItemRepository;

    @Inject
    private BudgetCalculationRepository budgetCalculationRepository;

}
