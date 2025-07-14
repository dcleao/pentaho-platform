package org.pentaho.platform.engine.security.authorization.spring;

import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

public class PentahoEngineAuthorizationManager implements AuthorizationManager<IAuthorizationContext> {
  @Override
  public AuthorizationDecision check( Supplier<Authentication> authentication, IAuthorizationContext object ) {
    return null;
  }
}
