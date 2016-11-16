/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
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
package org.estatio.dom.documents.binders;

import java.util.Arrays;
import java.util.List;

import org.isisaddons.module.security.dom.tenancy.WithApplicationTenancy;
import org.isisaddons.module.stringinterpolator.dom.StringInterpolatorService;

import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.party.Party;

/**
 * Creates a dataModel to be used with {@link StringInterpolatorService} for both content and subject;
 * requires domain object to implement {@link WithApplicationTenancy}.
 *
 * The input object is used for 'attachTo'.
 */
public class BinderForReportServerForInvoiceAttachToInvoiceAndBuyerAndSeller extends BinderForReportServerAbstract<Invoice> {

    public BinderForReportServerForInvoiceAttachToInvoiceAndBuyerAndSeller() {
        super(Invoice.class);
    }

    protected List<Object> determineAttachTo(final Invoice invoice) {
        final Party buyer = invoice.getBuyer();
        final Party seller = invoice.getSeller();
        return Arrays.asList(buyer, seller, invoice);
    }
}
