package org.estatio.dom.budgeting.budgetcalculation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

import org.estatio.dom.UdoDomainRepositoryAndFactory;
import org.estatio.dom.asset.Unit;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budgetitem.BudgetItem;
import org.estatio.dom.budgeting.keyitem.KeyItem;
import org.estatio.dom.budgeting.partioning.PartitionItem;
import org.estatio.dom.charge.Charge;

@DomainService(repositoryFor = BudgetCalculation.class, nature = NatureOfService.DOMAIN)
public class BudgetCalculationRepository extends UdoDomainRepositoryAndFactory<BudgetCalculation> {

    public BudgetCalculationRepository() {
        super(BudgetCalculationRepository.class, BudgetCalculation.class);
    }

    public BudgetCalculation updateOrCreateTemporaryBudgetCalculation(
            final PartitionItem partitionItem,
            final KeyItem keyItem,
            final BigDecimal value,
            final BudgetCalculationType calculationType){

        BudgetCalculation existingCalculation = findUnique(partitionItem, keyItem, BudgetCalculationStatus.TEMPORARY, calculationType);

        if (existingCalculation != null) {
            existingCalculation.setValue(value);
            return existingCalculation;
        }

        return createBudgetCalculation(partitionItem, keyItem, value, calculationType, BudgetCalculationStatus.TEMPORARY);
    }

    public BudgetCalculation createBudgetCalculation(
            final PartitionItem partitionItem,
            final KeyItem keyItem,
            final BigDecimal value,
            final BudgetCalculationType calculationType,
            final BudgetCalculationStatus status){

        BudgetCalculation budgetCalculation = newTransientInstance(BudgetCalculation.class);
        budgetCalculation.setPartitionItem(partitionItem);
        budgetCalculation.setKeyItem(keyItem);
        budgetCalculation.setValue(value);
        budgetCalculation.setCalculationType(calculationType);
        budgetCalculation.setStatus(status);
        budgetCalculation.setBudget(partitionItem.getBudget());
        budgetCalculation.setInvoiceCharge(partitionItem.getCharge());
        budgetCalculation.setIncomingCharge(partitionItem.getBudgetItem().getCharge());
        budgetCalculation.setUnit(keyItem.getUnit());

        persist(budgetCalculation);

        return budgetCalculation;
    }

    public BudgetCalculation updateOrCreateAssignedFromTemporary(final BudgetCalculation calculation){
        BudgetCalculation existingAssignedCalculation = findUnique(calculation.getPartitionItem(), calculation.getKeyItem(), BudgetCalculationStatus.ASSIGNED, calculation.getCalculationType());
        if (existingAssignedCalculation == null) {
            return createBudgetCalculation(calculation.getPartitionItem(), calculation.getKeyItem(), calculation.getValue(), calculation.getCalculationType(), BudgetCalculationStatus.ASSIGNED);
        } else {
            existingAssignedCalculation.setValue(calculation.getValue());
            return existingAssignedCalculation;
        }
    }

    public BudgetCalculation findUnique(
            final PartitionItem partitionItem,
            final KeyItem keyItem,
            final BudgetCalculationStatus calculationStatus,
            final BudgetCalculationType calculationType
            ){
        return uniqueMatch(
                "findUnique",
                "partitionItem", partitionItem,
                "keyItem", keyItem,
                "status", calculationStatus,
                "calculationType", calculationType);
    }

    public List<BudgetCalculation> findByPartitionItemAndCalculationType(PartitionItem partitionItem, BudgetCalculationType calculationType) {
        return allMatches("findByPartitionItemAndCalculationType", "partitionItem", partitionItem, "calculationType", calculationType);
    }

    public List<BudgetCalculation> findByPartitionItem(
            final PartitionItem partitionItem
    ){
        return allMatches("findByPartitionItem", "partitionItem", partitionItem);
    }

    public List<BudgetCalculation> findByPartitionItemAndStatus(final PartitionItem partitionItem, final BudgetCalculationStatus status) {
        return allMatches("findByPartitionItemAndStatus", "partitionItem", partitionItem, "status", status);
    }

