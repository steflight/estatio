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

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelType;
import org.incode.module.communications.dom.impl.commchannel.EmailAddress;
import org.incode.module.document.dom.impl.docs.Document;

import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.dnc.Invoice_email;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatus;

public abstract class InvoiceSummaryForPropertyDueDateStatus_sendByEmailAbstract extends InvoiceSummaryForPropertyDueDateStatus_sendAbstract {

    public InvoiceSummaryForPropertyDueDateStatus_sendByEmailAbstract(
            final InvoiceSummaryForPropertyDueDateStatus invoiceSummary,
            final String documentTypeReference) {
        super(invoiceSummary, documentTypeReference, CommunicationChannelType.EMAIL_ADDRESS);
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(contributed = Contributed.AS_ACTION)
    public InvoiceSummaryForPropertyDueDateStatus $$() throws IOException {

        final List<Tuple> tuples = tuplesToSend();
        for (Tuple tuple : tuples) {
            final Invoice invoice = tuple.getInvoice();
            final Document document = tuple.getDocument();

            final Invoice_email invoice_email = invoice_email(invoice);

            final EmailAddress emailAddress = invoice_email.default1$$(document);
            final String cc = invoice_email.default2$$(document);
            final String bcc = invoice_email.default3$$(document);
            final String message = invoice_email.default4$$();

            invoice_email.$$(document, emailAddress, cc, bcc, message);
        }
        return this.invoiceSummary;
    }

    public String disable$$() {
        return tuplesToSend().isEmpty()? "No documents available to be send by email": null;
    }

    @Override
    Predicate<Tuple> filter() {
        return Predicates.and(
                        tuple -> !exclude(tuple),
                        canBeSentByEmail()
                );
    }

    /**
     * Optional hook to allow subclasses to further restrict the documents that can be sent.
     */
    protected boolean exclude(final Tuple tuple) {
        return false;
    }

    private Predicate<Tuple> canBeSentByEmail() {
        return tuple -> {
            final Invoice_email emailMixin = invoice_email(tuple.getInvoice());
            final EmailAddress emailAddress = emailMixin.default1$$(tuple.getDocument());
            return emailAddress != null;
        };
    }

    private Invoice_email invoice_email(final Invoice invoice) {
        return factoryService.mixin(Invoice_email.class, invoice);
    }

    @Inject
    FactoryService factoryService;
}
