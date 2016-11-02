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

import com.google.common.eventbus.Subscribe;

import org.apache.isis.applib.AbstractSubscriber;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

import org.incode.module.communications.dom.mixins.Document_email;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;
import org.incode.module.document.dom.impl.types.DocumentType;
import org.incode.module.document.dom.impl.types.DocumentTypeRepository;

import org.estatio.dom.invoice.Constants;
import org.estatio.dom.invoice.Invoice;

@DomainService(nature = NatureOfService.DOMAIN)
public class InvoiceEmailPolicyService extends AbstractSubscriber {

    @Subscribe
    public void on(Document_email.ActionDomainEvent ev) {

        switch (ev.getEventPhase()) {
        case DISABLE:
            final Document document = (Document) ev.getMixedIn();
            ev.disable(disableSendInvoiceNote(document));
            break;
        }
    }

    @Programmatic
    String disableSendInvoiceNote(final Document document) {

        // only applies to InvoiceNote documents
        final DocumentType documentType = documentTypeRepository.findByReference(Constants.DOC_TYPE_REF_INVOICE);
        if(document.getType() != documentType) {
            return null;
        }

        // if so, then find the Invoice it is attached to.
        Invoice invoice = determineAttachedTo(document);
        if(invoice == null) {
            return "Document is not attached to an Invoice";
        }

        if (invoice.getInvoiceNumber() == null) {
            return "Invoice does not yet have an invoice number";
        }

        // ok then
        return null;
    }

    private Invoice determineAttachedTo(final Document document) {
        final List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
        for (Paperclip paperclip : paperclips) {
            final Object attachedTo = paperclip.getAttachedTo();
            if(attachedTo instanceof Invoice) {
                return (Invoice) attachedTo;
            }
        }
        return null;
    }

    @Inject
    PaperclipRepository paperclipRepository;
    @Inject
    DocumentTypeRepository documentTypeRepository;

}
