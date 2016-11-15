package org.estatio.dom.budgetassignment.override;

public enum BudgetOverrideType {

    FLATRATE("flatrate"),
    CEILING("ceiling"),
    FIXED(null);

    public final String reason;

    BudgetOverrideType(final String reason) {
        this.reason = reason;
    }
}
