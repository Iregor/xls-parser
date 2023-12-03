package com.store.demo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Data
@AllArgsConstructor
public class Category {
    private String name;
    private Category parentCategory;
    private List<Attribute> attributes;

    public String getDescription() {
        StringBuilder fullNameDescription = new StringBuilder();
        Deque<String> deq = new ArrayDeque<>();
        Category category = this;
        while (category != null) {
            deq.addLast(category.name);
            category = category.parentCategory;
        }
        while(!deq.isEmpty()) {
            fullNameDescription.append("/");
            fullNameDescription.append(deq.pollLast());
        }

        StringBuilder attributesDescription = new StringBuilder();
        for (Attribute attribute : attributes) {
            attributesDescription.append(String.format("%d: %s \n", attribute.getPriority(), attribute.getName()));
        }


        return String.format("%s. Attribute list: \n%s",
                fullNameDescription, attributesDescription);
    }
}
