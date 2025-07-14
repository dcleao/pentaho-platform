package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.security.authorization.AuthorizationManager;

public interface IAuthorizationRule extends AuthorizationManager<IAuthorizationContext> {
  IAuthorizationDecision authorize( @NonNull AuthorizationRequest request, @NonNull AuthorizationOptions options );
}
