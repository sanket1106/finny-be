package com.finny.service;

import com.finny.config.UserContext;
import com.finny.domain.Category;
import com.finny.dto.CategoryResponseDto;
import com.finny.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(value = "tenantTransactionManager", readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Tenant ID is missing from User Context.");
        }

        // Fetch all categories for the tenant
        List<Category> tenantCategories = categoryRepository.findAll();

        // Group categories by parent ID
        Map<String, List<Category>> childrenMap = tenantCategories.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        // Start with root categories (parent == null)
        return tenantCategories.stream()
                .filter(c -> c.getParent() == null)
                .map(c -> mapToDto(c, childrenMap))
                .collect(Collectors.toList());
    }

    private CategoryResponseDto mapToDto(Category category, Map<String, List<Category>> childrenMap) {
        CategoryResponseDto dto = new CategoryResponseDto(category.getId(), category.getName());
        
        List<Category> children = childrenMap.getOrDefault(category.getId(), new ArrayList<>());
        dto.setSubcategories(children.stream()
                .map(child -> mapToDto(child, childrenMap))
                .collect(Collectors.toList()));
        
        return dto;
    }
}
