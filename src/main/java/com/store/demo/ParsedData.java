package com.store.demo;

import com.store.demo.models.*;
import com.store.demo.models.Collection;
import lombok.Data;

import java.util.*;

@Data
public class ParsedData {
    private final Map<String, Attribute> attributeMap;
    private final Set<ModelImage> modelImageSet;
    private final Map<String, Category> categoryMap;
    private final Map<String, Model> modelMap;
    private final Set<ModelAttribute> modelAttributeSet;
    private final Set<CategoryAttribute> categoryAttributeSet;
    private final Set<Collection> collectionSet;
    private final Map<String, Sale> saleMap;

    public ParsedData() {
        attributeMap = new HashMap<>();
        modelImageSet = new HashSet<>();
        categoryMap = new LinkedHashMap<>();
        modelMap = new HashMap<>();
        modelAttributeSet = new HashSet<>();
        categoryAttributeSet = new HashSet<>();
        collectionSet = new LinkedHashSet<>();
        saleMap = new HashMap<>();
    }
}
