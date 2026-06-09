package io.army.example.type.resolver;

import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class SyncSessionParameterResolver implements ParameterResolver {


    private final SyncSessionContext sessionContext;

    public SyncSessionParameterResolver(SyncSessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }


    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {

        final Class<?> typeClass;
        typeClass = parameterContext.getParameter().getType();
        return SyncSession.class.isAssignableFrom(typeClass);
    }

    @Nullable
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return sessionContext.currentSession();
    }


}
