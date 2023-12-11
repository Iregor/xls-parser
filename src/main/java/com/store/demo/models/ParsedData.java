package com.store.demo.models;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class ParsedData {
    private final Set<Attribute> attributeSet;
    private final Set<ModelImage> modelImageSet;
    private final Set<Category> categorySet;
    private final Set<Model> modelSet;
    private final Set<ModelAttribute> modelAttributeSet;
    private final Set<CategoryAttribute> categoryAttributeSet;

    public ParsedData() {
        attributeSet = new HashSet<>();
        modelImageSet = new HashSet<>();
        categorySet = new HashSet<>();
        modelSet = new HashSet<>();
        modelAttributeSet = new HashSet<>();
        categoryAttributeSet = new HashSet<>();
    }
}
