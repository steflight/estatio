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

package org.estatio.dom.budgetassignment.override;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.incode.module.base.dom.testing.AbstractBeanPropertiesTest;

import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.lease.Lease;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetOverrideTest {

    public static class BeanProperties extends AbstractBeanPropertiesTest {

        @Test
        public void testFixed() {
            final BudgetOverrideForFixed pojo = new BudgetOverrideForFixed();
            newPojoTester()
                    .withFixture(pojos(Lease.class, Lease.class))
                    .withFixture(pojos(Charge.class, Charge.class))
                    .exercise(pojo);
        }

        @Test
        public void testMax() {
            final BudgetOverrideForMax pojo = new BudgetOverrideForMax();
            newPojoTester()
                    .withFixture(pojos(Lease.class, Lease.class))
                    .withFixture(pojos(Charge.class, Charge.class))
                    .exercise(pojo);
        }

        @Test
        public void testFlatRate() {
            final BudgetOverrideForFlatRate pojo = new BudgetOverrideForFlatRate();
            newPojoTester()
                    .withFixture(pojos(Lease.class, Lease.class))
                    .withFixture(pojos(Charge.class, Charge.class))
                    .exercise(pojo);
        }

    }

    LocalDate budgetStartDate;
    BigDecimal valueCalculatedByBudget;
    BudgetOverrideCalculationRepository budgetOverrideCalculationRepository;
    BudgetOverrideCalculation budgetOverrideCalculation;

    @Before
    public void setup() {
        budgetOverrideCalculation = new BudgetOverrideCalculation();
        budgetOverrideCalculationRepository = new BudgetOverrideCalculationRepository(){
            @Override
            public BudgetOverrideCalculation newBudgetOverrideCalculation(
                    final BigDecimal value,
                    final BudgetOverride budgetOverride,
                    final BudgetCalculationType type){
                return budgetOverrideCalculation;
            }
        };
    }

    public static class CalculateForMax extends BudgetOverrideTest {

        BudgetOverrideForMax override;
        BigDecimal maxValue;

        @Test
        public void calculateTest() {
            // given
            valueCalculatedByBudget = new BigDecimal("1000.00");
            override = new BudgetOverrideForMax(){
                @Override
                BigDecimal getCalculatedValueByBudget(final LocalDate budgetStartDate, final BudgetCalculationType type){
                    return valueCalculatedByBudget;
                }
            };
            override.budgetOverrideCalculationRepository = budgetOverrideCalculationRepository;
            budgetStartDate = new LocalDate(2015, 01, 01);

            // when
            maxValue = new BigDecimal("1000.00");
            override.setMaxValue(maxValue);
            List<BudgetOverrideCalculation> calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(0);

            // and when
            maxValue = new BigDecimal("999.99");
            override.setMaxValue(maxValue);
            calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(2);

            // and when
            override.setType(BudgetCalculationType.BUDGETED);
            calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(1);

            // and when
            override.setType(BudgetCalculationType.AUDITED);
            calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(1);

        }

    }

    public static class CalculateForFixed extends BudgetOverrideTest {

        BudgetOverrideForFixed override;
        BigDecimal fixedValue;

        @Test
        public void calculateTest() {
            // given
            fixedValue = new BigDecimal("1234.45");
            override = new BudgetOverrideForFixed(){
                @Override
                BigDecimal getCalculatedValueByBudget(final LocalDate budgetStartDate, final BudgetCalculationType type){
                    return valueCalculatedByBudget;
                }
            };
            override.budgetOverrideCalculationRepository = budgetOverrideCalculationRepository;
            override.setFixedValue(fixedValue);
            budgetStartDate = new LocalDate(2015, 01, 01);

            // when
            List<BudgetOverrideCalculation> calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(2);

            // and when
            override.setType(BudgetCalculationType.BUDGETED);
            calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(1);

            // and when
            override.setType(BudgetCalculationType.AUDITED);
            calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(1);

        }

    }

    public static class CalculateForFlatRate extends BudgetOverrideTest {

        BudgetOverrideForFlatRate override;
        BigDecimal valuePerM2;
        BigDecimal area;

        @Test
        public void calculateTest() {
            // given
            valuePerM2 = new BigDecimal("10.00");
            area = new BigDecimal("2.5");
            override = new BudgetOverrideForFlatRate(){
                @Override
                BigDecimal getCalculatedValueByBudget(final LocalDate budgetStartDate, final BudgetCalculationType type){
                    return valueCalculatedByBudget;
                }
            };
            override.budgetOverrideCalculationRepository = budgetOverrideCalculationRepository;
            override.setValuePerM2(valuePerM2);
            override.setWeightedArea(area);
            budgetStartDate = new LocalDate(2015, 01, 01);

            // when
            List<BudgetOverrideCalculation> calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(2);

            // and when
            override.setType(BudgetCalculationType.BUDGETED);
            calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(1);

            // and when
            override.setType(BudgetCalculationType.AUDITED);
            calculations = override.calculate(budgetStartDate);

            // then
            assertThat(calculations.size()).isEqualTo(1);

        }

    }

}
