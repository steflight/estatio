package org.estatio.integtests.budgetassignment;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.budgetassignment.calculationresult.BudgetCalculationRun;
import org.estatio.dom.budgetassignment.calculationresult.BudgetCalculationRunRepository;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budget.BudgetRepository;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.budgeting.budgetcalculation.Status;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForOxfGb;
import org.estatio.fixture.budget.BudgetsForOxf;
import org.estatio.fixture.lease.LeaseForOxfTopModel001Gb;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetCalculationRunRepositoryTest extends EstatioIntegrationTest {

    @Inject
    BudgetRepository budgetRepository;

    @Inject
    BudgetCalculationRunRepository budgetCalculationRunRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    LeaseRepository leaseRepository;

    Property propertyOxf;
    List<Budget> budgetsForOxf;
    Budget budget2015;

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new BudgetsForOxf());
                executionContext.executeChild(this, new LeaseForOxfTopModel001Gb());
            }
        });
        propertyOxf = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
        budgetsForOxf = budgetRepository.findByProperty(propertyOxf);
        budget2015 = budgetRepository.findByPropertyAndStartDate(propertyOxf, BudgetsForOxf.BUDGET_2015_START_DATE);
    }

    public static class FindOrCreate extends BudgetCalculationRunRepositoryTest {

        @Test
        public void test() {

            Lease leaseTopModel;

            // given
            leaseTopModel = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(0);

            // when
            BudgetCalculationRun run = wrap(budgetCalculationRunRepository).findOrCreateNewBudgetCalculationRun(leaseTopModel, budget2015, BudgetCalculationType.BUDGETED);

            // then
            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(1);
            assertThat(run.getBudget()).isEqualTo(budget2015);
            assertThat(run.getLease()).isEqualTo(leaseTopModel);
            assertThat(run.getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(run.getStatus()).isEqualTo(Status.NEW);

            // and when again
            wrap(budgetCalculationRunRepository).findOrCreateNewBudgetCalculationRun(leaseTopModel, budget2015, BudgetCalculationType.BUDGETED);

            // then is idemPotent
            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(1);

            // and when again
            run = wrap(budgetCalculationRunRepository).findOrCreateNewBudgetCalculationRun(leaseTopModel, budget2015, BudgetCalculationType.AUDITED);

            // then
            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(2);
            assertThat(run.getType()).isEqualTo(BudgetCalculationType.AUDITED);

        }
    }

    public static class FindByLease extends BudgetCalculationRunRepositoryTest {

        @Test
        public void findByLease() {

            Lease leaseTopModel;

            // given
            leaseTopModel = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
            assertThat(budgetCalculationRunRepository.findByLease(leaseTopModel).size()).isEqualTo(0);

            // when
            wrap(budgetCalculationRunRepository).findOrCreateNewBudgetCalculationRun(leaseTopModel, budget2015, BudgetCalculationType.BUDGETED);

            // then
            assertThat(budgetCalculationRunRepository.findByLease(leaseTopModel).size()).isEqualTo(1);

        }

    }

}
