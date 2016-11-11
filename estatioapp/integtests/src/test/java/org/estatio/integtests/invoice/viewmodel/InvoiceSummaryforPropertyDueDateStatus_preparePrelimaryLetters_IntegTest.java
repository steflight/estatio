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

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.email.EmailService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;

import org.isisaddons.module.command.dom.BackgroundCommandExecutionFromBackgroundCommandServiceJdo;
import org.isisaddons.module.command.dom.BackgroundCommandServiceJdoRepository;
import org.isisaddons.module.command.dom.CommandJdo;
import org.isisaddons.wicket.gmap3.cpt.service.LocationLookupService;

import org.incode.module.communications.dom.impl.commchannel.EmailAddress;
import org.incode.module.communications.dom.impl.comms.Communication;
import org.incode.module.communications.dom.impl.comms.CommunicationState;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentSort;
import org.incode.module.document.dom.impl.docs.DocumentState;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;
import org.incode.module.document.dom.impl.types.DocumentType;
import org.incode.module.document.dom.impl.types.DocumentTypeRepository;

import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatus;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatusRepository;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter_communication;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter_communicationState;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter_document;
import org.estatio.dom.invoice.viewmodel.dnc.DocAndCommForPrelimLetter_documentState;
import org.estatio.dom.invoice.viewmodel.dnc.InvoiceSummaryForPropertyDueDateStatus_preliminaryLetters;
import org.estatio.dom.invoice.viewmodel.dnc.InvoiceSummaryForPropertyDueDateStatus_preparePreliminaryLetters;
import org.estatio.dom.invoice.viewmodel.dnc.InvoiceSummaryForPropertyDueDateStatus_sendByEmailPreliminaryLetters;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.documents.DocumentTypeAndTemplatesFSForInvoicesUsingSsrs;
import org.estatio.fixture.invoice.InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003;
import org.estatio.fixturescripts.ApplicationSettingsForReportServerForDemo;
import org.estatio.fixturescripts.SeedDocumentAndCommsFixture;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class InvoiceSummaryforPropertyDueDateStatus_preparePrelimaryLetters_IntegTest extends EstatioIntegrationTest {

    public static class ActionInvocationIntegTest extends
            InvoiceSummaryforPropertyDueDateStatus_preparePrelimaryLetters_IntegTest {

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
            assertThat(invoice).isNotNull();
            assertThat(prelimLetterViewModel.getSendTo()).isNotNull();
            assertThat(prelimLetterViewModel.getSendTo()).isInstanceOf(EmailAddress.class);

            assertThat(mixin(DocAndCommForPrelimLetter_document.class, prelimLetterViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForPrelimLetter_documentState.class, prelimLetterViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForPrelimLetter_communication.class, prelimLetterViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForPrelimLetter_communicationState.class, prelimLetterViewModel).$$()).isNull();

            // when prepare
            mixin(InvoiceSummaryForPropertyDueDateStatus_preparePreliminaryLetters.class, summary).$$();

            // (clearing queryResultsCache)
            summary = findSummary();
            prelimLetterViewModel = prelimLetterViewModelOf(summary);

            // then
            Document prelimLetterDoc = mixin(DocAndCommForPrelimLetter_document.class, prelimLetterViewModel).$$();
            assertThat(prelimLetterDoc).isNotNull();
            assertThat(mixin(DocAndCommForPrelimLetter_documentState.class, prelimLetterViewModel).$$()).isEqualTo(DocumentState.NOT_RENDERED);

            assertThat(mixin(DocAndCommForPrelimLetter_communication.class, prelimLetterViewModel).$$()).isNull();
            assertThat(mixin(DocAndCommForPrelimLetter_communicationState.class, prelimLetterViewModel).$$()).isNull();

            // and also
            assertThat(prelimLetterDoc.getName()).isNotNull();
            assertThat(prelimLetterDoc.getId()).isNotNull();
            assertThat(prelimLetterDoc.getCreatedAt()).isNotNull();
            final DocumentType docTypePrelimLetter =
                    documentTypeRepository.findByReference(DocumentTypeAndTemplatesFSForInvoicesUsingSsrs.DOC_TYPE_REF_INVOICE_PRELIM);
            assertThat(prelimLetterDoc.getType()).isEqualTo(docTypePrelimLetter);

            assertThat(prelimLetterDoc.getState()).isEqualTo(DocumentState.NOT_RENDERED);
            assertThat(prelimLetterDoc.getRenderedAt()).isNull();
            assertThat(prelimLetterDoc.getSort()).isEqualTo(DocumentSort.EMPTY);
            assertThat(prelimLetterDoc.getMimeType()).isEqualTo("application/pdf");

            // and also
            final List<Paperclip> paperclips = paperclipRepository.findByDocument(prelimLetterDoc);
            assertThat(paperclips).hasSize(3);
            assertThat(paperclips).extracting(x -> x.getAttachedTo()).contains(invoice, invoice.getBuyer(), invoice.getSeller());

            // and when rendered
            runBackgroundCommands();

            summary = findSummary();
            prelimLetterViewModel = prelimLetterViewModelOf(summary);

            // then
            assertThat(mixin(DocAndCommForPrelimLetter_documentState.class, prelimLetterViewModel).$$()).isEqualTo(DocumentState.RENDERED);

            prelimLetterDoc = mixin(DocAndCommForPrelimLetter_document.class, prelimLetterViewModel).$$();
            assertThat(prelimLetterDoc.getState()).isEqualTo(DocumentState.RENDERED);
            assertThat(prelimLetterDoc.getRenderedAt()).isNotNull();
            assertThat(prelimLetterDoc.getSort()).isEqualTo(DocumentSort.BLOB);

            // and when send by email
            mixin(InvoiceSummaryForPropertyDueDateStatus_sendByEmailPreliminaryLetters.class, summary).$$();

            summary = findSummary();
            prelimLetterViewModel = prelimLetterViewModelOf(summary);

            // then
            final Communication prelimLetterComm = mixin(DocAndCommForPrelimLetter_communication.class, prelimLetterViewModel).$$();
            assertThat(prelimLetterComm).isNotNull();
            assertThat(mixin(DocAndCommForPrelimLetter_communicationState.class, prelimLetterViewModel).$$()).isEqualTo(CommunicationState.PENDING);

            // and when comm sent
            runBackgroundCommands();

            summary = findSummary();
            prelimLetterViewModel = prelimLetterViewModelOf(summary);

            // then
            assertThat(mixin(DocAndCommForPrelimLetter_communicationState.class, prelimLetterViewModel).$$()).isEqualTo(CommunicationState.SENT);
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

    void runBackgroundCommands() throws InterruptedException {

        List<CommandJdo> commands = backgroundCommandRepository.findBackgroundCommandsNotYetStarted();
        assertThat(commands).hasSize(1);

        BackgroundCommandExecutionFromBackgroundCommandServiceJdo backgroundExec =
                new BackgroundCommandExecutionFromBackgroundCommandServiceJdo();
        final SimpleSession session = new SimpleSession("scheduler_user", new String[] { "admin_role" });

        final Thread thread = new Thread(() -> backgroundExec.execute(session, null));
        thread.start();

        thread.join(5000L);

        transactionService.flushTransaction();

        commands = backgroundCommandRepository.findBackgroundCommandsNotYetStarted();
        assertThat(commands).isEmpty();
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
