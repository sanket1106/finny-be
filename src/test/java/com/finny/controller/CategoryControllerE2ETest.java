package com.finny.controller;

import com.finny.BaseMySqlTest;
import com.finny.domain.Category;
import com.finny.dto.CategoryResponseDto;
import com.finny.dto.LoginRequest;
import com.finny.dto.LoginResponse;
import com.finny.dto.RegisterRequest;
import com.finny.repository.CategoryRepository;
import com.finny.repository.master.UserRepository;
import com.finny.domain.master.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerE2ETest extends BaseMySqlTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private String token;
    private final String tenantId = "family_smith_id";

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUpData() {
        String suffix = String.valueOf(System.currentTimeMillis()).substring(8);
        String email = "cat-e2e-" + suffix + "@example.com";
        String password = "password123";

        // Register new user for existing tenant
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setTenantId(tenantId);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFirstName("Cat");
        registerRequest.setLastName("User");

        restTemplate.postForEntity(getBaseUrl() + "/api/v1/auth/register", registerRequest, String.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(getBaseUrl() + "/api/v1/auth/login", loginRequest, LoginResponse.class);
        token = loginResponse.getBody().getToken();

        User user = userRepository.findByEmail(email).orElseThrow();
        setupTenantContext(user.getId(), tenantId);

        // Seed some categories
        Category food = new Category();
        food.setName("Food");
        food = categoryRepository.save(food);

        Category groceries = new Category();
        groceries.setName("Groceries");
        groceries.setParent(food);
        categoryRepository.save(groceries);

        Category dining = new Category();
        dining.setName("Dining Out");
        dining.setParent(food);
        categoryRepository.save(dining);

        Category transport = new Category();
        transport.setName("Transport");
        categoryRepository.save(transport);
    }

    @AfterEach
    void cleanUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void testGetAllCategories_ReturnsHierarchicalList() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<List<CategoryResponseDto>> response = restTemplate.exchange(
                getBaseUrl() + "/api/v1/categories",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<CategoryResponseDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CategoryResponseDto> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(2); // Food and Transport

        CategoryResponseDto foodDto = body.stream().filter(c -> c.getName().equals("Food")).findFirst().get();
        assertThat(foodDto.getSubcategories()).hasSize(2);
        assertThat(foodDto.getSubcategories()).extracting(CategoryResponseDto::getName)
                .containsExactlyInAnyOrder("Groceries", "Dining Out");

        CategoryResponseDto transportDto = body.stream().filter(c -> c.getName().equals("Transport")).findFirst().get();
        assertThat(transportDto.getSubcategories()).isEmpty();
    }
}
