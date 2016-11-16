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

package org.estatio.dom.budgetassignment;

import org.junit.Test;

import org.incode.module.unittestsupport.dom.bean.AbstractBeanPropertiesTest;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.lease.Occupancy;

public class ServiceChargeItemTest {

    public static class BeanProperties extends AbstractBeanPropertiesTest {

        @Test
        public void test() {
            final ServiceChargeItem pojo = new ServiceChargeItem();
            newPojoTester()
                    .withFixture(pojos(Occupancy.class, Occupancy.class))
                    .withFixture(pojos(Charge.class, Charge.class))
                    .exercise(pojo);
        }

    }

}
