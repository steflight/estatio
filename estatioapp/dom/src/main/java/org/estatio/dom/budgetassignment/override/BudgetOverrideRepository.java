package org.estatio.dom.budgetassignment.override;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;

import org.estatio.dom.UdoDomainRepositoryAndFactory;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.lease.Lease;

@DomainService(repositoryFor = BudgetOverride.class, nature = NatureOfService.DOMAIN)
public class BudgetOverrideRepository extends UdoDomainRepositoryAndFactory<BudgetOverride> {

    public BudgetOverrideRepository() {
        super(BudgetOverrideRepository.class, BudgetOverride.class);
    }

    public BudgetOverrideForFixed newBudgetOverrideForFixed(
            final BigDecimal fixedValue,
            final Lease lease,
            @Parameter(optionality = Optionality.OPTIONAL)
            final LocalDate startDate,
            @Parameter(optionality = Optionality.OPTIONAL)
            final LocalDate endDate,
            final Charge invoiceCharge,
            @Parameter(optionality = Optionality.OPTIONAL)
            final Charge incomingCharge,
            @Parameter(optionality = Optionality.OPTIONAL)
            final BudgetCalculationType type,
            final String reason){
        BudgetOverrideForFixed newOverride = newTransientInstance(BudgetOverrideForFixed.class);
        newOverride = (BudgetOverrideForFixed) setValues(newOverride, lease, startDate, endDate, invoiceCharge, incomingCharge, type, reason);
        newOverride.setFixedValue(fixedValue);
        persistIfNotAlready(newOverride);
        return newOverride;
    }

    public BudgetOverrideForFlatRate newBudgetOverrideForFlatRate(
            final BigDecimal valueM2,
            final BigDecimal weightedArea,
            final Lease lease,
            @Parameter(optionality = Optionality.OPTIONAL)
            final LocalDate startDate,
            @Parameter(optionality = Optionality.OPTIONAL)
            final LocalDate endDate,
            final Charge invoiceCharge,
            @Parameter(optionality = Optionality.OPTIONAL)
            final Charge incomingCharge,
            @Parameter(optionality = Optionality.OPTIONAL)
            final BudgetCalculationType type,
            final String reason){
        BudgetOverrideForFlatRate newOverride = newTransientInstance(BudgetOverrideForFlatRate.class);
        newOverride = (BudgetOverrideForFlatRate) setValues(newOverride, lease, startDate, endDate, invoiceCharge, incomingCharge, type, reason);
        newOverride.setValuePerM2(valueM2);
        newOverride.setWeightedArea(weightedArea);
        persistIfNotAlready(newOverride);
        return newOverride;
    }

    public BudgetOverrideForMax newBudgetOverrideForMax(
            final BigDecimal maxValue,
            final Lease lease,
            @Parameter(optionality = Optionality.OPTIONAL)
            final LocalDate startDate,
            @Parameter(optionality = Optionality.OPTIONAL)
            final LocalDate endDate,
            final Charge invoiceCharge,
            @Parameter(optionality = Optionality.OPTIONAL)
            final Charge incomingCharge,
            @Parameter(optionality = Optionality.OPTIONAL)
            final BudgetCalculationType type,
            final String reason){
        BudgetOverrideForMax newOverride = newTransientInstance(BudgetOverrideForMax.class);
        newOverride = (BudgetOverrideForMax) setValues(newOverride, lease, startDate, endDate, invoiceCharge, incomingCharge, type, reason);
        newOverride.setMaxValue(maxValue);
        persistIfNotAlready(newOverride);
        return newOverride;
    }

    private BudgetOverride setValues(
            final BudgetOverride budgetOverride,
            final Lease lease,
            final LocalDate startDate,
            final LocalDate endDate,
            final Charge invoiceCharge,
            final Charge incomingCharge,
            final BudgetCalculationType type,
            final String reason
            ){
        budgetOverride.setLease(lease);
        budgetOverride.setStartDate(startDate);
        budgetOverride.setEndDate(endDate);
        budgetOverride.setInvoiceCharge(invoiceCharge);
        budgetOverride.setIncomingCharge(incomingCharge);
        budgetOverride.setType(type);
        budgetOverride.setReason(reason);
        return budgetOverride;
    }

    public List<BudgetOverride> allBudgetOverrides(){
        List<BudgetOverrideForMax> allMax = allInstances(BudgetOverrideForMax.class);
        List<BudgetOverrideForFixed> allFixed = allInstances(BudgetOverrideForFixed.class);
        List<BudgetOverride> result = new ArrayList<>();
        result.addAll(allMax);
        result.addAll(allFixed);
        return result;
    }

    public List<BudgetOverride> findByLease(final Lease lease) {
        return allMatches("findByLease", "lease", lease);
    }

    public List<BudgetOverride> findByLeaseAndInvoiceChargeAndType(final Lease lease, final Charge invoiceCharge, final BudgetCalculationType type) {
        return allMatches("findByLeaseAndInvoiceChargeAndType", "lease", lease, "invoiceCharge", invoiceCharge, "type", type);
    }
}
