/*
 * Copyright 2015 Yodo Int. Projects and Consultancy
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.estatio.dom.budgeting.budgetcalculation;

import org.junit.Test;

import org.incode.module.base.dom.testing.AbstractBeanPropertiesTest;

import org.estatio.dom.asset.Unit;
import org.estatio.dom.budgeting.ChargeForTesting;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.keyitem.KeyItem;
import org.estatio.dom.budgeting.partioning.PartitionItem;
import org.estatio.dom.charge.Charge;

public class BudgetCalculationTest {

    public static class BeanProperties extends AbstractBeanPropertiesTest {

        @Test
        public void test() {
            final BudgetCalculation pojo = new BudgetCalculation();
            newPojoTester()
                    .withFixture(pojos(PartitionItem.class, PartitionItem.class))
                    .withFixture(pojos(KeyItem.class, KeyItem.class))
                    .withFixture(pojos(Charge.class, ChargeForTesting.class))
                    .withFixture(pojos(Unit.class, Unit.class))
                    .withFixture(pojos(Budget.class, Budget.class))
                    .exercise(pojo);
        }

    }

}
