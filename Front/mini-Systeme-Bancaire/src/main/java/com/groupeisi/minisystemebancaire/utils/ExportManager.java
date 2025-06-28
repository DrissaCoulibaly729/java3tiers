package com.groupeisi.minisystemebancaire.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.groupeisi.minisystemebancaire.dtos.ClientDTo;
import com.groupeisi.minisystemebancaire.dtos.CreditDTo;
import com.groupeisi.minisystemebancaire.dtos.TransactionDTo;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilitaire pour l'export de données en PDF et CSV
 */
public class ExportManager {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String CSV_SEPARATOR = ";";

    // ========== EXPORTS PDF ==========

    /**
     * Exporter une liste de transactions en PDF
     */
    public static void exportTransactionsToPdf(List<TransactionDTo> transactions, ClientDTo client, String filePath)
            throws DocumentException, IOException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        // Polices
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

        // Titre
        Paragraph title = new Paragraph("RELEVÉ DE TRANSACTIONS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date d'export
        Paragraph exportDate = new Paragraph("Généré le : " + LocalDateTime.now().format(DATETIME_FORMATTER), normalFont);
        exportDate.setAlignment(Element.ALIGN_RIGHT);
        exportDate.setSpacingAfter(15);
        document.add(exportDate);

        // Informations client
        if (client != null) {
            Paragraph clientInfo = new Paragraph();
            clientInfo.add(new Chunk("Client : ", headerFont));
            clientInfo.add(new Chunk(client.getPrenom() + " " + client.getNom(), normalFont));
            clientInfo.add(Chunk.NEWLINE);
            clientInfo.add(new Chunk("Email : ", headerFont));
            clientInfo.add(new Chunk(client.getEmail(), normalFont));
            if (client.getTelephone() != null) {
                clientInfo.add(Chunk.NEWLINE);
                clientInfo.add(new Chunk("Téléphone : ", headerFont));
                clientInfo.add(new Chunk(client.getTelephone(), normalFont));
            }
            clientInfo.setSpacingAfter(20);
            document.add(clientInfo);
        }

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
            double totalMontant = 0;
            for (TransactionDTo transaction : transactions) {
                addTableCell(table, transaction.getDateTransaction() != null ?
                        transaction.getDateTransaction().format(DATETIME_FORMATTER) : "-", normalFont);
                addTableCell(table, formatTransactionType(transaction.getType()), normalFont);
                addTableCell(table, transaction.getDescription() != null ? transaction.getDescription() : "-", normalFont);

                String montantStr = CurrencyFormatter.format(transaction.getMontant().doubleValue());
                addTableCell(table, montantStr, normalFont);
                addTableCell(table, formatTransactionStatus(transaction.getStatut()), normalFont);

                totalMontant += transaction.getMontant().doubleValue();
            }

            document.add(table);

            // Résumé
            Paragraph summary = new Paragraph();
            summary.setSpacingBefore(15);
            summary.add(new Chunk("Nombre de transactions : ", headerFont));
            summary.add(new Chunk(String.valueOf(transactions.size()), normalFont));
            summary.add(Chunk.NEWLINE);
            summary.add(new Chunk("Total des montants : ", headerFont));
            summary.add(new Chunk(CurrencyFormatter.format(totalMontant), normalFont));
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

        // Date d'export
        Paragraph exportDate = new Paragraph("Généré le : " + LocalDateTime.now().format(DATETIME_FORMATTER), normalFont);
        exportDate.setAlignment(Element.ALIGN_RIGHT);
        exportDate.setSpacingAfter(15);
        document.add(exportDate);

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
     * Exporter le profil client en PDF
     */
    public static void exportClientProfileToPdf(ClientDTo client, String filePath)
            throws DocumentException, IOException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

        // Titre
        Paragraph title = new Paragraph("PROFIL CLIENT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(30);
        document.add(title);

        if (client != null) {
            // Informations personnelles
            Paragraph section1 = new Paragraph("INFORMATIONS PERSONNELLES", headerFont);
            section1.setSpacingAfter(10);
            document.add(section1);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            addInfoRow(infoTable, "Nom", client.getNom(), headerFont, normalFont);
            addInfoRow(infoTable, "Prénom", client.getPrenom(), headerFont, normalFont);
            addInfoRow(infoTable, "Email", client.getEmail(), headerFont, normalFont);
            addInfoRow(infoTable, "Téléphone", client.getTelephone(), headerFont, normalFont);
            addInfoRow(infoTable, "Adresse", client.getAdresse(), headerFont, normalFont);
            addInfoRow(infoTable, "Date de naissance",
                    client.getDateNaissance() != null ? client.getDateNaissance().format(DATE_FORMATTER) : "-",
                    headerFont, normalFont);
            addInfoRow(infoTable, "Numéro CNI", client.getNumeroCNI(), headerFont, normalFont);
            addInfoRow(infoTable, "Date de création",
                    client.getDateCreation() != null ? client.getDateCreation().format(DATETIME_FORMATTER) : "-",
                    headerFont, normalFont);

            document.add(infoTable);
        }

        document.close();
    }

    // ========== EXPORTS CSV ==========

    /**
     * Exporter des transactions en CSV
     */
    public static void exportTransactionsToCSV(List<TransactionDTo> transactions, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            // BOM pour Excel
            writer.write('\ufeff');

            // En-têtes CSV
            writer.append("Date").append(CSV_SEPARATOR);
            writer.append("Type").append(CSV_SEPARATOR);
            writer.append("Description").append(CSV_SEPARATOR);
            writer.append("Montant").append(CSV_SEPARATOR);
            writer.append("Statut").append("\n");

            // Données
            if (transactions != null) {
                for (TransactionDTo transaction : transactions) {
                    writer.append(transaction.getDateTransaction() != null ?
                            transaction.getDateTransaction().format(DATETIME_FORMATTER) : "-").append(CSV_SEPARATOR);
                    writer.append(formatTransactionType(transaction.getType())).append(CSV_SEPARATOR);
                    writer.append(escapeCSV(transaction.getDescription())).append(CSV_SEPARATOR);
                    writer.append(String.valueOf(transaction.getMontant().doubleValue())).append(CSV_SEPARATOR);
                    writer.append(formatTransactionStatus(transaction.getStatut())).append("\n");
                }
            }
        }
    }

