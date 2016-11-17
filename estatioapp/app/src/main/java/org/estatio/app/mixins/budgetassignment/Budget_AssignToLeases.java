package org.estatio.app.mixins.budgetassignment;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.budgetassignment.BudgetAssignmentService;
import org.estatio.dom.budgeting.budget.Budget;

@Mixin
public class Budget_AssignToLeases {

    private final Budget budget;
    public Budget_AssignToLeases(Budget budget){
        this.budget = budget;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(contributed = Contributed.AS_ACTION)
    public Budget assignToLeases() {
        budgetAssignmentService.assign(budget);
        return budget;
    }

    @Inject
    private BudgetAssignmentService budgetAssignmentService;

}
