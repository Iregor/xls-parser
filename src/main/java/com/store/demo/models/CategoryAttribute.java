package com.store.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAttribute {
    private Long id;
    private Category category;
    private Attribute attribute;
    private long priority;
    private String attributeType;
}
