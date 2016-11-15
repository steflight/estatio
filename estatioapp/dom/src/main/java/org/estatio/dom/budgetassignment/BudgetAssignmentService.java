package org.estatio.dom.budgetassignment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

import org.estatio.dom.asset.Unit;
import org.estatio.dom.budgetassignment.viewmodels.BudgetAssignmentResult;
import org.estatio.dom.budgetassignment.viewmodels.DetailedBudgetAssignmentResult;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculation;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationRepository;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationService;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationViewmodel;
import org.estatio.dom.budgeting.budgetcalculation.Status;
import org.estatio.dom.budgeting.budgetitem.BudgetItem;
import org.estatio.dom.budgeting.keytable.KeyTable;
import org.estatio.dom.budgeting.partioning.PartitionItem;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.dom.lease.LeaseStatus;
import org.estatio.dom.lease.Occupancy;
import org.estatio.dom.lease.OccupancyRepository;

@DomainService(nature = NatureOfService.DOMAIN)
public class BudgetAssignmentService {

    public List<DetailedBudgetAssignmentResult> getDetailedBudgetAssignmentResults(final Budget budget, final Lease lease){
        List<DetailedBudgetAssignmentResult> results = new ArrayList<>();

        for (Occupancy occupancy : lease.getOccupancies()) {
            if (occupancy.getInterval().overlaps(budget.getInterval())) {

                for (BudgetCalculationViewmodel calculationResult : calculationResults(budget, occupancy.getUnit())){

                    if (calculationResult.getCalculationType() == BudgetCalculationType.BUDGETED) {
                        results.add(
                                new DetailedBudgetAssignmentResult(
                                        occupancy.getUnit(),
                                        calculationResult.getPartitionItem().getBudgetItem().getCharge(),
                                        getRowLabelLastPart(calculationResult.getPartitionItem().getBudgetItem()),
                                        calculationResult.getValue(),
                                        calculationResult.getKeyItem().getKeyTable(),
                                        calculationResult.getPartitionItem().getCharge()
                                )
                        );
                    }

                }

            }
        }

        return results;
    }

