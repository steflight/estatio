package org.estatio.app.mixins.budgetassignment;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.budgetassignment.override.BudgetOverrideForMax;
import org.estatio.dom.budgetassignment.override.BudgetOverrideRepository;
import org.estatio.dom.budgetassignment.override.BudgetOverrideType;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.lease.Lease;

@Mixin
public class Lease_NewMaxValueOverride {

    private final Lease lease;
    public Lease_NewMaxValueOverride(Lease lease){
        this.lease = lease;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ACTION)
    public BudgetOverrideForMax newMaxValue(
            final BigDecimal maxValue,
            @Parameter(optionality = Optionality.OPTIONAL)
            final LocalDate startDate,
            @Parameter(optionality = Optionality.OPTIONAL)
            final LocalDate endDate,
            final Charge invoiceCharge,
            @Parameter(optionality = Optionality.OPTIONAL)
            final Charge incomingCharge,
            @Parameter(optionality = Optionality.OPTIONAL)
            final BudgetCalculationType type
    ) {
        return budgetOverrideRepository.newBudgetOverrideForMax(maxValue,lease,startDate,endDate,invoiceCharge,incomingCharge,type,BudgetOverrideType.CAPS.reason);
    }

    @Inject
    private BudgetOverrideRepository budgetOverrideRepository;

}
