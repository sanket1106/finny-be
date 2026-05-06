package com.finny.repository;

import com.finny.BaseMySqlTest;
import com.finny.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryRepositoryTest extends BaseMySqlTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        setupTenantContext("user_john_id", "family_smith_id");
    }

    @Test
    void testFindSeededCategory() {
        // Assume 'Food' category exists (seeded via finny.sh or registration)
        // Let's create one just to be sure we have programmatic data as requested
        Category category = new Category();
        category.setName("Programmatic Category");

        Category saved = categoryRepository.save(category);
        assertThat(saved.getId()).isNotNull();

        Optional<Category> found = categoryRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Programmatic Category");
        
        // Cleanup
        categoryRepository.delete(saved);
    }
}
