package com.Akshay.Excelrproject.DTO;


import lombok.Data;

@Data
public class AddressDTO {
    private String street;
    private String city;
    private String state;
    private String country;
    private String pincode;

    // Getters and setters
}