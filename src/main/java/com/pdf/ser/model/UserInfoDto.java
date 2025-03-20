package com.pdf.ser.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoDto {
    private String name;
    private String signature;
    private String address;
    private String pin;
    private String location;
    private String date;
    private String function;
}
