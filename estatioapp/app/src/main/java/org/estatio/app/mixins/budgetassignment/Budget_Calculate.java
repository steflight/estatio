package org.estatio.app.mixins.budgetassignment;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.budgetassignment.BudgetAssignmentService;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationService;

@Mixin
public class Budget_Calculate {

    private final Budget budget;
    public Budget_Calculate(Budget budget){
        this.budget = budget;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ACTION)
    public void calculate() {
        budgetCalculationService.calculatePersistedCalculations(budget);
        budgetAssignmentService.calculateOverrideValues(budget);
        budgetAssignmentService.assign(budget);
    }

    @Inject
    private BudgetCalculationService budgetCalculationService;

    @Inject
    private BudgetAssignmentService budgetAssignmentService;

}
