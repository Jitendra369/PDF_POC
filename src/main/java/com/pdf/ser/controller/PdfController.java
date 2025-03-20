package com.pdf.ser.controller;

import com.itextpdf.text.DocumentException;
import com.pdf.ser.model.PDFDto;
import com.pdf.ser.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @PostMapping("/create")
    public void createPdf(@RequestBody PDFDto pdfDto){
        pdfService.createPdf(pdfDto);
    }

    @PostMapping("/read")
    public String readPdfFile(@RequestBody PDFDto pdfDto) throws DocumentException, IOException {
        return pdfService.readPdfFile(pdfDto);
    }

    @PostMapping("/edit")
    public String readAndAddText(@RequestBody PDFDto pdfDto) throws DocumentException, IOException {
        return pdfService.readAndAddTextToPDF(pdfDto.getFilePath(),pdfDto.getNewFilePathToStore(),pdfDto.getTextToAddInFile());
    }

    @PostMapping("/modifyPdf")
    public String modifyPdf(@RequestBody PDFDto pdfDto) throws DocumentException, IOException {
         pdfService.createPDF();
         return "";
    }


}
