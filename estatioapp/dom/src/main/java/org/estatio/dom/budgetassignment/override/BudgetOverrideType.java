package org.estatio.dom.budgetassignment.override;

public enum BudgetOverrideType {

    FORFAIT("forfait"),
    CAPS("caps"),
    OTHER(null);

    public final String reason;

    BudgetOverrideType(final String reason) {
        this.reason = reason;
    }
}
