package com.Akshay.Excelrproject.DTO;


import lombok.Data;

import java.time.LocalDate;

@Data
public class CertificationDTO {
    private String name;
    private String institution;
    private String providedBy;
    private LocalDate date;

    // Getters and setters
}