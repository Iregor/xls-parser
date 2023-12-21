package com.store.demo.dto;

import com.store.demo.models.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CollectionDto {
    private Long id;
    private final String name;
    private String imageLink;
}
