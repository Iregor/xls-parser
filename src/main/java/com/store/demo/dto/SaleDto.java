package com.store.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaleDto {
    private long modelId;
    private int percent;
}
