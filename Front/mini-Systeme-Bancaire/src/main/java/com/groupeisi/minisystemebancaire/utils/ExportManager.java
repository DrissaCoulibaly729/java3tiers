package com.groupeisi.minisystemebancaire.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.groupeisi.minisystemebancaire.dtos.ClientDTo;
import com.groupeisi.minisystemebancaire.dtos.CreditDTo;
import com.groupeisi.minisystemebancaire.dtos.TransactionDTo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilitaire pour l'export de données en PDF et Excel
 */
public class ExportManager {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exporter une liste de transactions en PDF
     */
    public static void exportTransactionsToPdf(List<TransactionDTo> transactions, ClientDTo client, String filePath)
            throws DocumentException, IOException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        // En-tête du document avec les bonnes classes Font
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

        // Titre
        Paragraph title = new Paragraph("RELEVÉ DE TRANSACTIONS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Informations client
        if (client != null) {
            Paragraph clientInfo = new Paragraph();
            clientInfo.add(new Chunk("Client : ", headerFont));
            clientInfo.add(new Chunk(client.getPrenom() + " " + client.getNom(), normalFont));
            clientInfo.add(Chunk.NEWLINE);
            clientInfo.add(new Chunk("Email : ", headerFont));
            clientInfo.add(new Chunk(client.getEmail(), normalFont));
            clientInfo.setSpacingAfter(20);
            document.add(clientInfo);
        }

        // Date de génération
        Paragraph dateGen = new Paragraph("Généré le : " + java.time.LocalDateTime.now().format(DATETIME_FORMATTER), normalFont);
        dateGen.setAlignment(Element.ALIGN_RIGHT);
        dateGen.setSpacingAfter(20);
        document.add(dateGen);

        // Table des transactions
        if (transactions != null && !transactions.isEmpty()) {
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            // En-têtes
            addTableHeader(table, "Date", headerFont);
            addTableHeader(table, "Type", headerFont);
            addTableHeader(table, "Description", headerFont);
            addTableHeader(table, "Montant", headerFont);
            addTableHeader(table, "Statut", headerFont);

            // Données
            for (TransactionDTo transaction : transactions) {
                addTableCell(table, transaction.getDateTransaction() != null ?
                        transaction.getDateTransaction().format(DATETIME_FORMATTER) : "-", normalFont);
                addTableCell(table, formatTransactionType(transaction.getType()), normalFont);
                addTableCell(table, transaction.getDescription() != null ? transaction.getDescription() : "-", normalFont);
                addTableCell(table, CurrencyFormatter.format(transaction.getMontant().doubleValue()), normalFont);
                addTableCell(table, formatTransactionStatus(transaction.getStatut()), normalFont);
            }

            document.add(table);

            // Résumé
            BigDecimal totalCredits = transactions.stream()
                    .filter(t -> "depot".equals(t.getType()))
                    .map(TransactionDTo::getMontant)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDebits = transactions.stream()
                    .filter(t -> "retrait".equals(t.getType()) || "virement".equals(t.getType()))
                    .map(TransactionDTo::getMontant)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Paragraph summary = new Paragraph();
            summary.setSpacingBefore(20);
            summary.add(new Chunk("Total des crédits : ", headerFont));
            summary.add(new Chunk(CurrencyFormatter.format(totalCredits.doubleValue()), normalFont));
            summary.add(Chunk.NEWLINE);
            summary.add(new Chunk("Total des débits : ", headerFont));
            summary.add(new Chunk(CurrencyFormatter.format(totalDebits.doubleValue()), normalFont));
            document.add(summary);
        } else {
            Paragraph noData = new Paragraph("Aucune transaction trouvée.", normalFont);
            noData.setAlignment(Element.ALIGN_CENTER);
            document.add(noData);
        }

        document.close();
    }

    /**
     * Exporter une liste de crédits en PDF
     */
    public static void exportCreditsToPdf(List<CreditDTo> credits, ClientDTo client, String filePath)
            throws DocumentException, IOException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

        // Titre
        Paragraph title = new Paragraph("HISTORIQUE DES CRÉDITS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Informations client
        if (client != null) {
            Paragraph clientInfo = new Paragraph();
            clientInfo.add(new Chunk("Client : ", headerFont));
            clientInfo.add(new Chunk(client.getPrenom() + " " + client.getNom(), normalFont));
            clientInfo.setSpacingAfter(20);
            document.add(clientInfo);
        }

        // Table des crédits
        if (credits != null && !credits.isEmpty()) {
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // En-têtes
            addTableHeader(table, "Date demande", headerFont);
            addTableHeader(table, "Montant", headerFont);
            addTableHeader(table, "Durée", headerFont);
            addTableHeader(table, "Taux", headerFont);
            addTableHeader(table, "Mensualité", headerFont);
            addTableHeader(table, "Statut", headerFont);

            // Données
            for (CreditDTo credit : credits) {
                addTableCell(table, credit.getDateCreation() != null ?
                        credit.getDateCreation().format(DATETIME_FORMATTER) : "-", normalFont);
                addTableCell(table, CurrencyFormatter.format(credit.getMontant().doubleValue()), normalFont);
                addTableCell(table, credit.getDureeEnMois() + " mois", normalFont);
                addTableCell(table, credit.getTauxInteret() + " %", normalFont);
                addTableCell(table, credit.getMensualite() != null ?
                        CurrencyFormatter.format(credit.getMensualite().doubleValue()) : "-", normalFont);
                addTableCell(table, formatCreditStatus(credit.getStatut()), normalFont);
            }

            document.add(table);
        } else {
            Paragraph noData = new Paragraph("Aucun crédit trouvé.", normalFont);
            noData.setAlignment(Element.ALIGN_CENTER);
            document.add(noData);
        }

        document.close();
    }

