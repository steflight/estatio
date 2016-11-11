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

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import org.apache.isis.applib.services.factory.FactoryService;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannel;
import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelType;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentState;

import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatus;

import lombok.Data;

public abstract class InvoiceSummaryForPropertyDueDateStatus_sendAbstract extends InvoiceSummaryForPropertyDueDateStatus_actionAbstract {

    @Data
    public static class Tuple {
        final Invoice invoice;
        final Document document;
    }

    private final CommunicationChannelType communicationChannelType;

    public InvoiceSummaryForPropertyDueDateStatus_sendAbstract(
            final InvoiceSummaryForPropertyDueDateStatus invoiceSummary,
            final String documentTypeReference,
            final CommunicationChannelType communicationChannelType) {
        super(invoiceSummary, documentTypeReference);
        this.communicationChannelType = communicationChannelType;
    }

    abstract Predicate<Tuple> filter();

    List<Tuple> tuplesToSend() {
        return tuplesToSend(filter());
    }

    private List<Tuple> tuplesToSend(Predicate<Tuple> filter) {
        final List<Tuple> tuples = Lists.newArrayList();
        final List<Invoice> invoices = invoiceSummary.getInvoices();
        for (Invoice invoice : invoices) {
            appendTuplesToSend(invoice, filter, tuples);
        }
        return tuples;
    }

    private void appendTuplesToSend(
            final Invoice invoice,
            final Predicate<Tuple> filter, final List<Tuple> tuples) {
        final CommunicationChannel sendTo = invoice.getSendTo();
        if (sendTo == null) {
            return;
        }

        final CommunicationChannelType channelType = sendTo.getType();
        if (channelType != communicationChannelType) {
            return;
        }

        final Document document = findMostRecentAttachedTo(invoice, getDocumentType());
        if(document == null) {
            return;
        }
        if(document.getState() == DocumentState.NOT_RENDERED) {
            return;
        }

        final Tuple tuple = new Tuple(invoice, document);
        if(!filter.apply(tuple)) {
            return;
        }
        tuples.add(tuple);
    }

    @Inject
    FactoryService factoryService;
}
