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
package org.incode.module.communications.dom.mixins;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.background.BackgroundService2;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.xactn.TransactionService;

import org.incode.module.communications.dom.impl.commchannel.PostalAddress;
import org.incode.module.communications.dom.impl.comms.Communication;
import org.incode.module.communications.dom.impl.comms.CommunicationRepository;
import org.incode.module.communications.dom.spi.CommHeaderForPrint;
import org.incode.module.communications.dom.spi.DocumentCommunicationSupport;
import org.incode.module.document.dom.DocumentModule;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentState;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;

/**
 * Provides the ability to send an print.
 */
@Mixin
public class Document_print {

    private final Document document;

    public Document_print(final Document document) {
        this.document = document;
    }

    public static class ActionDomainEvent extends DocumentModule.ActionDomainEvent<Document_print> { }

    @Action(
            semantics = SemanticsOf.NON_IDEMPOTENT,
            domainEvent = ActionDomainEvent.class
    )
    public Communication $$(
            @ParameterLayout(named = "to:")
            final PostalAddress toChannel) throws IOException {

        if(this.document.getState() == DocumentState.NOT_RENDERED) {
            // can't generate the comm yet, so schedule to try again shortly.
            backgroundService.executeMixin(Document_print.class, document).$$(toChannel);
            return null;
        }

        // create comm and correspondents
        final String subject = document.getName();
        final String atPath = document.getAtPath();
        final Communication communication = communicationRepository.createPostal(subject, atPath, toChannel);

        transactionService.flushTransaction();

        // attach this doc to communication
        paperclipRepository.attach(document, DocumentConstants.PAPERCLIP_ROLE_ENCLOSED, communication);

        return communication;
    }

    public String disable$$() {
        if (document.getState() != DocumentState.RENDERED) {
            return "Document not yet rendered";
        }
        if(choices0$$().isEmpty()) {
            return "Could not locate any postal address to sent to";
        }
        return null;
    }

    public PostalAddress default0$$() {
        return determinePrintHeader().getToDefault();
    }

    public Set<PostalAddress> choices0$$() {
        return determinePrintHeader().getToChoices();
    }

    private CommHeaderForPrint determinePrintHeader() {
        return queryResultsCache.execute(() -> {
            final CommHeaderForPrint commHeaderForPrint = new CommHeaderForPrint();
            if(documentCommunicationSupports != null) {
                for (DocumentCommunicationSupport commSupport : documentCommunicationSupports) {
                    commSupport.inferPrintHeaderFor(document, commHeaderForPrint);;
                }
            }
            return commHeaderForPrint;
        }, Document_print.class, "determinePrintHeader", document);
    }



    @Inject
    QueryResultsCache queryResultsCache;

    @Inject
    List<DocumentCommunicationSupport> documentCommunicationSupports;

    @Inject
    TransactionService transactionService;

    @Inject
    PaperclipRepository paperclipRepository;

    @Inject
    CommunicationRepository communicationRepository;

    @Inject
    BackgroundService2 backgroundService;

}
