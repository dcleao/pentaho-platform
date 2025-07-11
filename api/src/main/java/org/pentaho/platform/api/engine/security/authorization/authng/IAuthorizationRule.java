package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;

public interface IAuthorizationRule extends AuthorizationManager<IAuthorizationContext> {
  AuthorizationDecision authorize( @NonNull IAuthorizationRequest request, @NonNull AuthorizationOptions options )
}
