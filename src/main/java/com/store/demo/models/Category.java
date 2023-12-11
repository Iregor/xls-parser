package com.store.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private String name;
    private Category parentCategory;
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

/*        StringBuilder attributesDescription = new StringBuilder();
        for (CategoryAttribute categoryAttribute : categoryAttributes) {
            attributesDescription.append(String.format("%d: %s \n", categoryAttribute.getPriority(), categoryAttribute.getAttribute().getName()));
        }*/


        return fullNameDescription.toString();
    }
}
