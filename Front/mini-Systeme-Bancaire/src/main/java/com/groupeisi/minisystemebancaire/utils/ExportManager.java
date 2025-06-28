package gm.rahmanproperties.optibank.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import gm.rahmanproperties.optibank.dtos.ClientDTo;
import gm.rahmanproperties.optibank.dtos.CreditDTo;
import gm.rahmanproperties.optibank.dtos.TransactionDTo;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportManager {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Export des transactions
    public static void exportTransactionsToPDF(List<TransactionDTo> transactions, ClientDTo client, String filePath) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // En-tête avec logo et informations client
            addHeader(document, "Relevé de transactions", client);

            // Tableau des transactions
            PdfPTable table = createTransactionTable(transactions);
            document.add(table);

            // Pied de page avec totaux
            addTransactionFooter(document, transactions);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    public static void exportTransactionsToExcel(List<TransactionDTo> transactions, ClientDTo client, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            // En-tête
            addExcelHeader(sheet, headerStyle, "Relevé de transactions - " + client.getNom() + " " + client.getPrenom());

            // Données
            addTransactionData(sheet, transactions, headerStyle, dateStyle, moneyStyle);

            // Auto-dimensionnement
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }

    // Export des crédits
    public static void exportCreditsToPDF(List<CreditDTo> credits, ClientDTo client, String filePath) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            addHeader(document, "Synthèse des crédits", client);
            PdfPTable table = createCreditTable(credits);
            document.add(table);
            addCreditFooter(document, credits);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    // Méthodes privées d'aide
    private static void addHeader(Document document, String title, ClientDTo client) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Paragraph header = new Paragraph(title, titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        // Informations client
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);

        addInfoRow(infoTable, "Client:", client.getNom() + " " + client.getPrenom());
        addInfoRow(infoTable, "Email:", client.getEmail());
        addInfoRow(infoTable, "Date d'édition:", LocalDateTime.now().format(DATE_FORMATTER));

        document.add(infoTable);
    }

    private static PdfPTable createTransactionTable(List<TransactionDTo> transactions) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        // En-têtes
        addTableHeader(table, "Date", "Type", "Montant", "Statut", "Description");

        // Données
        for (TransactionDTo transaction : transactions) {
            table.addCell(new PdfPCell(new Phrase(transaction.getDateTransaction().format(DATE_FORMATTER))));
            table.addCell(new PdfPCell(new Phrase(transaction.getType().toString())));
            table.addCell(new PdfPCell(new Phrase(formatMontant(transaction.getMontant()))));
            table.addCell(new PdfPCell(new Phrase(transaction.getStatut().toString())));
            table.addCell(new PdfPCell(new Phrase(transaction.getDescription())));
        }

        return table;
    }

    private static void addInfoRow(PdfPTable table, String label, String value) {
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        table.addCell(new PdfPCell(new Phrase(label, boldFont)));
        table.addCell(new PdfPCell(new Phrase(value)));
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }
    }

    private static String formatMontant(java.math.BigDecimal montant) {
        return String.format("%,.2f €", montant);
    }

    private static void addTransactionFooter(Document document, List<TransactionDTo> transactions) throws DocumentException {
        // Implémentez le pied de page selon vos besoins
    }

    private static void addCreditFooter(Document document, List<CreditDTo> credits) throws DocumentException {
        // Implémentez le pied de page selon vos besoins
    }

    // Méthodes pour Excel (à implémenter)
    private static CellStyle createHeaderStyle(Workbook workbook) {
        return workbook.createCellStyle();
    }

    private static CellStyle createDateStyle(Workbook workbook) {
        return workbook.createCellStyle();
    }

    private static CellStyle createMoneyStyle(Workbook workbook) {
        return workbook.createCellStyle();
    }

    private static void addExcelHeader(Sheet sheet, CellStyle headerStyle, String title) {
        // Implémentez selon vos besoins
    }

    private static void addTransactionData(Sheet sheet, List<TransactionDTo> transactions,
                                           CellStyle headerStyle, CellStyle dateStyle, CellStyle moneyStyle) {
        // Implémentez selon vos besoins
    }

    private static PdfPTable createCreditTable(List<CreditDTo> credits) {
        // Implémentez selon vos besoins
        return new PdfPTable(1);
    }
}
