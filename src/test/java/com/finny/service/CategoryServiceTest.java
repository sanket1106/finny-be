package com.finny.service;

import com.finny.config.UserContext;
import com.finny.domain.Category;
import com.finny.dto.CategoryResponseDto;
import com.finny.repository.CategoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        UserContext.setTenantId("test-tenant");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getAllCategories_ShouldReturnHierarchicalStructure() {
        // Arrange
        Category root1 = new Category();
        root1.setId("1");
        root1.setName("Food");

        Category sub1 = new Category();
        sub1.setId("2");
        sub1.setName("Groceries");
        sub1.setParent(root1);

        Category root2 = new Category();
        root2.setId("3");
        root2.setName("Transport");

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(root1, sub1, root2));

        // Act
        List<CategoryResponseDto> result = categoryService.getAllCategories();

        // Assert
        assertThat(result).hasSize(2);
        
        CategoryResponseDto foodDto = result.stream().filter(c -> c.getName().equals("Food")).findFirst().get();
        assertThat(foodDto.getSubcategories()).hasSize(1);
        assertThat(foodDto.getSubcategories().get(0).getName()).isEqualTo("Groceries");

        CategoryResponseDto transportDto = result.stream().filter(c -> c.getName().equals("Transport")).findFirst().get();
        assertThat(transportDto.getSubcategories()).isEmpty();
    }

    @Test
    void getAllCategories_WhenNoTenantInContext_ShouldThrowException() {
        // Arrange
        UserContext.clear();

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getAllCategories())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant ID is missing");
    }
}