    public List<BudgetCalculation> findByPartitionItemAndStatusAndCalculationType(final PartitionItem partitionItem, final BudgetCalculationStatus status, final BudgetCalculationType calculationType) {
        return allMatches("findByPartitionItemAndStatusAndCalculationType", "partitionItem", partitionItem, "status", status, "calculationType", calculationType);
    }

    public List<BudgetCalculation> allBudgetCalculations() {
        return allInstances();
    }

    public List<BudgetCalculation> findByBudgetAndCharge(final Budget budget, final Charge charge) {
        List<BudgetCalculation> result = new ArrayList<>();
        for (BudgetItem budgetItem : budget.getItems()){
            for (PartitionItem allocation : budgetItem.getPartitionItems()){
                if (allocation.getCharge().equals(charge)) {
                    result.addAll(findByPartitionItem(allocation));
                }
            }
        }
        return result;
    }

    public List<BudgetCalculation> findByBudget(final Budget budget) {
        List<BudgetCalculation> result = new ArrayList<>();
        for (BudgetItem item : budget.getItems()){

            result.addAll(findByBudgetItemAndCalculationType(item, BudgetCalculationType.AUDITED));
            result.addAll(findByBudgetItemAndCalculationType(item, BudgetCalculationType.BUDGETED));

        }
        return result;
    }

    public List<BudgetCalculation> findByBudgetAndCalculationType(final Budget budget, final BudgetCalculationType calculationType) {
        List<BudgetCalculation> result = new ArrayList<>();
        for (BudgetItem item : budget.getItems()){

            result.addAll(findByBudgetItemAndCalculationType(item, calculationType));

        }
        return result;
    }

    public List<BudgetCalculation> findByBudgetItemAndCalculationType(final BudgetItem budgetItem, final BudgetCalculationType calculationType) {

        List<BudgetCalculation> result = new ArrayList<>();
        for (PartitionItem allocation : budgetItem.getPartitionItems()) {

            result.addAll(findByPartitionItemAndCalculationType(allocation, calculationType));

        }
        return result;
    }

    public List<BudgetCalculation> findByBudgetAndStatus(final Budget budget, final BudgetCalculationStatus status) {
        List<BudgetCalculation> result = new ArrayList<>();
        for (BudgetItem item : budget.getItems()){

            result.addAll(findByBudgetItemAndStatus(item, status));

        }
        return result;
    }

    public List<BudgetCalculation> findByBudgetItemAndStatus(final BudgetItem budgetItem, final BudgetCalculationStatus status) {
        List<BudgetCalculation> result = new ArrayList<>();
        for (PartitionItem allocation : budgetItem.getPartitionItems()) {

            result.addAll(findByPartitionItemAndStatus(allocation, status));

        }
        return result;
    }

    public List<BudgetCalculation> findByBudgetAndStatusAndCalculationType(final Budget budget, final BudgetCalculationStatus status, final BudgetCalculationType calculationType) {
        List<BudgetCalculation> result = new ArrayList<>();
        for (BudgetItem item : budget.getItems()){

            result.addAll(findByBudgetItemAndStatusAndCalculationType(item, status, calculationType));

        }
        return result;
    }

    public List<BudgetCalculation> findByBudgetItemAndStatusAndCalculationType(final BudgetItem budgetItem, final BudgetCalculationStatus status, final BudgetCalculationType calculationType) {
        List<BudgetCalculation> result = new ArrayList<>();
        for (PartitionItem allocation : budgetItem.getPartitionItems()) {

            result.addAll(findByPartitionItemAndStatusAndCalculationType(allocation, status, calculationType));



        }
        return result;
    }

    public List<BudgetCalculation> findByBudgetAndUnitAndInvoiceChargeAndType(final Budget budget, final Unit unit, final Charge invoiceCharge, final BudgetCalculationType type) {
        return allMatches("findByBudgetAndUnitAndInvoiceChargeAndType", "budget", budget, "unit", unit, "invoiceCharge", invoiceCharge, "type", type);
    }
}

