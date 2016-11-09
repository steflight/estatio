/*
 *
 *  Copyright 2012-2015 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.dom.budgeting.allocation;

import java.math.BigDecimal;
import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.UdoDomainRepositoryAndFactory;
import org.estatio.dom.budgeting.budgetitem.BudgetItem;
import org.estatio.dom.budgeting.keytable.KeyTable;
import org.estatio.dom.charge.Charge;

@DomainService(repositoryFor = PartitionItem.class, nature = NatureOfService.DOMAIN)
@DomainServiceLayout()
public class BudgetItemAllocationRepository extends UdoDomainRepositoryAndFactory<PartitionItem> {

    public BudgetItemAllocationRepository() {
        super(BudgetItemAllocationRepository.class, PartitionItem.class);
    }

    // //////////////////////////////////////

    public PartitionItem newPartitionItem(
            final Charge charge,
            final KeyTable keyTable,
            final BudgetItem budgetItem,
            final BigDecimal percentage) {
        PartitionItem partitionItem = newTransientInstance(PartitionItem.class);
        partitionItem.setCharge(charge);
        partitionItem.setKeyTable(keyTable);
        partitionItem.setBudgetItem(budgetItem);
        partitionItem.setPercentage(percentage.setScale(6, BigDecimal.ROUND_HALF_UP));
        persistIfNotAlready(partitionItem);
        return partitionItem;
    }

    public String validateNewPartitionItem(
            final Charge charge,
            final KeyTable keyTable,
            final BudgetItem budgetItem,
            final BigDecimal percentage
    ){
        if(findByChargeAndBudgetItemAndKeyTable(charge, budgetItem, keyTable) != null) {
            return "This schedule item already exists";
        }
        return null;
    }

    // //////////////////////////////////////

    @Action(semantics = SemanticsOf.SAFE, restrictTo = RestrictTo.PROTOTYPING)
    @ActionLayout()
    public List<PartitionItem> allPartitionItems() {
        return allInstances();
    }

    // //////////////////////////////////////

    @Programmatic
    public List<PartitionItem> findByBudgetItem(final BudgetItem budgetItem) {
        return allMatches("findByBudgetItem", "budgetItem", budgetItem);
    }

    @Programmatic
    public List<PartitionItem> findByKeyTable(final KeyTable keyTable) {
        return allMatches("findByKeyTable", "keyTable", keyTable);
    }

    @Programmatic
    public PartitionItem findByChargeAndBudgetItemAndKeyTable(final Charge charge, final BudgetItem budgetItem, final KeyTable keyTable) {
        return uniqueMatch("findByChargeAndBudgetItemAndKeyTable", "charge", charge, "budgetItem", budgetItem, "keyTable", keyTable);
    }

    @Programmatic
    public PartitionItem findOrCreatePartitionItem(final BudgetItem budgetItem, final Charge invoiceCharge, final KeyTable keyTable, final BigDecimal percentage){
        final PartitionItem partitionItem = findByChargeAndBudgetItemAndKeyTable(invoiceCharge, budgetItem, keyTable);
        if (partitionItem == null) {
            return newPartitionItem(invoiceCharge, keyTable, budgetItem, percentage);
        }
        return partitionItem;
    }

    @Programmatic
    public PartitionItem updateOrCreatePartitionItem(final BudgetItem budgetItem, final Charge invoiceCharge, final KeyTable keyTable, final BigDecimal percentage){
        final PartitionItem partitionItem = findByChargeAndBudgetItemAndKeyTable(invoiceCharge, budgetItem, keyTable);
        if (partitionItem == null) {
            return newPartitionItem(invoiceCharge, keyTable, budgetItem, percentage);
        } else {
            partitionItem.setPercentage(percentage);
        }
        return partitionItem;
    }

}
