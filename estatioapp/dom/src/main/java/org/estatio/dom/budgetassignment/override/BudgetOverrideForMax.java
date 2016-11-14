package org.estatio.dom.budgetassignment.override;

import java.math.BigDecimal;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.InheritanceStrategy;

import org.joda.time.LocalDate;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budgetitem.BudgetItem;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        schema = "dbo"     // Isis' ObjectSpecId inferred from @Discriminator
)
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.SUPERCLASS_TABLE)
@javax.jdo.annotations.Discriminator("org.estatio.dom.budgetassignment.override.BudgetOverrideForMax")
public class BudgetOverrideForMax extends BudgetOverride {

    @Getter @Setter
    @Column(scale = 2)
    private BigDecimal maxValue;

    @Override
    public void calculate(final LocalDate budgetStartDate){
        if (isActiveOnCalculationDate(budgetStartDate)) {
            Budget budget = budgetRepository.findByPropertyAndDate(getLease().getProperty(), budgetStartDate);
            if (getIncomingCharge() == null) {
                for (BudgetItem item : budget.getItems()) {
                    // create budget override calculation
                }
            } else {
                // create (one) budget override calculation
            }
        }
    }

    @Override
    public ApplicationTenancy getApplicationTenancy() {
        return getLease().getApplicationTenancy();
    }

}
