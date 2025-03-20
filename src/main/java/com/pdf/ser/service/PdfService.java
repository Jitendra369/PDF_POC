package com.pdf.ser.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.*;
import com.pdf.ser.model.PDFDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class PdfService {

    public static final int FONT_GLOBAL_SIZE = 8;
    public void createPdf(PDFDto pdfDto) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfDto.getFilePath()));
            document.open();
            document.add(new Paragraph(" hell0, this is sample pdf data"));
            document.close();
            System.out.println("PDF file created successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readPdfFile(PDFDto pdfDto) throws IOException, DocumentException {
        File file = new File(pdfDto.getFilePath());
        if (!file.exists()) {
            return "PDF file does not exist in the path " + pdfDto.getFilePath();
        }
        PdfReader pdfReader = new PdfReader(pdfDto.getFilePath());
        if (pdfReader.isEncrypted()) {
            return "PDf is encrypted , and cannot read the file";
        }
        int numberOfPages = pdfReader.getNumberOfPages();

        StringBuilder sb = new StringBuilder();
        log.info("total number of pages in pdf is " + numberOfPages);

        for (int i = 1; i <= numberOfPages; i++) {
            sb.append(PdfTextExtractor.getTextFromPage(pdfReader, i));
            sb.append("\n");
        }
        pdfReader.close();
        return sb.toString();
    }

    public String readAndAddTextToPDF(String inputFilePath, String outputFilePath, String textToAdd) {
        PdfReader reader = null;
        PdfStamper pdfStamper = null;
        try {

            File file = new File(inputFilePath);
            if (!file.exists()) {
                return "PDF file does not exist in the path " + inputFilePath;
            }
            PdfReader pdfReader = new PdfReader(inputFilePath);
            if (pdfReader.isEncrypted()) {
                return "PDf is encrypted , and cannot read the file";
            }
            reader = new PdfReader(inputFilePath);
            pdfStamper = new PdfStamper(reader, new FileOutputStream(outputFilePath));

            int numberOfPages = reader.getNumberOfPages();
            log.info("total number of pages in pdf is " + numberOfPages);

            StringBuilder content = new StringBuilder();
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                String pageText = PdfTextExtractor.getTextFromPage(reader, i);
                content.append("page ").append(i).append(":\n").append(pageText).append("\n");

//                add new text to old pdf
                PdfContentByte canvas = pdfStamper.getOverContent(i);
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_CENTER,
                        new Phrase(textToAdd),
                        300,
                        50,
                        0
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pdfStamper != null) {
                try {
                    pdfStamper.close();
                } catch (DocumentException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
        return "PDf id ctreated successfully";
    }

    public String modifyPDF(PDFDto pdfDto) {
        PdfReader reader = null;
        PdfStamper stamper = null;

        try {
            readCoordinates(pdfDto.getFilePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try {
            File file = new File(pdfDto.getFilePath());
            if (!file.exists()) {
                return "PDF file does not exist at path: " + pdfDto.getFilePath();
            }

            reader = new PdfReader(pdfDto.getFilePath());
            if (reader.getNumberOfPages() < 4) {
                return "Pdf does not have 4 page";
            }
            stamper = new PdfStamper(reader, new FileOutputStream(pdfDto.getNewFilePathToStore()));

            int numberOfPages = reader.getNumberOfPages();
            StringBuilder content = new StringBuilder();

            for (int i = 1; i <= numberOfPages; i++) {
                String pageText = PdfTextExtractor.getTextFromPage(reader, i);
                content.append(pageText).append("\n");

                PdfContentByte canvas = stamper.getOverContent(i);
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                canvas.setFontAndSize(bf, 12);

                // Example positions - Adjust coordinates according to your PDF structure
                if (pageText.contains("Name")) {
                    canvas.beginText();
                    canvas.setTextMatrix(50, 560); // Position near "Name:"
                    canvas.showText(pdfDto.getUserInfoDto().getName());
                    canvas.endText();
                }
                if (pageText.contains("Address")) {
                    canvas.beginText();
                    canvas.setTextMatrix(250, 730); // Position near "Address:"
                    canvas.showText(pdfDto.getUserInfoDto().getAddress());
                    canvas.endText();
                }
                if (pageText.contains("Pin")) {
                    canvas.beginText();
                    canvas.setTextMatrix(250, 710); // Position near "Pin:"
                    canvas.showText(pdfDto.getUserInfoDto().getPin());
                    canvas.endText();
                }
                if (pageText.contains("Location")) {
                    canvas.beginText();
                    canvas.setTextMatrix(150, 690); // Position near "Location:"
                    canvas.showText(pdfDto.getUserInfoDto().getLocation());
                    canvas.endText();
                }

                if (pageText.contains("Function")) {
                    canvas.beginText();
                    canvas.setTextMatrix(55, 545); // Position near "Function:"
                    canvas.showText(pdfDto.getUserInfoDto().getLocation());
                    canvas.endText();
                }
            }

            stamper.setFormFlattening(true);
            return "PDF modified successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while modifying PDF: " + e.getMessage();
        } finally {
            try {
                if (stamper != null) stamper.close();
                if (reader != null) reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String modifyPDFV1(PDFDto pdfDto) {
        PdfReader reader = null;
        PdfStamper stamper = null;

        try {
            File file = new File(pdfDto.getFilePath());
            if (!file.exists()) {
                return "PDF file does not exist at path: " + pdfDto.getFilePath();
            }

            reader = new PdfReader(pdfDto.getFilePath());
            if (reader.getNumberOfPages() < 4) {
                return "PDF does not have 4 pages.";
            }

            stamper = new PdfStamper(reader, new FileOutputStream(pdfDto.getNewFilePathToStore()));

            // Process only the 4th page
            int targetPage = 4;
            PdfContentByte canvas = stamper.getOverContent(targetPage);
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            canvas.setFontAndSize(bf, 12);
            canvas.setColorFill(BaseColor.BLUE); // Optional: Set color for new text

            // Search and replace text on the 4th page
            PdfTextExtractor.getTextFromPage(reader, targetPage, new SimpleTextExtractionStrategy() {
                @Override
                public void renderText(TextRenderInfo renderInfo) {
                    String text = renderInfo.getText();
                    Vector start = renderInfo.getBaseline().getStartPoint();

                    try {
                        if (text.contains("Name")) {
                            canvas.beginText();
                            canvas.setTextMatrix(start.get(Vector.I1) + 50, start.get(Vector.I2)); // Adjust position
                            canvas.showText(pdfDto.getUserInfoDto().getName());
                            canvas.endText();
                        }
                        if (text.contains("Address")) {
                            canvas.beginText();
                            canvas.setTextMatrix(start.get(Vector.I1) + 50, start.get(Vector.I2));
                            canvas.showText(pdfDto.getUserInfoDto().getAddress());
                            canvas.endText();
                        }
                        if (text.contains("Pin")) {
                            canvas.beginText();
                            canvas.setTextMatrix(start.get(Vector.I1) + 50, start.get(Vector.I2));
                            canvas.showText(pdfDto.getUserInfoDto().getPin());
                            canvas.endText();
                        }
                        if (text.contains("Location")) {
                            canvas.beginText();
                            canvas.setTextMatrix(start.get(Vector.I1) + 50, start.get(Vector.I2));
                            canvas.showText(pdfDto.getUserInfoDto().getLocation());
                            canvas.endText();
                        }
                        if (text.contains("Function")) {
                            canvas.beginText();
                            canvas.setTextMatrix(start.get(Vector.I1) + 50, start.get(Vector.I2));
                            canvas.showText(pdfDto.getUserInfoDto().getLocation());
                            canvas.endText();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            stamper.setFormFlattening(true);
            return "PDF modified successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while modifying PDF: " + e.getMessage();
        } finally {
            try {
                if (stamper != null) stamper.close();
                if (reader != null) reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String modifyPDFV5(PDFDto pdfDto) {
        PdfReader reader = null;
        PdfStamper stamper = null;

        try {
            File file = new File(pdfDto.getFilePath());
            if (!file.exists()) {
                return "PDF file does not exist at path: " + pdfDto.getFilePath();
            }

            reader = new PdfReader(pdfDto.getFilePath());
            if (reader.getNumberOfPages() < 4) {
                return "PDF does not have 4 pages.";
            }

            stamper = new PdfStamper(reader, new FileOutputStream(pdfDto.getNewFilePathToStore()));

            // Target the 4th page
            int targetPage = 4;
            PdfContentByte canvas = stamper.getOverContent(targetPage);
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            canvas.setFontAndSize(bf, 12);
            canvas.setColorFill(BaseColor.BLUE);

            // Track occurrences for "Name"
            final int[] nameCount = {0};

            PdfTextExtractor.getTextFromPage(reader, targetPage, new SimpleTextExtractionStrategy() {
                @Override
                public void renderText(TextRenderInfo renderInfo) {
                    String text = renderInfo.getText();
                    Vector start = renderInfo.getBaseline().getStartPoint();

                    try {
                        if (text.contains("Name")) {
                            nameCount[0]++;
                            if (nameCount[0] == 2) { // Modify only the second "Name"
                                System.out.printf("Modifying second 'Name' at x = %.2f, y = %.2f%n",
                                        start.get(Vector.I1), start.get(Vector.I2));
                                addText(canvas, pdfDto.getUserInfoDto().getName(), start, 50, 0); // Offset to the right
                            }
                        }
                        // Add text without replacing
                        if (text.contains("At")) {
                            addText(canvas, pdfDto.getUserInfoDto().getLocation(), start, 70, 0); // Add next to "At"
                        }
                        if (text.contains("on")) {
                            nameCount[0]++;
                            if (nameCount[0] == 2) { // Modify only the second "Name"
                                System.out.printf("Modifying second 'Name' at x = %.2f, y = %.2f%n",
                                        start.get(Vector.I1), start.get(Vector.I2));
                                addText(canvas, pdfDto.getUserInfoDto().getName(), start, 50, 0); // Offset to the right
                            }
                        }
//                        if (text.contains("on")) {
//                            addText(canvas, pdfDto.getUserInfoDto().getDate(), start, 70, 0); // Add next to "On"
//                        }
                        if (text.contains("Function")) {
                            addText(canvas, pdfDto.getUserInfoDto().getFunction(), start, 70, 0); // Add next to "Function"
                        }
//                        if (text.contains("Signature")) {
//                            addText(canvas, pdfDto.getUserInfoDto().getSignature(), start, 70, 0); // Add next to "Signature"
//                        }

                        if (text.contains("Signature")) {
                            nameCount[0]++;
                            if (nameCount[0] == 2) { // Modify only the second "Name"
                                System.out.printf("Modifying second 'Name' at x = %.2f, y = %.2f%n",
                                        start.get(Vector.I1), start.get(Vector.I2));
                                addText(canvas, pdfDto.getUserInfoDto().getName(), start, 50, 0); // Offset to the right
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            stamper.setFormFlattening(true);
            return "PDF modified successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while modifying PDF: " + e.getMessage();
        } finally {
            try {
                if (stamper != null) stamper.close();
                if (reader != null) reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ✅ Add text beside or below the existing text without replacing it
    private void addText(PdfContentByte canvas, String value, Vector position, float offsetX, float offsetY) {
        try {
            canvas.beginText();
            canvas.setTextMatrix(position.get(Vector.I1) + offsetX, position.get(Vector.I2) + offsetY);
            canvas.showText(value);
            canvas.endText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String modifyPDFV3(PDFDto pdfDto) {
        PdfReader reader = null;
        PdfStamper stamper = null;

        try {
            File file = new File(pdfDto.getFilePath());
            if (!file.exists()) {
                return "PDF file does not exist at path: " + pdfDto.getFilePath();
            }

            reader = new PdfReader(pdfDto.getFilePath());
            if (reader.getNumberOfPages() < 4) {
                return "PDF does not have 4 pages.";
            }

            stamper = new PdfStamper(reader, new FileOutputStream(pdfDto.getNewFilePathToStore()));
            PdfContentByte canvas = stamper.getOverContent(4);
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            canvas.setFontAndSize(bf, 12);
            canvas.setColorFill(BaseColor.BLUE);

            // Track occurrences for each field
            final int[] nameCount = {0};
            final int[] atCount = {0};
            final int[] onCount = {0};
            final int[] functionCount = {0};
            final int[] signatureCount = {0};

            // Extract text and coordinates
            PdfTextExtractor.getTextFromPage(reader, 4, new SimpleTextExtractionStrategy() {
                @Override
                public void renderText(TextRenderInfo renderInfo) {
                    String text = renderInfo.getText();
                    Vector start = renderInfo.getBaseline().getStartPoint();

                    try {
                        // Modify second occurrence of "Name"
                        if (text.contains("Name")) {
                            nameCount[0]++;
                            if (nameCount[0] == 2) { // Modify only the second "Name"
                                writeText(canvas, pdfDto.getUserInfoDto().getName(), start);
                            }
                        }

                        // Modify second occurrence of "At"
                        if (text.contains("At")) {
                            atCount[0]++;
                            if (atCount[0] == 2) {
                                writeText(canvas, pdfDto.getUserInfoDto().getLocation(), start);
                            }
                        }

                        // Modify second occurrence of "On"
                        if (text.contains("On")) {
                            onCount[0]++;
                            if (onCount[0] == 2) {
                                writeText(canvas, pdfDto.getUserInfoDto().getPin(), start);
                            }
                        }

                        // Modify second occurrence of "Function"
                        if (text.contains("Function")) {
                            functionCount[0]++;
                            if (functionCount[0] == 2) {
                                writeText(canvas, pdfDto.getUserInfoDto().getAddress(), start);
                            }
                        }

                        // Modify first occurrence of "Signature" (example)
//                        if (text.contains("Signature")) {
//                            signatureCount[0]++;
//                            if (signatureCount[0] == 1) {
//                                writeText(canvas, pdfDto.getUserInfoDto().getName(), start);
//                            }
//                        }

                        if (text.contains("Signature:")) {
                            nameCount[0]++;
                            if (nameCount[0] == 3) { // Modify only the second "Name"
                                writeText(canvas, pdfDto.getUserInfoDto().getSignature(), start);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            stamper.setFormFlattening(true);
            return "PDF modified successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while modifying PDF: " + e.getMessage();
        } finally {
            try {
                if (stamper != null) stamper.close();
                if (reader != null) reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Helper method to write text at specific coordinates
    private void writeText(PdfContentByte canvas, String value, Vector position) {
        try {
            canvas.beginText();
            // Adjust x and y to position the value correctly
            canvas.setTextMatrix(position.get(Vector.I1) + 50, position.get(Vector.I2));
            canvas.showText(value);
            canvas.endText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readCoordinates(String inputFilePath) throws IOException {
        String filePath = inputFilePath;
        try {
            PdfReader reader = new PdfReader(filePath);
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);

            // Extract text and its position using an anonymous RenderListener
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                System.out.println("\nPage " + i + ":\n");

                parser.processContent(i, new RenderListener() {

                    @Override
                    public void beginTextBlock() {
                        System.out.println("---- Beginning of text block ----");
                    }

                    @Override
                    public void renderText(TextRenderInfo renderInfo) {
                        String text = renderInfo.getText(); // Extract the text
                        Vector start = renderInfo.getBaseline().getStartPoint(); // Get position
                        float x = start.get(Vector.I1); // X-coordinate
                        float y = start.get(Vector.I2); // Y-coordinate
                        System.out.printf("Text: '%s' at x = %.2f, y = %.2f%n", text, x, y);
                    }

                    @Override
                    public void renderImage(com.itextpdf.text.pdf.parser.ImageRenderInfo renderInfo) {
                        System.out.println("Image detected on the page.");
                    }

                    @Override
                    public void endTextBlock() {
                        System.out.println("---- End of text block ----");
                    }
                });
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addSpaceLine(Document document ,int number){
        for (int i = 1;i<= number ; i++){
            try {
                document.add(new Paragraph(" "));
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private Chunk addSpace(int numberOfSpace){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfSpace; i++) {
            sb.append(" ");
        }
        return new Chunk(sb.toString());
    }
    public void createPDF() {
        String outputFile = "demo_pdf_page.pdf";

        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            addSpaceLine(document, 4);

            Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE, Font.BOLD);
            Paragraph header = new Paragraph("The Company Representative", headerFont);
            header.setSpacingAfter(10);
            document.add(header);

            Font listFont = new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE);
            List bulletList = new List(List.UNORDERED);
            bulletList.setListSymbol(new Chunk(" •   ", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE))); // Small round bullet
            bulletList.setIndentationLeft(20); // Add left space before bullet points

            bulletList.add(new ListItem("Declares to be fully authorized to sign the present request",listFont));
            bulletList.add(new ListItem("Declares to be well aware and accepts the Bureau Veritas Marine & Offshore General Conditions given above",listFont));
            bulletList.add(new ListItem("Agrees to comply with Bureau Veritas Rules",listFont));

            document.add(bulletList);

            addSpaceLine(document, 2);

            String location = " Piraeus-GREECE ";
            String date = " 23 Jan 2025 ";

            Paragraph paragraph = new Paragraph();
            paragraph.add(new Chunk("At ", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
            paragraph.add(new Chunk(location, new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE, Font.BOLD)));
            paragraph.add(new Chunk(" on ", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
            paragraph.add(new Chunk(date, new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE, Font.BOLD)));

            document.add(paragraph);

            addSpaceLine(document, 2);

            Paragraph singLabelElement = new Paragraph(new Chunk("Signatures:", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE, Font.BOLD)));
            document.add(singLabelElement);

            addSpaceLine(document,1);

            Paragraph label1 = new Paragraph(new Chunk("Bureau Veritas Marine & Offshore :", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE, Font.BOLD)));
            document.add(label1);

            Paragraph para = new Paragraph();

//            para.setTabSettings(new TabSettings(250f)); // Set tab stop at 250 units
//            addSpaceLine(document,1);
            para.add(new Chunk("Name: ", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
//            addSpaceLine(document, 1);
            para.add(new Chunk("  Jitendra Kadu", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE, Font.BOLD)));
//            para.add(new Chunk("                                "));
            para.add(addSpace(40));
            para.add(new Chunk("Signature", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
            para.add(new Chunk("  jkadu@qweqw", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
            document.add(para);

//            addSpaceLine(document, 1);
            Paragraph para2 = new Paragraph();

            para2.add(new Chunk("Function", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
//            addSpaceLine(document, 1);
            para2.add(new Chunk("  Captain", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
            document.add(para2);


//            para.add(new Chunk("Signature: ", new Font(Font.FontFamily.TIMES_ROMAN, 12)));
//            para.add(new Chunk("signature", new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD)));
            Paragraph para3 = new Paragraph();

//            para.setTabSettings(new TabSettings(250f)); // Set tab stop at 250 units
            addSpaceLine(document,1);
            Paragraph label_2 = new Paragraph(new Chunk("COSMOSHIP MANAGEMENT SA :", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE, Font.BOLD)));
            document.add(label_2);

            para3.add(new Chunk("Name: ", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
//            addSpaceLine(document, 1);
            para3.add(new Chunk("  Vikas Gupta", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE, Font.BOLD)));
//            para.add(new Chunk("                                "));
            para3.add(addSpace(40));
            para3.add(new Chunk("Signature", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
            para3.add(new Chunk("  vikas@qweqw", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
            document.add(para3);

            Paragraph para_4 = new Paragraph();

            para_4.add(new Chunk("Function", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
//            addSpaceLine(document, 1);
            para_4.add(new Chunk("  Agent", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE)));
            document.add(para_4);


            addSpaceLine(document,2);
            addFooterData(document);
            addWaterMark(document, writer);

            document.close();
            writer.close();


        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }

//        try {
//            Document document = new Document(PageSize.A4);
//            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
//            document.open();
//
//            // ✅ 1. Add Header
//            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
//            Paragraph header = new Paragraph("The Company Representative:", headerFont);
//            header.setSpacingAfter(10);
//            document.add(header);
//
//            // ✅ 2. Add Bullet Points for Declarations
//            Font bulletFont = new Font(Font.FontFamily.HELVETICA, 12);
//            List bulletList = new List(List.UNORDERED);
//            bulletList.add(new ListItem("declares to be fully authorized to sign the present request", bulletFont));
//            bulletList.add(new ListItem("declares to be well aware and accepts the Bureau Veritas Marine & Offshore General Conditions given above", bulletFont));
//            bulletList.add(new ListItem("agrees to comply with Bureau Veritas Rules", bulletFont));
//            document.add(bulletList);
//
//            // ✅ 3. Add Annexed Documents Section
//            Paragraph annexHeader = new Paragraph("The following documents are annexed to this request:", headerFont);
//            annexHeader.setSpacingBefore(10);
//            annexHeader.setSpacingAfter(5);
//            document.add(annexHeader);
//
//            List annexList = new List(List.UNORDERED);
//            annexList.add(new ListItem("Request for statutory survey and other services", bulletFont));
//            annexList.add(new ListItem("General description of the hull and machinery", bulletFont));
//            document.add(annexList);
//
//            // ✅ 4. Add Location and Date (with fillable fields for "At" and "On")
//            Font locationFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC);
//            Paragraph location = new Paragraph("At __________________________, on __________________________", locationFont);
//            location.setAlignment(Element.ALIGN_LEFT);
//            location.setSpacingBefore(20);
//            document.add(location);
//
//            document.close(); // Close the document to allow PdfStamper to edit
//
//            // ✅ 5. Create Form Fields with PdfStamper
//            PdfReader reader = new PdfReader(outputFile);
//            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("output_with_fields.pdf"));
//
//            PdfContentByte canvas = stamper.getOverContent(1);
//
//            // ✅ Create "At" field
//            TextField atField = new TextField(stamper.getWriter(), new Rectangle(70, 680, 250, 700), "atField");
//            atField.setFontSize(12);
//            atField.setBorderColor(BaseColor.BLACK);
//            atField.setBorderWidth(0.5f);
//            atField.setBackgroundColor(BaseColor.LIGHT_GRAY);
//            stamper.addAnnotation(atField.getTextField(), 1);
//
//            // ✅ Create "On" field
//            TextField onField = new TextField(stamper.getWriter(), new Rectangle(320, 680, 500, 700), "onField");
//            onField.setFontSize(12);
//            onField.setBorderColor(BaseColor.BLACK);
//            onField.setBorderWidth(0.5f);
//            onField.setBackgroundColor(BaseColor.LIGHT_GRAY);
//            stamper.addAnnotation(onField.getTextField(), 1);
//
//            // ✅ Bureau Veritas Section
//            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Bureau Veritas Marine & Offshore", bulletFont), 36, 600, 0);
//            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Name: N. MASTROGIANNIS"), 36, 580, 0);
//            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Signature: __________________________"), 36, 560, 0);
//            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Function: __________________________"), 36, 540, 0);
//
//            // ✅ COSMOSHIP Section
//            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("COSMOSHIP MANAGEMENT SA", bulletFont), 320, 600, 0);
//
//            // ✅ Create "Name" field
//            TextField nameField = new TextField(stamper.getWriter(), new Rectangle(320, 580, 500, 600), "name");
//            nameField.setFontSize(12);
//            nameField.setBorderColor(BaseColor.BLACK);
//            nameField.setBorderWidth(0.5f);
//            nameField.setBackgroundColor(BaseColor.LIGHT_GRAY);
//            stamper.addAnnotation(nameField.getTextField(), 1);
//
//            // ✅ Create "Function" field
//            TextField functionField = new TextField(stamper.getWriter(), new Rectangle(320, 560, 500, 580), "function");
//            functionField.setFontSize(12);
//            functionField.setBorderColor(BaseColor.BLACK);
//            functionField.setBorderWidth(0.5f);
//            functionField.setBackgroundColor(BaseColor.LIGHT_GRAY);
//            stamper.addAnnotation(functionField.getTextField(), 1);
//
//            // ✅ Create "Signature" field
//            TextField signatureField = new TextField(stamper.getWriter(), new Rectangle(320, 540, 500, 560), "signature");
//            signatureField.setFontSize(12);
//            signatureField.setBorderColor(BaseColor.BLACK);
//            signatureField.setBorderWidth(0.5f);
//            signatureField.setBackgroundColor(BaseColor.LIGHT_GRAY);
//            stamper.addAnnotation(signatureField.getTextField(), 1);
//
//            // ✅ Footer Section
//            Font footerFont = new Font(Font.FontFamily.HELVETICA, 10);
//            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase("REQ-000029-CAC BV Reg. N° 50070B Page 4/7", footerFont), 297.5f, 40, 0);
//
//            // ✅ Watermark
//            PdfContentByte under = stamper.getUnderContent(1);
//            Font watermarkFont = new Font(Font.FontFamily.HELVETICA, 40, Font.BOLD, new GrayColor(0.75f));
//            Phrase watermark = new Phrase("DRAFT", watermarkFont);
//            ColumnText.showTextAligned(under, Element.ALIGN_CENTER, watermark, 297.5f, 421, 45);
//
//            stamper.setFormFlattening(false); // Keep form fields editable
//            stamper.close();
//            reader.close();
//
//            System.out.println("PDF with interactive fields created: output_with_fields.pdf");

//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void addWaterMark(Document document, PdfWriter writer) throws DocumentException, IOException {
        PdfContentByte canvas = writer.getDirectContentUnder();

        // ✅ Bold + Italic font
        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLDOBLIQUE, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);

        float fontSize = 80; // Adjust based on diagonal length
        canvas.setFontAndSize(baseFont, fontSize);
        canvas.setColorFill(new GrayColor(0.85f)); // Light gray for subtle effect

        // ✅ Calculate diagonal length
        float width = document.getPageSize().getWidth();
        float height = document.getPageSize().getHeight();
        double diagonalLength = Math.sqrt(width * width + height * height);

        // ✅ Adjust font size dynamically to match diagonal length
        fontSize = (float) (diagonalLength / 8); // Fine-tune this value as needed
        canvas.setFontAndSize(baseFont, fontSize);

        canvas.saveState();
        canvas.beginText();

        // ✅ Position watermark to start at the bottom-left and end at the top-right
        float x = width / 2;
        float y = height / 2;
        float angle = (float) Math.toDegrees(Math.atan2(height, width));

        canvas.showTextAligned(Element.ALIGN_CENTER, "DRAFT", x, y, 30);

        canvas.endText();
        canvas.restoreState();
    }

    private void addFooterData(Document document) throws DocumentException {

        document.add(new Paragraph(new Chunk("This document is issued in two originals, one for each contracting party.", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE))));
        Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE);
        Paragraph header = new Paragraph("The following documents are annexed to this request", headerFont);
        header.setSpacingAfter(10);
        document.add(header);
        Font listFont = new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE);
        List bulletList = new List(List.UNORDERED);
        bulletList.setListSymbol(new Chunk(" •   ", new Font(Font.FontFamily.TIMES_ROMAN, FONT_GLOBAL_SIZE))); // Small round bullet
        bulletList.setIndentationLeft(20); // Add left space before bullet points

        bulletList.add(new ListItem("Request for statutory survey and other services",listFont));
        bulletList.add(new ListItem("General description of the hull and machinery",listFont));
//        bulletList.add(new ListItem("Agrees to comply with Bureau Veritas Rules"));

        document.add(bulletList);
    }
}
