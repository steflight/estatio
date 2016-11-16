package org.estatio.integtests.budget;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.budgetassignment.BudgetAssignmentService;
import org.estatio.dom.budgetassignment.override.BudgetOverride;
import org.estatio.dom.budgetassignment.override.BudgetOverrideForFlatRate;
import org.estatio.dom.budgetassignment.override.BudgetOverrideForMax;
import org.estatio.dom.budgetassignment.override.BudgetOverrideRepository;
import org.estatio.dom.budgetassignment.override.BudgetOverrideType;
import org.estatio.dom.budgetassignment.override.BudgetOverrideValue;
import org.estatio.dom.budgetassignment.viewmodels.BudgetCalculationResultViewModel;
import org.estatio.dom.budgetassignment.viewmodels.DetailedBudgetCalculationResultViewmodel;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budget.BudgetRepository;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculation;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationService;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationViewmodel;
import org.estatio.dom.budgeting.budgetcalculation.Status;
import org.estatio.dom.budgeting.keytable.KeyTable;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.charge.ChargeRepository;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForBudNl;
import org.estatio.fixture.budget.BudgetForBud;
import org.estatio.fixture.budget.PartitionItemsForBud;
import org.estatio.fixture.charge.ChargeRefData;
import org.estatio.fixture.lease.LeasesForBudNl;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class

