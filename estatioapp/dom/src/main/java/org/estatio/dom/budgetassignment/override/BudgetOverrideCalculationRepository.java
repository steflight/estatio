package org.estatio.dom.budgetassignment.override;

import java.math.BigDecimal;
import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

import org.estatio.dom.UdoDomainRepositoryAndFactory;

@DomainService(repositoryFor = BudgetOverrideCalculation.class, nature = NatureOfService.DOMAIN)
public class BudgetOverrideCalculationRepository extends UdoDomainRepositoryAndFactory<BudgetOverrideCalculation> {

    public BudgetOverrideCalculationRepository() {
        super(BudgetOverrideCalculationRepository.class, BudgetOverrideCalculation.class);
    }

    public BudgetOverrideCalculation newBudgetOverrideCalculation(
            final BigDecimal value,
            final BudgetOverride budgetOverride){
        BudgetOverrideCalculation newCalculation = newTransientInstance(BudgetOverrideCalculation.class);
        newCalculation.setValue(value);
        newCalculation.setBudgetOverride(budgetOverride);
        persistIfNotAlready(newCalculation);
        return newCalculation;
    }

    public List<BudgetOverrideCalculation> allBudgetOverrideCalculations(){
        return allInstances();
    }

}
