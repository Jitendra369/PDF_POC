package com.pdf.ser.controller;

import com.itextpdf.text.DocumentException;
import com.pdf.ser.model.PDFDto;
import com.pdf.ser.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @PostMapping("/create")
    public ResponseEntity<?> createPdf(@RequestBody PDFDto pdfDto) throws IOException {
        return getPdfFile(pdfService.createPdf(pdfDto));
    }

    private ResponseEntity<?> getPdfFile( ByteArrayInputStream byteArrayInputStream ) throws IOException {
        byte[] byteArray = IOUtils.toByteArray(byteArrayInputStream);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_PDF);
        httpHeaders.setContentDisposition(ContentDisposition.attachment().filename("RFC_PDF_PREVIEW.pdf").build());
        return new ResponseEntity<>(byteArray, httpHeaders, HttpStatus.OK);

    }

    @PostMapping("/read")
    public String readPdfFile(@RequestBody PDFDto pdfDto) throws DocumentException, IOException {
        System.out.println("sdasd");
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