BudgetCalculationScenarioTest extends EstatioIntegrationTest {

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    BudgetRepository budgetRepository;

    @Inject
    BudgetCalculationService budgetCalculationService;

    @Inject
    BudgetAssignmentService budgetAssignmentService;

    @Inject
    LeaseRepository leaseRepository;

    @Inject
    ChargeRepository chargeRepository;

    @Inject
    BudgetOverrideRepository budgetOverrideRepository;


    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new PartitionItemsForBud());
            }
        });
    }


    public static class Calculate extends BudgetCalculationScenarioTest {

        Property property;
        Budget budget;
        List<BudgetCalculation> calculations;
        List<BudgetOverride> overrides;
        List<BudgetOverrideValue> overrideValues;
        List<BudgetCalculationViewmodel> budgetCalculationViewmodels;
        List<BudgetCalculationResultViewModel> budgetCalculationResultViewModels;
        List<DetailedBudgetCalculationResultViewmodel> detailedBudgetCalculationResultViewmodels;

        @Before
        public void setup() {
            // given
            property = propertyRepository.findPropertyByReference(PropertyForBudNl.REF);
            budget = budgetRepository.findByPropertyAndStartDate(property, BudgetForBud.BUDGET_2015_START_DATE);
            //**IMPORTANT!** truncate keytable
            KeyTable key2 = budget.getKeyTables().last();
            key2.getItems().last().deleteBudgetKeyItem();
        }

        @Test
        public void CalculateAndAssign() throws Exception {
            calculate();
            detailedCalculation();
            persistedCalculations();
            calculateOverrides();

//            assignBudget();
//            assignBudgetWhenUpdated();
//            assignBudgetWhenAudited();
//            assignBudgetWhenAuditedAndUpdated();
        }

        public void calculate() throws Exception {

            // when
            budgetCalculationViewmodels = budgetCalculationService.getAllCalculations(budget);
            budgetCalculationResultViewModels = budgetAssignmentService.getAssignmentResults(budget);

            // then
            assertThat(budgetCalculationViewmodels.size()).isEqualTo(33);
            assertThat(budgetCalculationResultViewModels.size()).isEqualTo(20);
            assertThat(budgetedAmountFor(LeasesForBudNl.REF1, ChargeRefData.NL_SERVICE_CHARGE)).isEqualTo(new BigDecimal("1928.571437"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF1, ChargeRefData.NL_SERVICE_CHARGE2)).isEqualTo(new BigDecimal("964.285722"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF2, ChargeRefData.NL_SERVICE_CHARGE)).isEqualTo(new BigDecimal("2857.142847"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF2, ChargeRefData.NL_SERVICE_CHARGE2)).isEqualTo(new BigDecimal("1928.571417"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF3, ChargeRefData.NL_SERVICE_CHARGE)).isEqualTo(new BigDecimal("3785.714283"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF3, ChargeRefData.NL_SERVICE_CHARGE2)).isEqualTo(new BigDecimal("2892.857139"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF4, ChargeRefData.NL_SERVICE_CHARGE)).isEqualTo(new BigDecimal("11214.285719"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF4, ChargeRefData.NL_SERVICE_CHARGE2)).isEqualTo(new BigDecimal("10607.142861"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF4A, ChargeRefData.NL_SERVICE_CHARGE)).isEqualTo(new BigDecimal("4714.285719"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF4A, ChargeRefData.NL_SERVICE_CHARGE2)).isEqualTo(new BigDecimal("3857.142861"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF5, ChargeRefData.NL_SERVICE_CHARGE)).isEqualTo(new BigDecimal("5642.857155"));
            assertThat(budgetedAmountFor(LeasesForBudNl.REF5, ChargeRefData.NL_SERVICE_CHARGE2)).isEqualTo(new BigDecimal("4821.428583"));

        }

        private BigDecimal budgetedAmountFor(final String leaseReference, final String invoiceChargeReference){

            BigDecimal resultValue = BigDecimal.ZERO;

            for (BudgetCalculationResultViewModel result : resultsForLease(leaseReference, invoiceChargeReference)){
                resultValue = resultValue.add(result.getBudgetedValue());
            }

            return resultValue;
        }

        private List<BudgetCalculationResultViewModel> resultsForLease(final String leaseReference, final String invoiceChargeReference){
            return budgetCalculationResultViewModels.stream().filter(x ->x.getLeaseReference().equals(leaseReference) && x.getInvoiceCharge().equals(invoiceChargeReference)).collect(Collectors.toList());
        }

        public void detailedCalculation() throws Exception {

            String INCOMING_CHARGE_LABEL_1 = "NLD_INCOMING_CHARGE_1 Incoming Charge 1 (NLD) | budgeted 10000.00 | 100.00 % table1";
            String INCOMING_CHARGE_LABEL_2 = "NLD_INCOMING_CHARGE_2 Incoming Charge 2 (NLD) | budgeted 20000.00 | 80.00 % table1 | 20.00 % table2";
            String INCOMING_CHARGE_LABEL_3 = "NLD_INCOMING_CHARGE_3 Incoming Charge 3 (NLD) | budgeted 30000.00 | 90.00 % table1 | 10.00 % table2";

            // given
            Lease leaseForDago = leaseRepository.findLeaseByReference(LeasesForBudNl.REF4);

            // when
            budgetCalculationViewmodels = budgetCalculationService.getAllCalculations(budget);
            detailedBudgetCalculationResultViewmodels = budgetAssignmentService.getDetailedBudgetAssignmentResults(budget, leaseForDago);

            // then
            assertThat(detailedBudgetCalculationResultViewmodels.size()).isEqualTo(8);

            assertThat(detailedBudgetCalculationResultViewmodels.get(0).getBudgetedValue()).isEqualTo(new BigDecimal("1428.571430"));
            assertThat(detailedBudgetCalculationResultViewmodels.get(0).getIncomingCharge()).isEqualTo(INCOMING_CHARGE_LABEL_1);
            assertThat(detailedBudgetCalculationResultViewmodels.get(0).getInvoiceCharge()).isEqualTo(chargeRepository.findByReference(ChargeRefData.NL_SERVICE_CHARGE).getReference());

            assertThat(detailedBudgetCalculationResultViewmodels.get(1).getBudgetedValue()).isEqualTo(new BigDecimal("2285.714288"));
            assertThat(detailedBudgetCalculationResultViewmodels.get(1).getIncomingCharge()).isEqualTo(INCOMING_CHARGE_LABEL_2);
            assertThat(detailedBudgetCalculationResultViewmodels.get(1).getInvoiceCharge()).isEqualTo(chargeRepository.findByReference(ChargeRefData.NL_SERVICE_CHARGE).getReference());

            assertThat(detailedBudgetCalculationResultViewmodels.get(3).getBudgetedValue()).isEqualTo(new BigDecimal("3857.142861"));
            assertThat(detailedBudgetCalculationResultViewmodels.get(3).getIncomingCharge()).isEqualTo(INCOMING_CHARGE_LABEL_3);
            assertThat(detailedBudgetCalculationResultViewmodels.get(3).getInvoiceCharge()).isEqualTo(chargeRepository.findByReference(ChargeRefData.NL_SERVICE_CHARGE2).getReference());

            assertThat(detailedBudgetCalculationResultViewmodels.get(5).getBudgetedValue()).isEqualTo(new BigDecimal("2500.000000"));
            assertThat(detailedBudgetCalculationResultViewmodels.get(5).getIncomingCharge()).isEqualTo(INCOMING_CHARGE_LABEL_1);
            assertThat(detailedBudgetCalculationResultViewmodels.get(5).getInvoiceCharge()).isEqualTo(chargeRepository.findByReference(ChargeRefData.NL_SERVICE_CHARGE).getReference());

            assertThat(detailedBudgetCalculationResultViewmodels.get(7).getBudgetedValue()).isEqualTo(new BigDecimal("6750.000000"));
            assertThat(detailedBudgetCalculationResultViewmodels.get(7).getIncomingCharge()).isEqualTo(INCOMING_CHARGE_LABEL_3);
            assertThat(detailedBudgetCalculationResultViewmodels.get(7).getInvoiceCharge()).isEqualTo(chargeRepository.findByReference(ChargeRefData.NL_SERVICE_CHARGE2).getReference());

        }

        public void persistedCalculations() throws Exception {

            // when
            calculations = budgetCalculationService.calculatePersistedCalculations(budget);

            // then
            assertThat(calculations.size()).isEqualTo(33);

        }

        public void calculateOverrides() throws Exception {

            // given
            Lease leasePoison = leaseRepository.findLeaseByReference(LeasesForBudNl.REF1);
            Lease leaseMiracle = leaseRepository.findLeaseByReference(LeasesForBudNl.REF2);
            Charge invoiceCharge = chargeRepository.findByReference(ChargeRefData.NL_SERVICE_CHARGE);
            Charge incomingCharge = chargeRepository.findByReference(ChargeRefData.NL_INCOMING_CHARGE_1);

            // when
            overrideValues = budgetAssignmentService.calculateOverrideValues(budget);

            // then
            assertThat(overrideValues.size()).isEqualTo(2);
            assertThat(budgetOverrideRepository.findByLease(leasePoison).size()).isEqualTo(1);
            BudgetOverrideForMax oPoison = (BudgetOverrideForMax) budgetOverrideRepository.findByLease(leasePoison).get(0);
            assertThat(oPoison.getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(oPoison.getInvoiceCharge()).isEqualTo(invoiceCharge);
            assertThat(oPoison.getIncomingCharge()).isEqualTo(incomingCharge);
            assertThat(oPoison.getStartDate()).isEqualTo(budget.getStartDate());
            assertThat(oPoison.getEndDate()).isNull();
            assertThat(oPoison.getReason()).isEqualTo(BudgetOverrideType.CEILING.reason);
            assertThat(oPoison.getMaxValue()).isEqualTo(new BigDecimal("350.00"));
            assertThat(oPoison.getValues().size()).isEqualTo(1);
            assertThat(oPoison.getValues().first().getValue()).isEqualTo(new BigDecimal("350.00"));
            assertThat(oPoison.getValues().first().getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(oPoison.getValues().first().getStatus()).isEqualTo(Status.NEW);

            assertThat(budgetOverrideRepository.findByLease(leaseMiracle).size()).isEqualTo(1);
            BudgetOverrideForFlatRate oMiracle = (BudgetOverrideForFlatRate) budgetOverrideRepository.findByLease(leaseMiracle).get(0);
            assertThat(oMiracle.getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(oMiracle.getInvoiceCharge()).isEqualTo(invoiceCharge);
            assertThat(oMiracle.getIncomingCharge()).isNull();
            assertThat(oMiracle.getStartDate()).isEqualTo(budget.getStartDate());
            assertThat(oMiracle.getEndDate()).isNull();
            assertThat(oMiracle.getReason()).isEqualTo(BudgetOverrideType.FLATRATE.reason);
            assertThat(oMiracle.getValuePerM2()).isEqualTo(new BigDecimal("12.50"));
            assertThat(oMiracle.getWeightedArea()).isEqualTo(new BigDecimal("90.00"));
            assertThat(oMiracle.getValues().size()).isEqualTo(1);
            assertThat(oMiracle.getValues().first().getValue()).isEqualTo(new BigDecimal("1125.0000"));
            assertThat(oMiracle.getValues().first().getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(oMiracle.getValues().first().getStatus()).isEqualTo(Status.NEW);

        }

        public void assignBudget() throws Exception {

        }

        public void assignBudgetWhenUpdated() throws Exception {

        }

        public void assignBudgetWhenAudited() throws Exception {

        }

        public void assignBudgetWhenAuditedAndUpdated() throws Exception {

        }

    }

}
