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
package org.estatio.dom.invoice.viewmodel.dnc;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Mixin;

import org.estatio.dom.invoice.Constants;
import org.estatio.dom.invoice.dnc.InvoicePrintAndEmailPolicyService;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatus;

@Mixin
public class InvoiceSummaryForPropertyDueDateStatus_sendByEmailInvoiceNotes extends
        InvoiceSummaryForPropertyDueDateStatus_sendByEmailAbstract {

    public InvoiceSummaryForPropertyDueDateStatus_sendByEmailInvoiceNotes(final InvoiceSummaryForPropertyDueDateStatus invoiceSummary) {
        super(invoiceSummary, Constants.DOC_TYPE_REF_INVOICE);
    }

    @Override
    protected boolean exclude(final Tuple tuple) {
        return invoiceEmailPolicyService.disableSendInvoiceNote(tuple.getInvoice(), tuple.getDocument()) != null;
    }

    @Inject
    InvoicePrintAndEmailPolicyService invoiceEmailPolicyService;
}
