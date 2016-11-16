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
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.email.EmailService;
import org.apache.isis.applib.services.factory.FactoryService;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannel;
import org.incode.module.communications.dom.impl.commchannel.EmailAddress;
import org.incode.module.communications.dom.impl.comms.Communication;
import org.incode.module.communications.dom.mixins.Document_email;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentAbstract;
import org.incode.module.document.dom.impl.docs.DocumentState;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;

import org.estatio.dom.EstatioDomainModule;
import org.estatio.dom.invoice.Constants;
import org.estatio.dom.invoice.Invoice;

/**
 * Provides the ability to send an email.
 */
@Mixin
public class Invoice_email {

    public static final int EMAIL_COVERING_NOTE_MULTILINE = 14;

    private final Invoice invoice;

    public Invoice_email(final Invoice invoice) {
        this.invoice = invoice;
    }

    public static class ActionDomainEvent extends EstatioDomainModule.ActionDomainEvent<Invoice_email> { }

    @Action(
            semantics = SemanticsOf.NON_IDEMPOTENT,
            domainEvent = ActionDomainEvent.class
    )
    @ActionLayout(cssClassFa = "at")
    @MemberOrder(name = "documents", sequence = "4.1")
    public Communication $$(
            final Document document,
            @ParameterLayout(named = "to:")
            final EmailAddress toChannel,
            @Parameter(
                    optionality = Optionality.OPTIONAL,
                    maxLength = CommunicationChannel.EmailType.MAX_LEN,
                    regexPattern = CommunicationChannel.EmailType.REGEX,
                    regexPatternReplacement = CommunicationChannel.EmailType.REGEX_DESC)
            @ParameterLayout(named = "cc:")
            final String cc,
            @Parameter(
                    optionality = Optionality.OPTIONAL,
                    maxLength = CommunicationChannel.EmailType.MAX_LEN,
                    regexPattern = CommunicationChannel.EmailType.REGEX,
                    regexPatternReplacement = CommunicationChannel.EmailType.REGEX_DESC)
            @ParameterLayout(named = "bcc:")
            final String bcc) throws IOException {

        // just delegate to Document_email to do the work.
        final Communication communication = document_email(document).$$(toChannel, cc, bcc);

        // now that a comm has been sent, also attach this document to the buyer and seller
        // that way, if the (temporary) invoice is subsequently deleted
        paperclipRepository.attach(document, "buyer", invoice.getBuyer());
        paperclipRepository.attach(document, "seller", invoice.getSeller());

        return communication;
    }

    private Document_email document_email(final Document document) {
        return factoryService.mixin(Document_email.class, document);
    }

    public String disable$$() {
        if (emailService == null || !emailService.isConfigured()) {
            return "Email service not configured";
        }
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
            final String reference = document.getType().getReference();
            if (!Constants.DOC_TYPE_REF_PRELIM.equals(reference) && !Constants.DOC_TYPE_REF_INVOICE.equals(reference)) {
                continue;
            }
            final Document_email document_email = document_email(document);
            if(document_email.disable$$() != null) {
                continue;
            }
            documents.add(document);
        }
        return documents;
    }

    public Set<EmailAddress> choices1$$(final Document document) {
        return document == null ? Collections.emptySet() : document_email(document).choices0$$();
    }

    // TODO: currently not properly supported by Isis, but does no harm
    @Programmatic
    public EmailAddress default1$$(final Document document) {
        return document == null ? null : document_email(document).default0$$();
    }

    // TODO: currently not properly supported by Isis, but does no harm
    @Programmatic
    public String default2$$(final Document document) {
        return document == null ? null : document_email(document).default1$$();
    }

    // TODO: currently not properly supported by Isis, but does no harm
    @Programmatic
    public String default3$$(final Document document) {
        return document == null ? null :document_email(document).default2$$();
    }


    @Inject
    FactoryService factoryService;

    @Inject
    PaperclipRepository paperclipRepository;

    @Inject
    EmailService emailService;


}
