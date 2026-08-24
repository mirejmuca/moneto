package com.moneto.api.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.moneto.api.model.Transaction;
import com.moneto.api.model.TransactionType;
import com.moneto.api.model.User;
import com.moneto.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final InsightsService insightsService;

    public byte[] generateReport() {
        subscriptionService.requireTier(com.moneto.api.model.SubscriptionTier.PLUS);

        User user = userService.getCurrentUser();
        String symbol = getCurrencySymbol(user.getCurrency().name());

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), monthStart, now);

        double income = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), user.getCurrency().name()))
                .mapToDouble(BigDecimal::doubleValue).sum();

        double expense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), user.getCurrency().name()))
                .mapToDouble(BigDecimal::doubleValue).sum();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        // Titull
        doc.add(new Paragraph("Moneto Financial Report")
                .setFontSize(20).setBold());
        doc.add(new Paragraph(user.getName() + "  •  " + now.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                .setFontSize(11).setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));
        doc.add(new Paragraph("\n"));

        // Përmbledhje
        doc.add(new Paragraph("Monthly Summary").setFontSize(14).setBold());
        Table summary = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth();
        summary.addCell(summaryCell("Income", symbol + Math.round(income)));
        summary.addCell(summaryCell("Expenses", symbol + Math.round(expense)));
        summary.addCell(summaryCell("Balance", symbol + Math.round(income - expense)));
        doc.add(summary);
        doc.add(new Paragraph("\n"));

        // Insighte
        Map<String, Object> insights = insightsService.getInsights();
        doc.add(new Paragraph("Insights").setFontSize(14).setBold());
        Table insightsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();
        addInsightRow(insightsTable, "Month-over-Month Change",
                insights.get("monthOverMonthChange") != null ? insights.get("monthOverMonthChange") + "%" : "N/A");
        addInsightRow(insightsTable, "Savings Rate",
                insights.get("savingsRate") != null ? insights.get("savingsRate") + "%" : "N/A");
        addInsightRow(insightsTable, "Daily Average", symbol + insights.get("dailyAverage"));
        addInsightRow(insightsTable, "Priciest Day",
                insights.get("mostExpensiveDay") != null ? insights.get("mostExpensiveDay").toString() : "N/A");
        addInsightRow(insightsTable, "Fastest Growing Category",
                insights.get("topGrowingCategory") != null ? insights.get("topGrowingCategory").toString() : "N/A");
        doc.add(insightsTable);
        doc.add(new Paragraph("\n"));

        // Transaksionet
        doc.add(new Paragraph("Transactions").setFontSize(14).setBold());
        Table txTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2}))
                .useAllAvailableWidth();
        txTable.addHeaderCell(headerCell("Date"));
        txTable.addHeaderCell(headerCell("Description"));
        txTable.addHeaderCell(headerCell("Category"));
        txTable.addHeaderCell(headerCell("Amount"));

        for (Transaction t : transactions) {
            txTable.addCell(new Cell().add(new Paragraph(t.getDate().toString()).setFontSize(9)));
            txTable.addCell(new Cell().add(new Paragraph(t.getDescription() != null ? t.getDescription() : "—").setFontSize(9)));
            txTable.addCell(new Cell().add(new Paragraph(t.getCategory() != null ? t.getCategory().getName() : "—").setFontSize(9)));
            String sign = t.getType() == TransactionType.INCOME ? "+" : "-";
            txTable.addCell(new Cell().add(new Paragraph(sign + getCurrencySymbol(t.getCurrency().name()) + t.getAmount()).setFontSize(9)));
        }
        doc.add(txTable);

        doc.close();
        return baos.toByteArray();
    }

    private Cell summaryCell(String label, String value) {
        Cell cell = new Cell();
        cell.add(new Paragraph(label).setFontSize(10).setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));
        cell.add(new Paragraph(value).setFontSize(16).setBold());
        return cell;
    }

    private void addInsightRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value).setFontSize(10).setBold().setTextAlignment(TextAlignment.RIGHT)));
    }

    private Cell headerCell(String text) {
        return new Cell().add(new Paragraph(text).setFontSize(10).setBold());
    }

    private String getCurrencySymbol(String currency) {
        return switch (currency) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "ALL" -> "L";
            case "CHF" -> "CHF ";
            case "CAD" -> "C$";
            case "AUD" -> "A$";
            case "JPY" -> "¥";
            default -> "";
        };
    }
}