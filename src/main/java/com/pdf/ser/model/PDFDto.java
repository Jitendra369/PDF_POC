package com.pdf.ser.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PDFDto {

    private String fileName;
    private String filePath;
    private String newFilePathToStore;
    private String textToAddInFile;
    private UserInfoDto userInfoDto;
}
