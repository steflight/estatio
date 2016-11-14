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

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannel;
import org.incode.module.communications.dom.impl.commchannel.EmailAddress;
import org.incode.module.communications.dom.impl.comms.Communication;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentTemplate;
import org.incode.module.document.dom.impl.docs.DocumentTemplateRepository;
import org.incode.module.document.dom.impl.docs.Document_delete;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;
import org.incode.module.document.dom.impl.types.DocumentType;
import org.incode.module.document.dom.impl.types.DocumentTypeRepository;

import org.estatio.dom.invoice.Constants;
import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.InvoiceRepository;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.invoice.Invoice_createAndAttachDocument;
import org.estatio.dom.invoice.PaymentMethod;
import org.estatio.dom.invoice.dnc.Invoice_email;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatusRepository;
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
                Invoice invoice = findInvoice();
                DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);

                // when
                final Document document = wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);

                // then
                assertThat(document).isNotNull();
                List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
                assertThat(paperclips).hasSize(1);

                assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice);
            }


            @Test
            public void for_invoice_note() throws Exception {

                // given
                Invoice invoice = findInvoice();
                DocumentTemplate invoiceNoteTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_INVOICE, invoice);

                // given the invoice has been invoiced
                invoice.setStatus(InvoiceStatus.INVOICED);
                invoice.setInvoiceNumber("12345");
                invoice.setInvoiceDate(clockService.now());

                // when
                final Document document = wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(invoiceNoteTemplate);

                // then
                assertThat(document).isNotNull();
                List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
                assertThat(paperclips).hasSize(1);

                assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice);
            }

            @Test
            public void cannot_create_invoice_note_if_the_invoice_has_not_yet_been_invoied() throws Exception {

                // given
                Invoice invoice = findInvoice();
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


    public static class Invoice_remove_IntegTest extends Invoice_DocumentManagement_IntegTest {

        @Test
        public void when_has_associated_document_that_has_not_been_sent() throws Exception {

            // given
            Invoice invoice = findInvoice();
            assertThat(invoice).isNotNull();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();

            // and given have a PL doc
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
            final Document document = wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
            assertThat(document).isNotNull();

            // and given is attached to only invoice
            List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
            assertThat(paperclips).hasSize(1);

            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice);

            final Bookmark documentBookmark = bookmarkService.bookmarkFor(document);

            // when remove the invoice
            wrap(invoice).remove();

            transactionService.nextTransaction();

            // then (deletes invoice, its paperclip)
            invoice = findInvoice();
            assertThat(invoice).isNull();

            // and expect (to have deleted associated document too)
            expectedExceptions.expectMessage("only resolve object that is persistent");

            // when
            bookmarkService.lookup(documentBookmark);
        }

        @Test
        public void when_has_associated_document_that_HAS_been_sent() throws Exception {

            // given
            Invoice invoice = findInvoice();
            assertThat(invoice).isNotNull();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();

            // and given have a PL doc
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
            final Document document = wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
            assertThat(document).isNotNull();

            // and given document sent
            final CommunicationChannel sendTo = invoice.getSendTo();
            assertThat(sendTo).isInstanceOf(EmailAddress.class);
            mixin(Invoice_email.class, invoice).$$(document, (EmailAddress)sendTo, null, null, null);

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
            wrap(invoice).remove();

            transactionService.flushTransaction();

            // then (deletes invoice, one of its paperclips)
            invoice = findInvoice();
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
            Invoice invoice = findInvoice();
            assertThat(invoice).isNotNull();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();

            // and given have a PL doc
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
            final Document document = wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);
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
            Invoice invoice = findInvoice();
            assertThat(invoice).isNotNull();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();

            // and given have a PL doc
            DocumentTemplate prelimLetterTemplate = findDocumentTemplateFor(Constants.DOC_TYPE_REF_PRELIM, invoice);
            final Document document = wrap(mixin(Invoice_createAndAttachDocument.class, invoice)).$$(prelimLetterTemplate);

            // and given document sent
            final CommunicationChannel sendTo = invoice.getSendTo();
            assertThat(sendTo).isInstanceOf(EmailAddress.class);
            mixin(Invoice_email.class, invoice).$$(document, (EmailAddress)sendTo, null, null, null);

            transactionService.flushTransaction();

            // then expect
            expectedExceptions.expectMessage("Document has already been sent as a communication");

            // when attempt to
            wrap(mixin(Document_delete.class, document)).$$();
        }

    }



    //endregion

    //region > helpers (finders)

    private List<Invoice> findMatchingInvoices(final Party seller, final Party buyer, final Lease lease, final LocalDate invoiceStartDate) {
        return invoiceRepository.findMatchingInvoices(
                seller, buyer, PaymentMethod.DIRECT_DEBIT,
                lease, InvoiceStatus.NEW,
                invoiceStartDate);
    }

    Invoice findInvoice() {

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

        List<Invoice> matchingInvoices = findMatchingInvoices(seller, buyer, lease, invoiceStartDate);
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

    @Inject
    InvoiceSummaryForPropertyDueDateStatusRepository invoiceSummaryRepository;

    @Inject
    RepositoryService repositoryService;
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

}
