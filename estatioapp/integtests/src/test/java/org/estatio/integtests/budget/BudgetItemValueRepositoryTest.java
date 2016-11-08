package org.estatio.integtests.budget;

import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budget.BudgetRepository;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.budgeting.budgetitem.BudgetItem;
import org.estatio.dom.budgeting.budgetitem.BudgetItemValue;
import org.estatio.dom.budgeting.budgetitem.BudgetItemValueRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForOxfGb;
import org.estatio.fixture.budget.BudgetsForOxf;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetItemValueRepositoryTest extends EstatioIntegrationTest {

    @Inject PropertyRepository propertyRepository;

    @Inject BudgetRepository budgetRepository;

    @Inject BudgetItemValueRepository budgetItemValueRepository;

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new BudgetsForOxf());
            }
        });
    }

    @Test
    @Ignore // TODO: fails when running all integration tests while other, not type-safe version, works; fine when running tests in this class only
    public void findByBudgetItemAndTypeTest() {

        // given
        Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
        Budget budget = budgetRepository.findByPropertyAndStartDate(property, new LocalDate(2015, 01, 01));
        BudgetItem budgetItem = budget.getItems().first();

        assertThat(budgetItem.getValues().size()).isEqualTo(1);
        assertThat(budgetItem.getValues().first().getType()).isEqualTo(BudgetCalculationType.BUDGETED);

        // when
        List<BudgetItemValue> results = budgetItemValueRepository.findByBudgetItemAndType(budgetItem, BudgetCalculationType.BUDGETED);

        // then
        assertThat(results.size()).isEqualTo(1);
    }

    // TODO: This temporary method and test is for comparison reasons
    @Test
    public void findByBudgetItemAndType_NotTypeSafe_Test() {

        // given
        Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
        Budget budget = budgetRepository.findByPropertyAndStartDate(property, new LocalDate(2015, 01, 01));
        BudgetItem budgetItem = budget.getItems().first();

        assertThat(budgetItem.getValues().size()).isEqualTo(1);
        assertThat(budgetItem.getValues().first().getType()).isEqualTo(BudgetCalculationType.BUDGETED);

        // when
        List<BudgetItemValue> results = budgetItemValueRepository.fbBIandT(budgetItem, BudgetCalculationType.BUDGETED);

        // then
        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    public void findUniqueTest(){

        // given
        Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
        Budget budget = budgetRepository.findByPropertyAndStartDate(property, new LocalDate(2015, 01, 01));
        BudgetItem budgetItem = budget.getItems().first();

        // when
        BudgetItemValue result = budgetItemValueRepository.findUnique(budgetItem, new LocalDate(2015,01,01), BudgetCalculationType.BUDGETED);

        // then
        assertThat(result.getDate()).isEqualTo(new LocalDate(2015, 01, 01));

    }

}
