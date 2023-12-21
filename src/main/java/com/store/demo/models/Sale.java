package com.store.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Sale {
    private Long id;
    private Model model;
    private int percent;
}
