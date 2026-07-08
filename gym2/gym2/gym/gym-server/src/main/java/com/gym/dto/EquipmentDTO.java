package com.gym.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentDTO {

    private String name;
    private String equipmentType;
    private String targetMuscle;
    private String warnings;
    private LocalDate purchaseDate;
    private String location;
    private String description;
    private String imageUrl;
}
