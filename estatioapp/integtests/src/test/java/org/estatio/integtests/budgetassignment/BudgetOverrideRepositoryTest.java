package org.estatio.integtests.budgetassignment;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.budgetassignment.override.BudgetOverrideForFixed;
import org.estatio.dom.budgetassignment.override.BudgetOverrideForMax;
import org.estatio.dom.budgetassignment.override.BudgetOverrideRepository;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.charge.ChargeRepository;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.charge.ChargeRefData;
import org.estatio.fixture.lease.LeaseForOxfTopModel001Gb;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetOverrideRepositoryTest extends EstatioIntegrationTest {

    @Inject
    BudgetOverrideRepository budgetOverrideRepository;

    @Inject
    PropertyRepository propertyRepository;

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

    public static class NewBudgetOverride extends BudgetOverrideRepositoryTest {

        @Test
        public void newBudgetOverrideWorks() {

            BigDecimal overrideValue;
            String reason;

            // given
            Lease leaseTopModel = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
            Charge invoiceCharge = chargeRepository.findByReference(ChargeRefData.GB_SERVICE_CHARGE);

            overrideValue = new BigDecimal("1234.56");
            reason = "Some reason";

            assertThat(budgetOverrideRepository.allBudgetOverrides().size()).isEqualTo(0);

            // when
            BudgetOverrideForFixed budgetOverrideForFixed = wrap(budgetOverrideRepository).newBudgetOverrideForFixed(overrideValue, leaseTopModel, null, null, invoiceCharge, null, null, reason);

            // then
            assertThat(budgetOverrideRepository.allBudgetOverrides().size()).isEqualTo(1);
            assertThat(budgetOverrideForFixed.getFixedValue()).isEqualTo(overrideValue);
            assertThat(budgetOverrideForFixed.getLease()).isEqualTo(leaseTopModel);
            assertThat(budgetOverrideForFixed.getInvoiceCharge()).isEqualTo(invoiceCharge);
            assertThat(budgetOverrideForFixed.getReason()).isEqualTo(reason);

            // and when
            BudgetOverrideForMax budgetOverrideForMax = wrap(budgetOverrideRepository).newBudgetOverrideForMax(overrideValue, leaseTopModel, null, null, invoiceCharge, null, null, reason);

            // then
            assertThat(budgetOverrideRepository.allBudgetOverrides().size()).isEqualTo(2);

        }
    }

    public static class FindByLease extends BudgetOverrideRepositoryTest {

        @Test
        public void findByLease() {

            Lease leaseTopModel;

            // given
            leaseTopModel = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
            Charge invoiceCharge = chargeRepository.findByReference(ChargeRefData.GB_SERVICE_CHARGE);
            BigDecimal overrideValue = new BigDecimal("1234.56");
            String reason = "Some reason";
            assertThat(budgetOverrideRepository.findByLease(leaseTopModel).size()).isEqualTo(0);

            // when
            BudgetOverrideForFixed budgetOverrideForFixed = wrap(budgetOverrideRepository).newBudgetOverrideForFixed(overrideValue, leaseTopModel, null, null, invoiceCharge, null, null, reason);

            // then
            assertThat(budgetOverrideRepository.findByLease(leaseTopModel).size()).isEqualTo(1);
            assertThat(budgetOverrideForFixed.getLease()).isEqualTo(leaseTopModel);

        }

    }





}
