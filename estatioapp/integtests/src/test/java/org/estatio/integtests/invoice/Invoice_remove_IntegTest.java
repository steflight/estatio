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

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.email.EmailService;
import org.apache.isis.applib.services.xactn.TransactionService;

import org.isisaddons.module.command.dom.BackgroundCommandServiceJdoRepository;
import org.isisaddons.wicket.gmap3.cpt.service.LocationLookupService;

import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;
import org.incode.module.document.dom.impl.types.DocumentTypeRepository;

import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatus;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatusRepository;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter_document;
import org.estatio.dom.invoice.viewmodel.dnc.InvoiceSummaryForPropertyDueDateStatus_preliminaryLetters;
import org.estatio.dom.invoice.viewmodel.dnc.InvoiceSummaryForPropertyDueDateStatus_preparePreliminaryLetters;
import org.estatio.dom.party.Party;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.invoice.InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003;
import org.estatio.fixturescripts.ApplicationSettingsForReportServerForDemo;
import org.estatio.fixturescripts.SeedDocumentAndCommsFixture;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class Invoice_remove_IntegTest extends EstatioIntegrationTest {

    public static class ActionInvocationIntegTest extends
            Invoice_remove_IntegTest {

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
            DocAndCommForPrelimLetter prelimLetterViewModel = prelimLetterViewModelOf(summary);

            Invoice invoice = prelimLetterViewModel.getInvoice();
            assertThat(invoice.getStatus().invoiceIsChangable()).isTrue();


            // and given have a PL doc
            mixin(InvoiceSummaryForPropertyDueDateStatus_preparePreliminaryLetters.class, summary).$$();

            // (clearing queryResultsCache)
            summary = findSummary();
            prelimLetterViewModel = prelimLetterViewModelOf(summary);

            // and given is attached to invoice, buyer and seller
            Document prelimLetterDoc = mixin(DocAndCommForPrelimLetter_document.class, prelimLetterViewModel).$$();
            List<Paperclip> paperclips = paperclipRepository.findByDocument(prelimLetterDoc);
            assertThat(paperclips).hasSize(3);

            final Party invoiceSeller = invoice.getSeller();
            final Party invoiceBuyer = invoice.getBuyer();
            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice, invoiceBuyer, invoiceSeller);

            // when remove the invoice
            wrap(invoice).remove();

            transactionService.flushTransaction();

            // then
            paperclips = paperclipRepository.findByDocument(prelimLetterDoc);
            assertThat(paperclips).hasSize(2);
            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoiceBuyer, invoiceSeller);

        }
    }


    InvoiceSummaryForPropertyDueDateStatus findSummary() {

        // clears out queryResultsCache
        transactionService.nextTransaction();

        List<InvoiceSummaryForPropertyDueDateStatus> summaries = invoiceSummaryRepository.findInvoicesByStatus(InvoiceStatus.NEW);
        assertThat(summaries).hasSize(1);
        return summaries.get(0);
    }

    DocAndCommForPrelimLetter prelimLetterViewModelOf(final InvoiceSummaryForPropertyDueDateStatus summary) {
        List<DocAndCommForPrelimLetter> prelimLetterViewModels =
                mixin( InvoiceSummaryForPropertyDueDateStatus_preliminaryLetters.class, summary).$$();
        assertThat(prelimLetterViewModels).hasSize(1);

        prelimLetterViewModels = mixin(InvoiceSummaryForPropertyDueDateStatus_preliminaryLetters.class, summary).$$();


        return prelimLetterViewModels.get(0);
    }

    @Inject
    InvoiceSummaryForPropertyDueDateStatusRepository invoiceSummaryRepository;

    @Inject
    PaperclipRepository paperclipRepository;
    @Inject
    DocumentTypeRepository documentTypeRepository;
    @Inject
    BackgroundCommandServiceJdoRepository backgroundCommandRepository;
    @Inject
    TransactionService transactionService;

    @Inject
    List<EmailService> emailServices;

    @Inject LocationLookupService locationLookupService;
}
