package com.finny.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class UserIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String userId = UserContext.getCurrentUser();
        // Return a default if no user is set (e.g. for startup or admin tasks)
        // Ensure this default exists in your DB setup if required by Hibernate
        // validation
        return (userId != null) ? userId : "default_common";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
