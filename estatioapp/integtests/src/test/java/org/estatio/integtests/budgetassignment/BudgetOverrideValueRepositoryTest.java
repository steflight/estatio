package org.estatio.integtests.budgetassignment;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.budgetassignment.override.BudgetOverrideForFixed;
import org.estatio.dom.budgetassignment.override.BudgetOverrideRepository;
import org.estatio.dom.budgetassignment.override.BudgetOverrideValue;
import org.estatio.dom.budgetassignment.override.BudgetOverrideValueRepository;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.budgeting.budgetcalculation.Status;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.charge.ChargeRepository;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.charge.ChargeRefData;
import org.estatio.fixture.lease.LeaseForOxfTopModel001Gb;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetOverrideValueRepositoryTest extends EstatioIntegrationTest {

    @Inject
    BudgetOverrideRepository budgetOverrideRepository;

    @Inject
    BudgetOverrideValueRepository budgetOverrideValueRepository;

    @Inject
    LeaseRepository leaseRepository;

    @Inject
    ChargeRepository chargeRepository;

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new LeaseForOxfTopModel001Gb());
            }
        });
    }

    @Test
    public void newBudgetOverrideCalculationWorks(){

        BudgetOverrideForFixed budgetOverrideForFixed;
        BudgetOverrideValue budgetOverrideValue;
        BigDecimal calculatedValue = new BigDecimal("1234.56");

        // given
        Lease leaseTopModel = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
        Charge invoiceCharge = chargeRepository.findByReference(ChargeRefData.GB_SERVICE_CHARGE);
        BigDecimal overrideValue = new BigDecimal("1234.56");
        String reason = "Some reason";
        budgetOverrideForFixed = wrap(budgetOverrideRepository).newBudgetOverrideForFixed(overrideValue, leaseTopModel, null, null, invoiceCharge, null, null, reason);
        assertThat(budgetOverrideValueRepository.allBudgetOverrideCalculations().size()).isEqualTo(0);

        // when
        budgetOverrideValue = wrap(budgetOverrideValueRepository).newBudgetOverrideValue(calculatedValue, budgetOverrideForFixed, BudgetCalculationType.BUDGETED);

        // then
        assertThat(budgetOverrideValueRepository.allBudgetOverrideCalculations().size()).isEqualTo(1);
        assertThat(budgetOverrideValue.getValue()).isEqualTo(calculatedValue);
        assertThat(budgetOverrideValue.getBudgetOverride()).isEqualTo(budgetOverrideForFixed);
        assertThat(budgetOverrideValue.getType()).isEqualTo(BudgetCalculationType.BUDGETED);
        assertThat(budgetOverrideValue.getStatus()).isEqualTo(Status.NEW);
        assertThat(budgetOverrideValue.getApplicationTenancy()).isEqualTo(budgetOverrideForFixed.getApplicationTenancy());

    }







}
