package com.finny.service;

import com.finny.domain.master.Tenant;
import com.finny.domain.master.User;
import com.finny.dto.RegisterRequest;
import com.finny.repository.master.TenantRepository;
import com.finny.repository.master.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.finny.constant.AuthConstants;
import com.finny.domain.master.UserSession;
import com.finny.dto.LoginRequest;
import com.finny.dto.LoginResponse;
import com.finny.repository.master.UserSessionRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantProvisioningService tenantProvisioningService;

    private final UserSessionRepository userSessionRepository;

    public AuthService(UserRepository userRepository, 
                       TenantRepository tenantRepository, 
                       PasswordEncoder passwordEncoder,
                       TenantProvisioningService tenantProvisioningService,
                       UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantProvisioningService = tenantProvisioningService;
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional("masterTransactionManager")
    public void registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email already exists");
        }

        Tenant tenant;
        Optional<Tenant> existingTenant = tenantRepository.findById(request.getTenantId());
        
        if (existingTenant.isPresent()) {
            tenant = existingTenant.get();
        } else {
            // Create new tenant
            tenant = new Tenant();
            tenant.setId(request.getTenantId());
            
            String tenantName = request.getTenantName();
            if (tenantName == null || tenantName.isBlank()) {
                tenantName = "Tenant-" + UUID.randomUUID().toString().substring(0, 8);
            }
            tenant.setName(tenantName);
            
            String dbName = "finny_tenant_" + request.getTenantId().replace("-", "_");
            tenant.setDbName(dbName);
            
            tenant = tenantRepository.save(tenant);
            
            // Provision database
            tenantProvisioningService.provisionTenantDatabase(tenant.getDbName(), tenant.getId());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setTenant(tenant);

        userRepository.save(user);
    }

    @Transactional("masterTransactionManager")
    public LoginResponse loginUser(LoginRequest request, String userAgent) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(AuthConstants.ERROR_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException(AuthConstants.ERROR_INVALID_CREDENTIALS);
        }

        // Invalidate old active sessions
        List<UserSession> activeSessions = userSessionRepository.findByUserAndActiveTrue(user);
        for (UserSession session : activeSessions) {
            session.setActive(false);
            session.setInvalidatedBy(AuthConstants.INVALIDATED_BY_SYSTEM);
        }
        userSessionRepository.saveAll(activeSessions);

        // Truncate client info
        String clientInfo = userAgent;
        if (clientInfo != null && clientInfo.length() > AuthConstants.MAX_CLIENT_INFO_LENGTH) {
            clientInfo = clientInfo.substring(0, AuthConstants.MAX_CLIENT_INFO_LENGTH);
        }

        // Create new session
        UserSession newSession = new UserSession();
        newSession.setUser(user);
        String token = UUID.randomUUID().toString();
        newSession.setToken(token);
        newSession.setClientInfo(clientInfo);
        newSession.setActive(true);

        userSessionRepository.save(newSession);

        return new LoginResponse(token);
    }
}
