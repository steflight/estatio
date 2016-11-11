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
package org.estatio.dom.invoice.dnc;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.xactn.TransactionService;

import org.incode.module.communications.dom.impl.commchannel.PostalAddress;
import org.incode.module.communications.dom.impl.comms.Communication;
import org.incode.module.communications.dom.impl.comms.CommunicationRepository;
import org.incode.module.communications.dom.mixins.Document_print;
import org.incode.module.document.dom.DocumentModule;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentAbstract;
import org.incode.module.document.dom.impl.docs.DocumentState;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;

import org.estatio.dom.invoice.Invoice;

/**
 * Provides the ability to send an print.
 */
@Mixin
public class Invoice_print {

    private final Invoice invoice;

    public Invoice_print(final Invoice invoice) {
        this.invoice = invoice;
    }

    public static class ActionDomainEvent extends DocumentModule.ActionDomainEvent<Invoice_print> { }

    @Action(
            semantics = SemanticsOf.NON_IDEMPOTENT,
            domainEvent = ActionDomainEvent.class
    )
    @ActionLayout(cssClassFa = "print")
    public Communication $$(
            final Document document,
            @ParameterLayout(named = "to:")
            final PostalAddress toChannel) throws IOException {

        // just delegate to Document_email to do the work.
        final Communication communication = document_print(document).$$(toChannel);

        // now that a comm has been sent, also attach this document to the buyer and seller
        // that way, if the (temporary) invoice is subsequently deleted
        paperclipRepository.attach(document, "buyer", invoice.getBuyer());
        paperclipRepository.attach(document, "seller", invoice.getSeller());

        return communication;
    }

    private Document_print document_print(final Document document) {
        return factoryService.mixin(Document_print.class, document);
    }

    public String disable$$() {
        if(choices0$$().isEmpty()) {
            return "No documents available to send";
        }
        return null;
    }

    public Document default0$$() {
        final List<Document> documents = choices0$$();
        return documents.size() == 1 ? documents.get(0): null;
    }

    public List<Document> choices0$$() {
        final List<Paperclip> paperclips = paperclipRepository.findByAttachedTo(invoice);
        final List<Document> documents = Lists.newArrayList();
        for (Paperclip paperclip : paperclips) {
            final DocumentAbstract documentAbs = paperclip.getDocument();
            if (!(documentAbs instanceof Document)) {
                continue;
            }
            final Document document = (Document) documentAbs;
            if (document.getState() != DocumentState.RENDERED) {
                continue;
            }
            final Document_print document_print = document_print(document);
            if(document_print.disable$$() != null) {

                // equivalent of all this stuff...

//                final CommHeaderForPrint printHeader = determinePrintHeader(document);
//                final Set<PostalAddress> toChoices = printHeader.getToChoices();
//                if (toChoices.isEmpty()) {
//                    // ... and there are choices to send to
//                    continue;
//                }
//                final String disabledReason = printHeader.getDisabledReason();
//                if (disabledReason != null) {
//                    // ... and not otherwise disabled
//                    continue;
//                }

                continue;
            }
            documents.add(document);
        }
        return documents;
    }

    public Set<PostalAddress> choices1$$(final Document document) {
        return document == null ? Collections.emptySet() : document_print(document).choices0$$();
    }

    // TODO: currently not properly supported by Isis, but does not harm
    public PostalAddress default1$$(final Document document) {
        return document == null ? null : document_print(document).default0$$();
    }

//    private CommHeaderForPrint determinePrintHeader(final Document document) {
//        return queryResultsCache.execute(() -> {
//            final CommHeaderForPrint commHeaderForPrint = new CommHeaderForPrint();
//            invoiceDocumentCommunicationSupport.inferPrintHeaderFor(invoice, document, commHeaderForPrint);;
//            return commHeaderForPrint;
//        }, Invoice_print.class, "determinePrintHeader", document);
//    }


    @Inject
    FactoryService factoryService;

    @Inject
    QueryResultsCache queryResultsCache;

//    @Inject
//    UNUSED_InvoiceDocumentCommunicationSupport invoiceDocumentCommunicationSupport;

    @Inject
    TransactionService transactionService;

    @Inject
    PaperclipRepository paperclipRepository;

    @Inject
    CommunicationRepository communicationRepository;

//    @Inject
//    BackgroundService2 backgroundService;

}
