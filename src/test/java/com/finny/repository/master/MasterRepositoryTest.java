package com.finny.repository.master;

import com.finny.BaseMySqlTest;
import com.finny.domain.master.CurrencyCode;
import com.finny.domain.master.Tenant;
import com.finny.domain.master.User;
import com.finny.domain.master.UserSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MasterRepositoryTest extends BaseMySqlTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private CurrencyCodeRepository currencyCodeRepository;

    @Test
    void testPreExistingSeededData() {
        // Verify Tenant
        Optional<Tenant> tenant = tenantRepository.findById("family_smith_id");
        assertThat(tenant).isPresent();
        assertThat(tenant.get().getName()).isEqualTo("The Smith Family");

        // Verify User
        Optional<User> user = userRepository.findById("user_john_id");
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("john.smith@example.com");

        // Verify Currency
        Optional<CurrencyCode> inr = currencyCodeRepository.findById("INR");
        assertThat(inr).isPresent();
        assertThat(inr.get().getName()).isEqualTo("Indian Rupee");
    }

    @Test
    void testUserSessionCreation() {
        User user = userRepository.findById("user_john_id").orElseThrow();
        
        UserSession session = new UserSession();
        session.setUser(user);
        session.setToken("test-token-" + System.currentTimeMillis());
        session.setActive(true);
        
        UserSession saved = userSessionRepository.save(session);
        assertThat(saved.getId()).isNotNull();
        
        Optional<UserSession> found = userSessionRepository.findByTokenAndActiveTrue(session.getToken());
        assertThat(found).isPresent();
        
        // Cleanup
        userSessionRepository.delete(saved);
    }
}
