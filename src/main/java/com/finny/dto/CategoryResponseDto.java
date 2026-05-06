package com.finny.dto;

import java.util.ArrayList;
import java.util.List;

public class CategoryResponseDto {
    private String id;
    private String name;
    private List<CategoryResponseDto> subcategories = new ArrayList<>();

    public CategoryResponseDto() {
    }

    public CategoryResponseDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CategoryResponseDto> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<CategoryResponseDto> subcategories) {
        this.subcategories = subcategories;
    }
}
