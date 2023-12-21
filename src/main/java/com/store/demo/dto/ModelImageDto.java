package com.store.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelImageDto {
    private long modelId;
    private String imageLink;
}
