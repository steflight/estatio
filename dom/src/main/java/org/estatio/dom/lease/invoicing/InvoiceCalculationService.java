package org.estatio.dom.lease.invoicing;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.estatio.dom.charge.Charge;
import org.estatio.dom.lease.InvoicingFrequency;
import org.estatio.dom.lease.LeaseItem;
import org.estatio.dom.lease.LeaseTerm;
import org.estatio.dom.tax.Tax;
import org.estatio.dom.utils.CalendarUtils;
import org.estatio.services.appsettings.EstatioSettingsService;

import org.estatio.dom.utils.LocalDateInterval;
import org.estatio.dom.utils.LocalDateInterval.IntervalEnding;
import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Hidden;

@Hidden
public class InvoiceCalculationService {

    public void calculateAndInvoiceItems(LeaseTerm leaseTerm, LocalDate startDate, LocalDate dueDate) {
        calculateAndInvoiceItems(leaseTerm, startDate, dueDate, leaseTerm.getLeaseItem().getInvoicingFrequency());
    }

    public void calculateAndInvoiceItems(LeaseTerm leaseTerm, LocalDate startDate, LocalDate dueDate, InvoicingFrequency freq) {
        CalculationResult result = calculate(leaseTerm, startDate, dueDate, freq);
        createInvoiceItems(leaseTerm, startDate, dueDate, result, freq);
    }

    static class CalculationResult {
        BigDecimal value = BigDecimal.ZERO.setScale(2);
        LocalDateInterval periodInterval;

        public BigDecimal getCalculatedValue() {
            return value;
        }
    }

    CalculationResult calculate(LeaseTerm leaseTerm, LocalDate periodStartDate, LocalDate dueDate) {
        return calculate(leaseTerm, periodStartDate, dueDate, leaseTerm.getLeaseItem().getInvoicingFrequency());
    }

    public CalculationResult calculate(LeaseTerm leaseTerm, LocalDate periodStartDate, LocalDate dueDate, InvoicingFrequency freq) {
        CalculationResult result = new CalculationResult();
        result.periodInterval = new LocalDateInterval(CalendarUtils.intervalMatching(periodStartDate, freq.rrule));
        if (result.periodInterval.getStartDate() != null) {
            LocalDateInterval termInterval = new LocalDateInterval(leaseTerm.getStartDate(), leaseTerm.getEndDate(), IntervalEnding.INCLUDING_END_DATE);
            LocalDateInterval overlap = result.periodInterval.overlap(termInterval);
            if (overlap != null) {
                BigDecimal boundingRangeDays = new BigDecimal(overlap.getDays());
                BigDecimal rangeDays = new BigDecimal(termInterval.getDays());
                BigDecimal rangeFactor = rangeDays.divide(boundingRangeDays, MathContext.DECIMAL64);
                BigDecimal freqFactor = freq.numerator.divide(freq.denominator, MathContext.DECIMAL64);
                BigDecimal currentValue = leaseTerm.valueForDueDate(dueDate);
                if (currentValue != null && freqFactor != null && rangeFactor != null) {
                    result.value = currentValue.multiply(freqFactor).multiply(rangeFactor).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        return result;
    }

    public BigDecimal calculatedValue(LeaseTerm leaseTerm, LocalDate periodStartDate, LocalDate dueDate, InvoicingFrequency freq) {
        CalculationResult result = calculate(leaseTerm, periodStartDate, dueDate, freq);
        return result.getCalculatedValue();
    }

    void createInvoiceItems(LeaseTerm leaseTerm, LocalDate startDate, LocalDate dueDate, CalculationResult calculationResult, InvoicingFrequency freq) {
        if (calculationResult.value != null) {
            BigDecimal invoicedValue;
            LocalDate mockDate = estatioSettingsService.fetchEpochDate();
            if (mockDate != null && startDate.compareTo(mockDate) < 0) {
                CalculationResult mockResult = calculate(leaseTerm, startDate, startDate, freq);
                invoicedValue = mockResult.getCalculatedValue();
            } else {
                invoicedValue = leaseTerm.invoicedValueFor(startDate);
            }
            BigDecimal newValue = calculationResult.value.subtract(invoicedValue);
            if (newValue.compareTo(BigDecimal.ZERO) != 0) {
                InvoiceItemForLease invoiceItem = leaseTerm.findOrCreateUnapprovedInvoiceItemFor(startDate, dueDate);
                invoiceItem.setNetAmount(newValue);
                invoiceItem.setQuantity(BigDecimal.ONE);
                LeaseItem leaseItem = leaseTerm.getLeaseItem();
                Charge charge = leaseItem.getCharge();
                invoiceItem.setCharge(charge);
                invoiceItem.setDescription(charge.getDescription());
                invoiceItem.setDueDate(dueDate);
                invoiceItem.setStartDate(calculationResult.periodInterval.getStartDate());
                invoiceItem.setEndDate(calculationResult.periodInterval.getEndDate());
                Tax tax = charge.getTax();
                invoiceItem.setTax(tax);
                invoiceItem.attachToInvoice();
                invoiceItem.verify();
            }
        }
    }

    // {{ injected: EstatioSettingsService
    private EstatioSettingsService estatioSettingsService;

    public void setEstatioSettings(final EstatioSettingsService estatioSettings) {
        this.estatioSettingsService = estatioSettings;
    }
    // }}

}
