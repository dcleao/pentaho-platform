package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.security.authorization.AuthorizationDecision;

public interface IAuthorizationEngine {
  @NonNull
  AuthorizationDecision authorize( @NonNull IAuthorizationRequest request, @NonNull AuthorizationOptions options );
}
