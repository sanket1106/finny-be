package com.finny;

import com.finny.config.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseMySqlTest {

    @AfterEach
    void tearDown() {
        // Clear UserContext after each test
        UserContext.clear();
    }

    protected void setupTenantContext(String userId) {
        UserContext.setCurrentUser(userId);
    }

    protected void setupTenantContext(String userId, String tenantId) {
        UserContext.setCurrentUser(userId);
        UserContext.setTenantId(tenantId);
    }
}
