package com.store.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelAttribute {
    private Model model;
    private CategoryAttribute categoryAttribute;
    private String value;
}
