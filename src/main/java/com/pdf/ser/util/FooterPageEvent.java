package com.pdf.ser.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class FooterPageEvent extends PdfPageEventHelper {

    Font footerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable footer = new PdfPTable(3);
        try {
            footer.setWidths(new int[]{1, 1, 1});
            footer.setTotalWidth(document.right() - document.left()); // Full page width
            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(20);

            // LEFT
            PdfPCell leftCell = new PdfPCell(new Phrase("REQ-00029-CAC", footerFont));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(leftCell);

            // CENTER
            PdfPCell centerCell = new PdfPCell(new Phrase("BV Reg. N 500070B", footerFont));
            centerCell.setBorder(Rectangle.NO_BORDER);
            centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footer.addCell(centerCell);

            // RIGHT
            PdfPCell rightCell = new PdfPCell(new Phrase("Page " + writer.getPageNumber(), footerFont));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.addCell(rightCell);

            footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 10, writer.getDirectContent());
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }
}
