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
package org.estatio.integtests.invoice.viewmodel;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.clock.ClockService;

import org.incode.module.communications.dom.impl.commchannel.EmailAddress;
import org.incode.module.communications.dom.impl.comms.Communication;
import org.incode.module.communications.dom.impl.comms.CommunicationState;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentAbstract;
import org.incode.module.document.dom.impl.docs.DocumentSort;
import org.incode.module.document.dom.impl.docs.DocumentState;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;
import org.incode.module.document.dom.impl.types.DocumentType;
import org.incode.module.document.dom.impl.types.DocumentTypeRepository;

import org.estatio.dom.invoice.Constants;
import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatus;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatusRepository;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote_communication;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote_communicationState;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote_document;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote_documentState;
import org.estatio.dom.invoice.viewmodel.dnc.InvoiceSummaryForPropertyDueDateStatus_invoiceNotes;
import org.estatio.dom.invoice.viewmodel.dnc.InvoiceSummaryForPropertyDueDateStatus_prepareInvoiceNotes;
import org.estatio.dom.invoice.viewmodel.dnc.InvoiceSummaryForPropertyDueDateStatus_sendByEmailInvoiceNotes;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.invoice.InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003;
import org.estatio.fixturescripts.ApplicationSettingsForReportServerForDemo;
import org.estatio.fixturescripts.SeedDocumentAndCommsFixture;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class InvoiceSummaryforPropertyDueDateStatus_invoiceNotes_IntegTest extends EstatioIntegrationTest {

    public static class ActionInvocationIntegTest extends
            InvoiceSummaryforPropertyDueDateStatus_invoiceNotes_IntegTest {

        @Before
        public void setupData() {
            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());
                    executionContext.executeChild(this, new InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003());

                    executionContext.executeChild(this, new SeedDocumentAndCommsFixture());
                    executionContext.executeChild(this, new ApplicationSettingsForReportServerForDemo());
                }
            });
        }

        @Test
        public void happy_case() throws Exception {

            // given
            InvoiceSummaryForPropertyDueDateStatus summary = findSummary();
            DocAndCommForInvoiceNote invoiceNoteViewModel = invoiceNoteViewModelOf(summary);

            Invoice invoice = invoiceNoteViewModel.getInvoice();
            assertThat(invoice).isNotNull();
            assertThat(invoiceNoteViewModel.getSendTo()).isNotNull();
            assertThat(invoiceNoteViewModel.getSendTo()).isInstanceOf(EmailAddress.class);

            assertThat(mixin(DocAndCommForInvoiceNote_document.class, invoiceNoteViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForInvoiceNote_documentState.class, invoiceNoteViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForInvoiceNote_communication.class, invoiceNoteViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForInvoiceNote_communicationState.class, invoiceNoteViewModel).$$()).isNull();

            // when prepare
            mixin(InvoiceSummaryForPropertyDueDateStatus_prepareInvoiceNotes.class, summary).$$();

            // (clearing queryResultsCache)
            summary = findSummary();
            invoiceNoteViewModel = invoiceNoteViewModelOf(summary);

            // then still null
            Document invoiceNoteDoc = mixin(DocAndCommForInvoiceNote_document.class, invoiceNoteViewModel).$$();
            assertThat(invoiceNoteDoc).isNull();


            // given invoiced
            invoice.setStatus(InvoiceStatus.INVOICED);
            invoice.setInvoiceNumber("12345");
            invoice.setInvoiceDate(clockService.now());

            summary = findSummary(InvoiceStatus.INVOICED);
            invoiceNoteViewModel = invoiceNoteViewModelOf(summary);


            // when prepare
            mixin(InvoiceSummaryForPropertyDueDateStatus_prepareInvoiceNotes.class, summary).$$();


            // then now populated
            invoiceNoteDoc = mixin(DocAndCommForInvoiceNote_document.class, invoiceNoteViewModel).$$();
            assertThat(invoiceNoteDoc).isNotNull();

            assertThat(mixin(DocAndCommForInvoiceNote_documentState.class, invoiceNoteViewModel).$$()).isEqualTo(DocumentState.NOT_RENDERED);

            assertThat(mixin(DocAndCommForInvoiceNote_communication.class, invoiceNoteViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForInvoiceNote_communicationState.class, invoiceNoteViewModel).$$()).isNull();

            // and also
            assertThat(invoiceNoteDoc.getName()).isNotNull();
            assertThat(invoiceNoteDoc.getId()).isNotNull();
            assertThat(invoiceNoteDoc.getCreatedAt()).isNotNull();
            final DocumentType docTypeInvoiceNote = documentTypeRepository.findByReference(Constants.DOC_TYPE_REF_INVOICE);
            assertThat(invoiceNoteDoc.getType()).isEqualTo(docTypeInvoiceNote);

            assertThat(invoiceNoteDoc.getState()).isEqualTo(DocumentState.NOT_RENDERED);
            assertThat(invoiceNoteDoc.getRenderedAt()).isNull();
            assertThat(invoiceNoteDoc.getSort()).isEqualTo(DocumentSort.EMPTY);
            assertThat(invoiceNoteDoc.getMimeType()).isEqualTo("application/pdf");

            // and also attached to only invoice
            List<Paperclip> paperclips = paperclipRepository.findByDocument(invoiceNoteDoc);
            assertThat(paperclips).hasSize(1);
            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice);

            // and when rendered
            runBackgroundCommands();

            summary = findSummary(InvoiceStatus.INVOICED);
            invoiceNoteViewModel = invoiceNoteViewModelOf(summary);

            // then
            assertThat(mixin(DocAndCommForInvoiceNote_documentState.class, invoiceNoteViewModel).$$()).isEqualTo(DocumentState.RENDERED);

            invoiceNoteDoc = mixin(DocAndCommForInvoiceNote_document.class, invoiceNoteViewModel).$$();
            assertThat(invoiceNoteDoc.getState()).isEqualTo(DocumentState.RENDERED);
            assertThat(invoiceNoteDoc.getRenderedAt()).isNotNull();
            assertThat(invoiceNoteDoc.getSort()).isEqualTo(DocumentSort.BLOB);

            //
            // and when send by email
            //
            mixin(InvoiceSummaryForPropertyDueDateStatus_sendByEmailInvoiceNotes.class, summary).$$();

            summary = findSummary(InvoiceStatus.INVOICED);
            invoiceNoteViewModel = invoiceNoteViewModelOf(summary);

            // then
            final Communication invoiceNoteComm = mixin(DocAndCommForInvoiceNote_communication.class, invoiceNoteViewModel).$$();
            assertThat(invoiceNoteComm).isNotNull();
            assertThat(mixin(DocAndCommForInvoiceNote_communicationState.class, invoiceNoteViewModel).$$()).isEqualTo(CommunicationState.PENDING);

            // and PL doc now also attached to comm, invoice.buyer and invoice.seller (as well as invoice)
            paperclips = paperclipRepository.findByDocument(invoiceNoteDoc);
            assertThat(paperclips).hasSize(4);
            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice, invoice.getBuyer(), invoice.getSeller(), invoiceNoteComm);

            // and comm attached to PL and also to a new covernote
            paperclips = paperclipRepository.findByAttachedTo(invoiceNoteComm);
            assertThat(paperclips).hasSize(2);
            assertThat(paperclips).extracting(x -> x.getDocument()).contains(invoiceNoteDoc);

            final DocumentAbstract invoiceNoteDocF = invoiceNoteDoc;
            final Optional<Paperclip> paperclipToCoverNoteIfAny = paperclips.stream().filter(x -> x.getDocument() != invoiceNoteDocF) .findFirst();
            assertThat(paperclipToCoverNoteIfAny.isPresent()).isTrue();
            final Paperclip paperclip = paperclipToCoverNoteIfAny.get();
            assertThat(paperclip.getDocument()).isInstanceOf(Document.class);
            final Document coverNote = (Document) paperclip.getDocument();

            // and also cover note is populated
            assertThat(coverNote.getName()).isNotNull();
            assertThat(coverNote.getId()).isNotNull();
            assertThat(coverNote.getCreatedAt()).isNotNull();
            final DocumentType docTypeInvoiceNoteCoverNote = documentTypeRepository.findByReference(Constants.DOC_TYPE_REF_INVOICE_EMAIL_COVER_NOTE);
            assertThat(coverNote.getType()).isEqualTo(docTypeInvoiceNoteCoverNote);

            assertThat(coverNote.getState()).isEqualTo(DocumentState.RENDERED);
            assertThat(coverNote.getRenderedAt()).isNotNull();
            assertThat(coverNote.getSort()).isEqualTo(DocumentSort.CLOB);
            assertThat(coverNote.getMimeType()).isEqualTo("text/html");

            // and when comm sent
            runBackgroundCommands();

            summary = findSummary(InvoiceStatus.INVOICED);
            invoiceNoteViewModel = invoiceNoteViewModelOf(summary);

            // then
            assertThat(mixin(DocAndCommForInvoiceNote_communicationState.class, invoiceNoteViewModel).$$()).isEqualTo(CommunicationState.SENT);
        }

    }


    InvoiceSummaryForPropertyDueDateStatus findSummary() {
        return findSummary(InvoiceStatus.NEW);
    }

    InvoiceSummaryForPropertyDueDateStatus findSummary(final InvoiceStatus invoiceStatus) {

        // clears out queryResultsCache
        transactionService.nextTransaction();

        List<InvoiceSummaryForPropertyDueDateStatus> summaries = invoiceSummaryRepository.findInvoicesByStatus(
                invoiceStatus);
        assertThat(summaries).hasSize(1);
        return summaries.get(0);
    }

    DocAndCommForInvoiceNote invoiceNoteViewModelOf(final InvoiceSummaryForPropertyDueDateStatus summary) {
        List<DocAndCommForInvoiceNote> viewModels =
                mixin( InvoiceSummaryForPropertyDueDateStatus_invoiceNotes.class, summary).$$();
        assertThat(viewModels).hasSize(1);

        return viewModels.get(0);
    }

    @Inject
    ClockService clockService;

    @Inject
    InvoiceSummaryForPropertyDueDateStatusRepository invoiceSummaryRepository;

    @Inject
    PaperclipRepository paperclipRepository;
    @Inject
    DocumentTypeRepository documentTypeRepository;
}
