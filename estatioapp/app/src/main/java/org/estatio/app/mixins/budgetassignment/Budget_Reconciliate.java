package org.estatio.app.mixins.budgetassignment;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.budgeting.budget.Budget;

@Mixin
public class Budget_Reconciliate {

    private final Budget budget;
    public Budget_Reconciliate(Budget budget){
        this.budget = budget;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(contributed = Contributed.AS_ACTION)
    public Budget reconciliate() {
        // TODO: implement
        return budget;
    }

}