    /**
     * Exporter des données client en Excel
     */
    public static void exportClientDataToExcel(ClientDTo client, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Feuille informations client
        Sheet clientSheet = workbook.createSheet("Informations Client");

        int rowNum = 0;
        Row row;
        Cell cell;

        // En-tête
        row = clientSheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("INFORMATIONS CLIENT");

        // Style pour les en-têtes Excel
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        cell.setCellStyle(headerStyle);

        rowNum++; // Ligne vide

        // Données client
        if (client != null) {
            addExcelRow(clientSheet, rowNum++, "ID Client", client.getId().toString());
            addExcelRow(clientSheet, rowNum++, "Nom", client.getNom());
            addExcelRow(clientSheet, rowNum++, "Prénom", client.getPrenom());
            addExcelRow(clientSheet, rowNum++, "Email", client.getEmail());
            addExcelRow(clientSheet, rowNum++, "Téléphone", client.getTelephone());
            addExcelRow(clientSheet, rowNum++, "Adresse", client.getAdresse());
            addExcelRow(clientSheet, rowNum++, "Date de naissance",
                    client.getDateNaissance() != null ? client.getDateNaissance().format(DATE_FORMATTER) : "-");
            addExcelRow(clientSheet, rowNum++, "Numéro CNI", client.getNumeroCNI());
            addExcelRow(clientSheet, rowNum++, "Date de création",
                    client.getDateCreation() != null ? client.getDateCreation().format(DATETIME_FORMATTER) : "-");
        }

        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            clientSheet.autoSizeColumn(i);
        }

        // Sauvegarder le fichier
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }

    /**
     * Exporter des transactions en Excel
     */
    public static void exportTransactionsToExcel(List<TransactionDTo> transactions, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");

        // Style pour les en-têtes Excel
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // En-têtes
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Type", "Description", "Montant", "Statut"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Données
        if (transactions != null) {
            int rowNum = 1;
            for (TransactionDTo transaction : transactions) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(transaction.getDateTransaction() != null ?
                        transaction.getDateTransaction().format(DATETIME_FORMATTER) : "-");
                row.createCell(1).setCellValue(formatTransactionType(transaction.getType()));
                row.createCell(2).setCellValue(transaction.getDescription() != null ? transaction.getDescription() : "-");
                row.createCell(3).setCellValue(transaction.getMontant().doubleValue());
                row.createCell(4).setCellValue(formatTransactionStatus(transaction.getStatut()));
            }
        }

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        // Sauvegarder
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }

    /**
     * Exporter des crédits en Excel
     */
    public static void exportCreditsToExcel(List<CreditDTo> credits, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Crédits");

        // Style pour les en-têtes Excel
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // En-têtes
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date demande", "Montant", "Durée (mois)", "Taux (%)", "Mensualité", "Statut"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Données
        if (credits != null) {
            int rowNum = 1;
            for (CreditDTo credit : credits) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(credit.getDateCreation() != null ?
                        credit.getDateCreation().format(DATETIME_FORMATTER) : "-");
                row.createCell(1).setCellValue(credit.getMontant().doubleValue());
                row.createCell(2).setCellValue(credit.getDureeEnMois());
                row.createCell(3).setCellValue(credit.getTauxInteret().doubleValue());
                row.createCell(4).setCellValue(credit.getMensualite() != null ?
                        credit.getMensualite().doubleValue() : 0);
                row.createCell(5).setCellValue(formatCreditStatus(credit.getStatut()));
            }
        }

        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        // Sauvegarder
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }

    // Méthodes utilitaires privées pour PDF
    private static void addTableHeader(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    // Méthode utilitaire pour Excel
    private static void addExcelRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value != null ? value : "-");
    }

    // Méthodes de formatage
    private static String formatTransactionType(String type) {
        switch (type.toLowerCase()) {
            case "depot": return "Dépôt";
            case "retrait": return "Retrait";
            case "virement": return "Virement";
            default: return type;
        }
    }

    private static String formatTransactionStatus(String status) {
        switch (status.toLowerCase()) {
            case "validee": return "Validée";
            case "en_attente": return "En attente";
            case "annulee": return "Annulée";
            default: return status;
        }
    }

    private static String formatCreditStatus(String status) {
        switch (status.toLowerCase()) {
            case "en_attente": return "En attente";
            case "approuve": return "Approuvé";
            case "refuse": return "Refusé";
            case "rembourse": return "Remboursé";
            default: return status;
        }
    }
}