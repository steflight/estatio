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
package org.estatio.integtests.invoice;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.google.common.io.Resources;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.wrapper.HiddenException;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.value.Blob;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannel;
import org.incode.module.communications.dom.impl.commchannel.EmailAddress;
import org.incode.module.communications.dom.impl.comms.Communication;
import org.incode.module.communications.dom.impl.comms.CommunicationState;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentAbstract;
import org.incode.module.document.dom.impl.docs.DocumentSort;
import org.incode.module.document.dom.impl.docs.DocumentState;
import org.incode.module.document.dom.impl.docs.DocumentTemplate;
import org.incode.module.document.dom.impl.docs.DocumentTemplateRepository;
import org.incode.module.document.dom.impl.docs.Document_delete;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;
import org.incode.module.document.dom.impl.paperclips.Paperclip_changeRole;
import org.incode.module.document.dom.impl.types.DocumentType;
import org.incode.module.document.dom.impl.types.DocumentTypeRepository;

import org.estatio.dom.invoice.Constants;
import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.InvoiceRepository;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.invoice.dnc.Invoice_attachReceipt;
import org.estatio.dom.invoice.dnc.Invoice_createAndAttachDocument;
import org.estatio.dom.invoice.PaymentMethod;
import org.estatio.dom.invoice.dnc.Invoice_email;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote_communication;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote_communicationState;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote_document;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForInvoiceNote_documentState;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter_communication;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter_communicationState;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter_document;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter_documentState;
import org.estatio.dom.invoice.viewmodel.dnc.Invoice_invoiceNotes;
import org.estatio.dom.invoice.viewmodel.dnc.Invoice_preliminaryLetters;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.dom.party.Party;
import org.estatio.dom.party.PartyRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.invoice.InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003;
import org.estatio.fixturescripts.ApplicationSettingsForReportServerForDemo;
import org.estatio.fixturescripts.SeedDocumentAndCommsFixture;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class Invoice_DocumentManagement_IntegTest extends EstatioIntegrationTest {

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


    public static class Invoice_createAndAttachDocument_IntegTest extends Invoice_DocumentManagement_IntegTest {

        public static class ActionInvocationIntegTest extends
                Invoice_DocumentManagement_IntegTest {

            @Test
            public void for_prelim_letter() throws Exception {

                // given
                Invoice invoice = findInvoice(InvoiceStatus.NEW);
                DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);

                // when
                wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
                Document document = prelimLetterOf(invoice);

                // then
                assertThat(document).isNotNull();
                List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
                assertThat(paperclips).hasSize(1);

                assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice);
            }


            @Test
            public void for_invoice_note() throws Exception {

                // given
                Invoice invoice = findInvoice(InvoiceStatus.NEW);
                DocumentTemplate invoiceNoteTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_INVOICE, invoice);

                // given the invoice has been invoiced
                approveAndInvoice(invoice);

                // when
                wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(invoiceNoteTemplate);
                Document document = invoiceNoteOf(invoice);

                // then
                assertThat(document).isNotNull();
                List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
                assertThat(paperclips).hasSize(1);

                assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice);
            }

            @Test
            public void cannot_create_invoice_note_if_the_invoice_has_not_yet_been_invoiced() throws Exception {

                // given
                Invoice invoice = findInvoice(InvoiceStatus.NEW);
                DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
                DocumentTemplate invoiceNoteTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_INVOICE, invoice);

                // when
                final List<DocumentTemplate> documentTemplates =
                        mixin(Invoice_createAndAttachDocument.class, invoice).choices0$$();

                // then
                assertThat(documentTemplates).doesNotContain(invoiceNoteTemplate);
                assertThat(documentTemplates).contains(prelimLetterTemplate);
            }
        }

    }


    public static class Invoice_attachReceipt_IntegTest extends Invoice_DocumentManagement_IntegTest {

        @Test
        public void when_has_associated_document_that_has_not_been_sent() throws Exception {

            // given
            Invoice invoice = findInvoice(InvoiceStatus.NEW);
            assertThat(invoice).isNotNull();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();

            List<Paperclip> paperclips = paperclipRepository.findByAttachedTo(invoice);
            assertThat(paperclips).isEmpty();

            final Invoice_attachReceipt invoice_attachReceipt = mixin(Invoice_attachReceipt.class, invoice);

            // when
            final List<DocumentType> documentTypes = invoice_attachReceipt.choices0$$();

            // then
            assertThat(documentTypes).hasSize(2);

            // and when
            final DocumentType documentType = documentTypes.get(0);
            final String fileName = "sales-receipt.pdf";
            final Blob blob = asBlob(fileName);

            wrap(invoice_attachReceipt).$$(documentType, blob, null);

            // then
            paperclips = paperclipRepository.findByAttachedTo(invoice);
            assertThat(paperclips).hasSize(1);

            final Paperclip paperclip = paperclips.get(0);
            final DocumentAbstract documentAbs = paperclip.getDocument();
            assertThat(documentAbs).isInstanceOf(Document.class);
            Document document = (Document) documentAbs;

            assertThat(documentAbs.getId()).isNotNull();
            assertThat(documentAbs.getSort()).isEqualTo(DocumentSort.BLOB);
            assertThat(documentAbs.getMimeType()).isEqualTo("application/pdf");
            assertThat(documentAbs.getName()).isEqualTo(fileName);
            assertThat(documentAbs.getAtPath()).isEqualTo(invoice.getApplicationTenancyPath());
            assertThat(documentAbs.getBlobBytes()).isEqualTo(blob.getBytes());
            assertThat(documentAbs.getType()).isEqualTo(documentType);
            assertThat(document.getRenderedAt()).isNotNull();
            assertThat(document.getCreatedAt()).isNotNull();

            final Object attachedTo = paperclip.getAttachedTo();
            assertThat(attachedTo).isSameAs(invoice);

            assertThat(paperclip.getRoleName()).isEqualTo(Invoice_attachReceipt.PAPERCLIP_ROLE_NAME);
            assertThat(paperclip.getDocumentCreatedAt()).isEqualTo(document.getCreatedAt());
            assertThat(paperclip.getDocumentDate()).isEqualTo(document.getCreatedAt());

        }

    }



    public static class Invoice_email_IntegTest extends Invoice_DocumentManagement_IntegTest {

        @Test
        public void when_prelim_letter_any_invoice_receipts_attached_are_ignored() throws IOException {

            // given
            Invoice invoice = findInvoice(InvoiceStatus.NEW);
            DocAndCommForPrelimLetter prelimLetterViewModel = prelimLetterViewModelOf(invoice);

            assertThat(mixin(DocAndCommForPrelimLetter_document.class, prelimLetterViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForPrelimLetter_documentState.class, prelimLetterViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForPrelimLetter_communication.class, prelimLetterViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForPrelimLetter_communicationState.class, prelimLetterViewModel).$$()).isNull();

            // and given there is a receipt attached to the invoice
            final Invoice_attachReceipt invoice_attachReceipt = mixin(Invoice_attachReceipt.class, invoice);

            final List<DocumentType> documentTypes = invoice_attachReceipt.choices0$$();
            assertThat(documentTypes).hasSize(2);
            final DocumentType documentType = documentTypes.get(0);

            final Blob blob = asBlob("sales-receipt.pdf");

            wrap(invoice_attachReceipt).$$(documentType, blob, null);

            invoice = findInvoice(InvoiceStatus.NEW);

            List<Paperclip> paperclips = paperclipRepository.findByAttachedTo(invoice);
            assertThat(paperclips).hasSize(1);
            final DocumentAbstract attachedReceipt = paperclips.get(0).getDocument();

            // when
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
            wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
            Document document = prelimLetterOf(invoice);

            // (clearing queryResultsCache)
            invoice = findInvoice(InvoiceStatus.NEW);
            prelimLetterViewModel = prelimLetterViewModelOf(invoice);

            // then the newly created prelim letter doc
            Document prelimLetterDoc = mixin(DocAndCommForPrelimLetter_document.class, prelimLetterViewModel).$$();
            assertThat(prelimLetterDoc).isSameAs(document);
            assertThat(document.getState()).isEqualTo(DocumentState.RENDERED);

            assertThat(mixin(DocAndCommForPrelimLetter_communication.class, prelimLetterViewModel).$$()).isNull();

            // is attached to only invoice
            paperclips = paperclipRepository.findByDocument(prelimLetterDoc);
            assertThat(paperclips).hasSize(1);
            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice);

            // while the invoice itself now has two attachments (the original receipt and the newly created doc)
            paperclips = paperclipRepository.findByAttachedTo(invoice);
            assertThat(paperclips).hasSize(2);
            assertThat(paperclips).extracting(x -> x.getDocument()).contains(attachedReceipt, prelimLetterDoc);


            // and given
            final CommunicationChannel sendTo = invoice.getSendTo();
            assertThat(sendTo).isInstanceOf(EmailAddress.class);
            final EmailAddress sendToEmailAddress = (EmailAddress) sendTo;

            // when
            final Communication communication =
                    wrap(mixin(Invoice_email.class, invoice)).$$(document, sendToEmailAddress, null, null);

            invoice = findInvoice(InvoiceStatus.NEW);
            prelimLetterViewModel = prelimLetterViewModelOf(invoice);

            // then
            final Communication prelimLetterComm =
                    mixin(DocAndCommForPrelimLetter_communication.class, prelimLetterViewModel).$$();
            assertThat(prelimLetterComm).isSameAs(communication);

            assertThat(communication.getState()).isEqualTo(CommunicationState.PENDING);

            // and PL doc now also attached to comm, invoice.buyer and invoice.seller (as well as invoice)
            paperclips = paperclipRepository.findByDocument(prelimLetterDoc);
            assertThat(paperclips).hasSize(4);
            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice, invoice.getBuyer(), invoice.getSeller(), prelimLetterComm);

            // and comm attached to PL and also to a new covernote
            paperclips = paperclipRepository.findByAttachedTo(prelimLetterComm);
            assertThat(paperclips).hasSize(2);
            assertThat(paperclips).extracting(x -> x.getDocument()).contains(prelimLetterDoc);

        }

        @Test
        public void when_invoice_note_then_any_receipts_attached_are_included() throws IOException {

            // given an 'invoiced' invoice (so can create invoice notes for it)
            Invoice invoice = findInvoice(InvoiceStatus.NEW);
            approveAndInvoice(invoice);

            // without any document yet created
            DocAndCommForInvoiceNote invoiceNoteViewModel = invoiceNoteViewModelOf(invoice);

            assertThat(mixin(DocAndCommForInvoiceNote_document.class, invoiceNoteViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForInvoiceNote_documentState.class, invoiceNoteViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForInvoiceNote_communication.class, invoiceNoteViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForInvoiceNote_communicationState.class, invoiceNoteViewModel).$$()).isNull();

            // and given there is a receipt attached to the invoice
            final Invoice_attachReceipt invoice_attachReceipt = mixin(Invoice_attachReceipt.class, invoice);

            final List<DocumentType> documentTypes = invoice_attachReceipt.choices0$$();
            assertThat(documentTypes).hasSize(2);
            final DocumentType documentType = documentTypes.get(0);

            final Blob blob = asBlob("sales-receipt.pdf");

            wrap(invoice_attachReceipt).$$(documentType, blob, null);

            invoice = findInvoice(InvoiceStatus.INVOICED);
            List<Paperclip> paperclips = paperclipRepository.findByAttachedTo(invoice);
            assertThat(paperclips).hasSize(1);
            final DocumentAbstract attachedReceipt = paperclips.get(0).getDocument();

            // when
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_INVOICE, invoice);
            wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
            Document document = invoiceNoteOf(invoice);

            invoice = findInvoice(InvoiceStatus.INVOICED);
            invoiceNoteViewModel = invoiceNoteViewModelOf(invoice);

            // then the newly created invoice note doc
            Document invoiceNoteDoc = mixin(DocAndCommForInvoiceNote_document.class, invoiceNoteViewModel).$$();
            assertThat(invoiceNoteDoc).isSameAs(document);
            assertThat(document.getState()).isEqualTo(DocumentState.RENDERED);

            assertThat(mixin(DocAndCommForInvoiceNote_communication.class, invoiceNoteViewModel).$$()).isNull();

            // is attached to invoice and also the receipt
            paperclips = paperclipRepository.findByDocument(invoiceNoteDoc);
            assertThat(paperclips).hasSize(2);
            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice, attachedReceipt);

            // while the invoice itself also has two attachments (the original receipt and the newly created doc)
            paperclips = paperclipRepository.findByAttachedTo(invoice);
            assertThat(paperclips).hasSize(2);
            assertThat(paperclips).extracting(x -> x.getDocument()).contains(attachedReceipt, invoiceNoteDoc);


            // and given
            final CommunicationChannel sendTo = invoice.getSendTo();
            assertThat(sendTo).isInstanceOf(EmailAddress.class);
            final EmailAddress sendToEmailAddress = (EmailAddress) sendTo;

            // when
            final Communication communication =
                    wrap(mixin(Invoice_email.class, invoice)).$$(document, sendToEmailAddress, null, null);

            invoice = findInvoice(InvoiceStatus.INVOICED);
            invoiceNoteViewModel = invoiceNoteViewModelOf(invoice);

            // then
            final Communication invoiceNoteComm =
                    mixin(DocAndCommForInvoiceNote_communication.class, invoiceNoteViewModel).$$();
            assertThat(invoiceNoteComm).isSameAs(communication);

            assertThat(communication.getState()).isEqualTo(CommunicationState.PENDING);

            // and InvNote doc now also attached to comm, invoice.buyer and invoice.seller (as well as invoice and receipt)
            paperclips = paperclipRepository.findByDocument(invoiceNoteDoc);
            assertThat(paperclips).hasSize(5);
            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice, invoice.getBuyer(), invoice.getSeller(), invoiceNoteComm, attachedReceipt);

            // and comm attached to PL, also to a new covernote, AND ALSO to the original attached receipt
            paperclips = paperclipRepository.findByAttachedTo(invoiceNoteComm);
            assertThat(paperclips).hasSize(3);
            assertThat(paperclips).extracting(x -> x.getDocument()).contains(invoiceNoteDoc, attachedReceipt);

        }

    }

    public static class Invoice_remove_IntegTest extends Invoice_DocumentManagement_IntegTest {

        @Test
        public void when_has_associated_document_that_has_not_been_sent() throws Exception {

            // given
            Invoice invoice = findInvoice(InvoiceStatus.NEW);
            assertThat(invoice).isNotNull();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();

            // and given have a PL doc
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
            wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
            Document document = prelimLetterOf(invoice);
            assertThat(document).isNotNull();

            // and given is attached to only invoice
            List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
            assertThat(paperclips).hasSize(1);

            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice);

            final Bookmark documentBookmark = bookmarkService.bookmarkFor(document);

            // when remove the invoice
            wrap(mixin(Invoice._remove.class, invoice)).$$();

            transactionService.nextTransaction();

            // then (deletes invoice, its paperclip)
            invoice = findInvoice(InvoiceStatus.NEW);
            assertThat(invoice).isNull();

            // and expect (to have deleted associated document too)
            expectedExceptions.expectMessage("only resolve object that is persistent");

            // when
            bookmarkService.lookup(documentBookmark);
        }

        @Test
        public void when_has_associated_document_that_HAS_been_sent() throws Exception {

            // given
            Invoice invoice = findInvoice(InvoiceStatus.NEW);
            assertThat(invoice).isNotNull();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();

            // and given have a PL doc
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
            wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
            Document document = prelimLetterOf(invoice);
            assertThat(document).isNotNull();

            // and given document sent
            final CommunicationChannel sendTo = invoice.getSendTo();
            assertThat(sendTo).isInstanceOf(EmailAddress.class);
            mixin(Invoice_email.class, invoice).$$(document, (EmailAddress)sendTo, null, null);

            transactionService.flushTransaction();

            // and given is attached to invoice, buyer and seller and the comm
            List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
            assertThat(paperclips).hasSize(4);

            final Party invoiceSeller = invoice.getSeller();
            final Party invoiceBuyer = invoice.getBuyer();
            final Optional<Communication> commIfAny =
                    paperclips.stream()
                            .map(x -> x.getAttachedTo())
                            .filter(x -> x instanceof Communication)
                            .map(Communication.class::cast)
                            .findFirst();
            assertThat(commIfAny.isPresent()).isTrue();
            final Communication communication = commIfAny.get();

            assertThat(paperclips)
                    .extracting(x -> x.getAttachedTo())
                    .contains(invoice, invoiceBuyer, invoiceSeller, communication);

            // when remove the invoice
            wrap(mixin(Invoice._remove.class, invoice)).$$();

            transactionService.flushTransaction();

            // then (deletes invoice, one of its paperclips)
            invoice = findInvoice(InvoiceStatus.NEW);
            assertThat(invoice).isNull();

            // but still attached to buyer, seller and communication
            assertThat(paperclipRepository.findByAttachedTo(invoiceBuyer)).extracting(x -> x.getDocument()).contains(document);
            assertThat(paperclipRepository.findByAttachedTo(invoiceSeller)).extracting(x -> x.getDocument()).contains(document);
            assertThat(paperclipRepository.findByAttachedTo(communication)).extracting(x -> x.getDocument()).contains(document);

            // and document still exists
            final Bookmark documentBookmark = bookmarkService.bookmarkFor(document);
            transactionService.nextTransaction();

            // (ie no exception thrown)
            bookmarkService.lookup(documentBookmark);
        }

    }


    public static class Document_delete_IntegTest extends Invoice_DocumentManagement_IntegTest {

        @Test
        public void can_delete_document_when_not_been_sent() throws Exception {

            // given
            Invoice invoice = findInvoice(InvoiceStatus.NEW);
            assertThat(invoice).isNotNull();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();

            // and given have a PL doc
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
            wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
            Document document = prelimLetterOf(invoice);

            assertThat(document).isNotNull();

            assertThat(paperclipRepository.findByAttachedTo(invoice)).hasSize(1);

            // when
            final Bookmark documentBookmark = bookmarkService.bookmarkFor(document);

            wrap(mixin(Document_delete.class, document)).$$();

            transactionService.nextTransaction();

            // then expect
            expectedExceptions.expectMessage("only resolve object that is persistent");

            // when attempt to find
            bookmarkService.lookup(documentBookmark);

        }

        @Test
        public void canNOT_delete_document_once_it_has_been_sent() throws Exception {

            // given
            Invoice invoice = findInvoice(InvoiceStatus.NEW);
            assertThat(invoice).isNotNull();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();

            // and given have a PL doc
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
            wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
            Document document = prelimLetterOf(invoice);

            // and given document sent
            final CommunicationChannel sendTo = invoice.getSendTo();
            assertThat(sendTo).isInstanceOf(EmailAddress.class);
            mixin(Invoice_email.class, invoice).$$(document, (EmailAddress)sendTo, null, null);

            transactionService.flushTransaction();

            // then expect
            expectedExceptions.expectMessage("Document has already been sent as a communication");

            // when attempt to
            wrap(mixin(Document_delete.class, document)).$$();
        }

    }


    public static class Paperclip_changeRole_IntegTest extends Invoice_DocumentManagement_IntegTest {

        @Test
        public void for_prelim_letter() throws Exception {

            // given
            Invoice invoice = findInvoice(InvoiceStatus.NEW);
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);

            // and given
            wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
            Document document = prelimLetterOf(invoice);
            assertThat(document).isNotNull();

            // and given
            List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
            assertThat(paperclips).hasSize(1);
            final Paperclip paperclip = paperclips.get(0);

            // expect
            expectedExceptions.expect(HiddenException.class);

            // when
            wrap(mixin(Paperclip_changeRole.class, paperclip)).$$("new role");
        }
    }


    //region > helpers

    private List<Invoice> findMatchingInvoices(
            final Party seller,
            final Party buyer,
            final Lease lease,
            final LocalDate invoiceStartDate, final InvoiceStatus invoiceStatus) {
        return invoiceRepository.findMatchingInvoices(
                seller, buyer, PaymentMethod.DIRECT_DEBIT,
                lease, invoiceStatus,
                invoiceStartDate);
    }

    Invoice findInvoice(final InvoiceStatus invoiceStatus) {

        // clears out queryResultsCache
        transactionService.nextTransaction();

        final Party seller = partyRepository
                .findPartyByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003.PARTY_REF_SELLER);
        final Party buyer = partyRepository
                .findPartyByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003.PARTY_REF_BUYER);
        final Lease lease = leaseRepository
                .findLeaseByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003.LEASE_REF);
        final LocalDate invoiceStartDate = InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003
                .startDateFor(lease);

        List<Invoice> matchingInvoices = findMatchingInvoices(seller, buyer, lease, invoiceStartDate, invoiceStatus);
        assertThat(matchingInvoices.size()).isLessThanOrEqualTo(1);
        return matchingInvoices.isEmpty() ? null : matchingInvoices.get(0);
    }

    DocumentTemplate findDocumentTemplateFor(final String docTypeRef, final Invoice invoice) {
        final DocumentType documentType = documentTypeRepository.findByReference(docTypeRef);
        assertThat(documentType).isNotNull();
        DocumentTemplate documentTemplate = documentTemplateRepository.findFirstByTypeAndApplicableToAtPath(documentType, invoice.getApplicationTenancyPath());
        assertThat(documentType).isNotNull();
        return documentTemplate;
    }

    DocAndCommForPrelimLetter prelimLetterViewModelOf(final Invoice invoice) {
        List<DocAndCommForPrelimLetter> prelimLetterViewModels = mixin(Invoice_preliminaryLetters.class, invoice).$$();
        assertThat(prelimLetterViewModels).hasSize(1);
        return prelimLetterViewModels.get(0);
    }

    DocAndCommForInvoiceNote invoiceNoteViewModelOf(final Invoice invoice) {
        List<DocAndCommForInvoiceNote> invoiceNoteViewModels = mixin(Invoice_invoiceNotes.class, invoice).$$();
        assertThat(invoiceNoteViewModels).hasSize(1);
        return invoiceNoteViewModels.get(0);
    }

    static Blob asBlob(final String fileName) throws IOException {
        final URL url = Resources.getResource(Invoice_DocumentManagement_IntegTest.class, fileName);
        final byte[] bytes = Resources.toByteArray(url);
        return new Blob(fileName, "application/pdf", bytes);
    }

    void approveAndInvoice(final Invoice invoice) {
        wrap(mixin(Invoice._approve.class, invoice)).$$();
        wrap(mixin(Invoice._invoice.class, invoice)).$$(invoice.getDueDate().minusDays(1));
    }

    Document prelimLetterOf(final Invoice invoice) {
        final List<DocAndCommForPrelimLetter> viewModels = mixin(Invoice_preliminaryLetters.class, invoice).$$();
        assertThat(viewModels).hasSize(1);
        final DocAndCommForPrelimLetter viewModel = viewModels.get(0);
        final Document document = mixin(DocAndCommForPrelimLetter_document.class, viewModel).$$();
        assertThat(document).isNotNull();
        return document;
    }

    Document invoiceNoteOf(final Invoice invoice) {
        final List<DocAndCommForInvoiceNote> viewModels = mixin(Invoice_invoiceNotes.class, invoice).$$();
        assertThat(viewModels).hasSize(1);
        final DocAndCommForInvoiceNote viewModel = viewModels.get(0);
        final Document document = mixin(DocAndCommForInvoiceNote_document.class, viewModel).$$();
        assertThat(document).isNotNull();
        return document;
    }



    //endregion

    //region > injected services

    @Inject
    BookmarkService2 bookmarkService;

    @Inject
    InvoiceRepository invoiceRepository;
    @Inject
    PartyRepository partyRepository;
    @Inject
    LeaseRepository leaseRepository;

    @Inject
    DocumentTemplateRepository documentTemplateRepository;
    @Inject
    DocumentTypeRepository documentTypeRepository;

    @Inject
    PaperclipRepository paperclipRepository;
    @Inject
    TransactionService transactionService;

    @Inject
    ClockService clockService;
    //endregion

}
