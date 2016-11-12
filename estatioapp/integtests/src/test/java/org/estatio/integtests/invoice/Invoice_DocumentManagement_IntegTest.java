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

import javax.inject.Inject;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;

import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentTemplate;
import org.incode.module.document.dom.impl.docs.DocumentTemplateRepository;
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


    public static class _createAndAttachDocument_IntegTest extends Invoice_DocumentManagement_IntegTest {

        public static class ActionInvocationIntegTest extends
                Invoice_DocumentManagement_IntegTest {

            @Test
            public void happy_case() throws Exception {

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
        }

    }


    public static class _remove_IntegTest extends Invoice_DocumentManagement_IntegTest {

        @Test
        public void happy_case() throws Exception {

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

    }

    //region > helpers (matchers)
    <T> Matcher<T> isPersistent(final T domainObject) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(final T t) {
                return repositoryService.isPersistent(t);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("is persistent");
            }
        };
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

    //endregion

}