    private BigDecimal getTotalBudgetedValue(final BudgetItem budgetItem){
        BigDecimal returnValue = BigDecimal.ZERO;
        List<BudgetCalculationViewmodel> resultsForItem =
                budgetCalculationService.getCalculations(budgetItem.getBudget()).stream().filter(x -> x.getPartitionItem().getBudgetItem().equals(budgetItem)).collect(Collectors.toList()
        );
        for (BudgetCalculationViewmodel bcResult : resultsForItem){
            if (bcResult.getValue() != null && bcResult.getCalculationType() == BudgetCalculationType.BUDGETED) {
                returnValue = returnValue.add(bcResult.getValue());
            }
        }
        return returnValue.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String getRowLabelLastPart(final BudgetItem budgetItem){

        String concatString = new String();

        concatString = concatString
                .concat(" | budgeted ")
                .concat(budgetItem.getBudgetedValue().toString());

        List<RowLabelHelper> helpers = new ArrayList<>();
        for (PartitionItem partitionItem : budgetItem.getPartitionItems()){
            helpers.add(new RowLabelHelper(partitionItem.getPercentage(), partitionItem.getKeyTable()));
        }
        Collections.sort(helpers);
        for (RowLabelHelper helper : helpers){
            concatString =
                    concatString
                    .concat(" | ")
                    .concat(helper.percentage)
                    .concat(helper.keyTableName);
        }
        return concatString;

    }

    public List<BudgetAssignmentResult> getAssignmentResults(final Budget budget){
        List<BudgetAssignmentResult> results = new ArrayList<>();
        for (Lease lease : leaseRepository.findLeasesByProperty(budget.getProperty())){
           results.addAll(getAssignmentResults(budget, lease));
        }
        return results;
    }

    private List<BudgetAssignmentResult> getAssignmentResults(final Budget budget, final Lease lease){
        List<BudgetAssignmentResult> results = new ArrayList<>();
        // TODO: this is an extra filter because currently occupancies can outrun terminated leases
        if (lease.getStatus() != LeaseStatus.TERMINATED) {
            for (Occupancy occupancy : lease.getOccupancies()) {
                List<BudgetCalculationViewmodel> calculationResultsForOccupancy = new ArrayList<>();
                if (occupancy.getInterval().overlaps(budget.getInterval())) {
                    calculationResultsForOccupancy.addAll(calculationResults(budget, occupancy.getUnit()));
                }
                results.addAll(createFromCalculationResults(lease, occupancy.getUnit(), calculationResultsForOccupancy));
            }
        }
        return results;
    }

    private List<BudgetCalculationViewmodel> calculationResults(final Budget budget, final Unit u){
        return Lists.newArrayList(
                budgetCalculationService.getCalculations(budget).stream().filter(x -> x.getKeyItem().getUnit().equals(u)).collect(Collectors.toList())
        );
    }

    private List<BudgetAssignmentResult> createFromCalculationResults(final Lease lease, final Unit unit, final List<BudgetCalculationViewmodel> calculationResultsForLease){
        List<BudgetAssignmentResult> assignmentResults = new ArrayList<>();
        for (BudgetCalculationViewmodel calculationResult : calculationResultsForLease){
            List<BudgetAssignmentResult> filteredByChargeAndKeyTable = assignmentResults.stream()
                    .filter(x -> x.getInvoiceCharge().equals(calculationResult.getPartitionItem().getCharge().getReference()))
                    .filter(x -> x.getKeyTable().equals(calculationResult.getPartitionItem().getKeyTable().getName()))
                    .collect(Collectors.toList());
            if (filteredByChargeAndKeyTable.size()>0){
                filteredByChargeAndKeyTable.get(0).add(calculationResult);
            } else {
                assignmentResults.add(new BudgetAssignmentResult(
                    lease,
                    unit,
                    calculationResult.getKeyItem().getKeyTable(),
                    calculationResult.getPartitionItem().getCharge(),
                    calculationResult.getValue()
                ));
            }
        }
        return assignmentResults;
    }


    public List<BudgetCalculationLink> assignBudgetCalculations(final Budget budget) {

        removeCurrentlyAssignedCalculations(budget);

        List<BudgetCalculationLink> result = new ArrayList<>();

        for (Charge invoiceCharge : budget.getInvoiceCharges()) {

            List<BudgetCalculation> calculationsForCharge = budgetCalculationRepository.findByBudgetAndCharge(budget, invoiceCharge);

            for (Occupancy occupancy : occupancyRepository.occupanciesByPropertyAndInterval(budget.getProperty(), budget.getInterval())) {

                List<BudgetCalculation> budgetCalculationsForOccupancy = calculationsForOccupancy(calculationsForCharge, occupancy);

                // find or create service charge item
                if (budgetCalculationsForOccupancy.size()>0){

                    ServiceChargeItem serviceChargeItem = serviceChargeItemRepository.findOrCreateServiceChargeItem(occupancy, invoiceCharge);

                }

            }

        }

        return result;
    }

    private void removeCurrentlyAssignedCalculations(final Budget budget) {
        for (BudgetCalculation calculation : budgetCalculationRepository.findByBudgetAndStatus(budget, Status.ASSIGNED)){

            for (BudgetCalculationLink link : budgetCalculationLinkRepository.findByBudgetCalculation(calculation)){
                link.remove();
            }

            calculation.remove();
        }
    }

    private List<BudgetCalculation> calculationsForOccupancy(final List<BudgetCalculation> calculationList, final Occupancy occupancy){
        List<BudgetCalculation> result = new ArrayList<>();

        for (BudgetCalculation budgetCalculation : calculationList){

            if (budgetCalculation.getKeyItem().getUnit().equals(occupancy.getUnit())){
                result.add(budgetCalculation);
            }
        }

        return result;
    }

    List<Occupancy> associatedOccupancies(final BudgetCalculation calculation){
        return occupancyRepository.occupanciesByUnitAndInterval(calculation.getKeyItem().getUnit(), calculation.getBudget().getInterval());
    }


    private class RowLabelHelper implements Comparable<RowLabelHelper> {

        RowLabelHelper(
                final BigDecimal percentage,
                final KeyTable keyTable){
            this.percentage = percentage.setScale(2, BigDecimal.ROUND_HALF_UP).toString().concat(" % ");
            this.keyTableName = keyTable.getName();
        }

        String percentage;

        String keyTableName;

        @Override public int compareTo(final RowLabelHelper o) {
            return this.keyTableName.compareTo(o.keyTableName);
        }

    }

    @Inject
    BudgetCalculationRepository budgetCalculationRepository;

    @Inject
    private BudgetCalculationService budgetCalculationService;

    @Inject
    private BudgetCalculationLinkRepository budgetCalculationLinkRepository;

    @Inject
    private OccupancyRepository occupancyRepository;

    @Inject
    private ServiceChargeItemRepository serviceChargeItemRepository;

    @Inject
    private LeaseRepository leaseRepository;

}