    /**
     * Exporter des données client en CSV
     */
    public static void exportClientDataToCSV(ClientDTo client, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            // BOM pour Excel
            writer.write('\ufeff');

            // Format : Label;Valeur
            writer.append("Information").append(CSV_SEPARATOR).append("Valeur").append("\n");

            if (client != null) {
                writer.append("ID Client").append(CSV_SEPARATOR).append(String.valueOf(client.getId())).append("\n");
                writer.append("Nom").append(CSV_SEPARATOR).append(escapeCSV(client.getNom())).append("\n");
                writer.append("Prénom").append(CSV_SEPARATOR).append(escapeCSV(client.getPrenom())).append("\n");
                writer.append("Email").append(CSV_SEPARATOR).append(escapeCSV(client.getEmail())).append("\n");
                writer.append("Téléphone").append(CSV_SEPARATOR).append(escapeCSV(client.getTelephone())).append("\n");
                writer.append("Adresse").append(CSV_SEPARATOR).append(escapeCSV(client.getAdresse())).append("\n");
                writer.append("Date de naissance").append(CSV_SEPARATOR)
                        .append(client.getDateNaissance() != null ? client.getDateNaissance().format(DATE_FORMATTER) : "-").append("\n");
                writer.append("Numéro CNI").append(CSV_SEPARATOR).append(escapeCSV(client.getNumeroCNI())).append("\n");
                writer.append("Date de création").append(CSV_SEPARATOR)
                        .append(client.getDateCreation() != null ? client.getDateCreation().format(DATETIME_FORMATTER) : "-").append("\n");
            }
        }
    }

    /**
     * Exporter des crédits en CSV
     */
    public static void exportCreditsToCSV(List<CreditDTo> credits, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            // BOM pour Excel
            writer.write('\ufeff');

            // En-têtes
            writer.append("Date demande").append(CSV_SEPARATOR);
            writer.append("Montant").append(CSV_SEPARATOR);
            writer.append("Durée (mois)").append(CSV_SEPARATOR);
            writer.append("Taux (%)").append(CSV_SEPARATOR);
            writer.append("Mensualité").append(CSV_SEPARATOR);
            writer.append("Statut").append("\n");

            // Données
            if (credits != null) {
                for (CreditDTo credit : credits) {
                    writer.append(credit.getDateCreation() != null ?
                            credit.getDateCreation().format(DATETIME_FORMATTER) : "-").append(CSV_SEPARATOR);
                    writer.append(String.valueOf(credit.getMontant().doubleValue())).append(CSV_SEPARATOR);
                    writer.append(String.valueOf(credit.getDureeEnMois())).append(CSV_SEPARATOR);
                    writer.append(String.valueOf(credit.getTauxInteret().doubleValue())).append(CSV_SEPARATOR);
                    writer.append(credit.getMensualite() != null ?
                            String.valueOf(credit.getMensualite().doubleValue()) : "0").append(CSV_SEPARATOR);
                    writer.append(formatCreditStatus(credit.getStatut())).append("\n");
                }
            }
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    // PDF - Méthodes utilitaires
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

    private static void addInfoRow(PdfPTable table, String label, String value,
                                   com.itextpdf.text.Font labelFont, com.itextpdf.text.Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label + " :", labelFont));
        labelCell.setPadding(5);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        valueCell.setPadding(5);
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    // CSV - Méthode pour échapper les caractères spéciaux
    private static String escapeCSV(String value) {
        if (value == null) return "-";
        if (value.contains(CSV_SEPARATOR) || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Méthodes de formatage
    private static String formatTransactionType(String type) {
        if (type == null) return "-";
        switch (type.toLowerCase()) {
            case "depot": return "Dépôt";
            case "retrait": return "Retrait";
            case "virement": return "Virement";
            case "transfert": return "Transfert";
            default: return type;
        }
    }

    private static String formatTransactionStatus(String status) {
        if (status == null) return "-";
        switch (status.toLowerCase()) {
            case "validee": return "Validée";
            case "en_attente": return "En attente";
            case "annulee": return "Annulée";
            case "rejetee": return "Rejetée";
            default: return status;
        }
    }

    private static String formatCreditStatus(String status) {
        if (status == null) return "-";
        switch (status.toLowerCase()) {
            case "en_attente": return "En attente";
            case "approuve": return "Approuvé";
            case "refuse": return "Refusé";
            case "rembourse": return "Remboursé";
            case "en_cours": return "En cours";
            default: return status;
        }
    }
}


