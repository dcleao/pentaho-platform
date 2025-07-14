package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;

public interface IAuthorizationContext {
  @NonNull
  AuthorizationRequest getRequest();

  @NonNull
  IAuthorizationEngine getEngine();

  // Sub-authorization, within current authorization evaluation context.
  // Same options.
  // With cycle detection.
  @Nullable
  AuthorizationDecision authorize( @NonNull AuthorizationRequest request );
}
