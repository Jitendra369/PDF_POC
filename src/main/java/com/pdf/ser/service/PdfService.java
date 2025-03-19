package com.pdf.ser.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.pdf.ser.model.PDFDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class PdfService {

    public void createPdf(PDFDto pdfDto){
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfDto.getFilePath()));
            document.open();
            document.add(new Paragraph(" hell0, this is sample pdf data"));
            document.close();
            System.out.println("PDF file created successfully");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String readPdfFile(PDFDto pdfDto) throws IOException, DocumentException {
        File file = new File(pdfDto.getFilePath());
        if (!file.exists()){
            return "PDF file does not exist in the path "+ pdfDto.getFilePath();
        }
        PdfReader pdfReader = new PdfReader(pdfDto.getFilePath());
        if (pdfReader.isEncrypted()){
            return "PDf is encrypted , and cannot read the file";
        }
        int numberOfPages = pdfReader.getNumberOfPages();

        StringBuilder sb = new StringBuilder();
        log.info("total number of pages in pdf is "+ numberOfPages);

        for (int i = 1; i <= numberOfPages;i++){
            sb.append(PdfTextExtractor.getTextFromPage(pdfReader, i));
            sb.append("\n");
        }
        pdfReader.close();
        return sb.toString();
    }

    public String readAndAddTextToPDF(String inputFilePath, String outputFilePath , String textToAdd){
        PdfReader reader = null;
        PdfStamper pdfStamper = null;
        try{

            File file = new File(inputFilePath);
            if (!file.exists()){
                return "PDF file does not exist in the path "+ inputFilePath;
            }
            PdfReader pdfReader = new PdfReader(inputFilePath);
            if (pdfReader.isEncrypted()){
                return "PDf is encrypted , and cannot read the file";
            }
            reader = new PdfReader(inputFilePath);
            pdfStamper = new PdfStamper(reader, new FileOutputStream(outputFilePath));

            int numberOfPages = reader.getNumberOfPages();
            log.info("total number of pages in pdf is "+ numberOfPages);

            StringBuilder content = new StringBuilder();
            for (int i = 1; i <=  reader.getNumberOfPages() ; i++){
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
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (pdfStamper != null){
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
            File file = new File(pdfDto.getFilePath());
            if (!file.exists()) {
                return "PDF file does not exist at path: " + pdfDto.getFilePath();
            }

            reader = new PdfReader(pdfDto.getFilePath());
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
                    canvas.setTextMatrix(250, 750); // Position near "Name:"
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
}
