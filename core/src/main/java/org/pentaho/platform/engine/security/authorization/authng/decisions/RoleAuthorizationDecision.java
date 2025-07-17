package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;

import java.util.Objects;

// TODO: docs
// TODO: toString
public class RoleAuthorizationDecision extends AbstractAuthorizationDecision {
  @NonNull
  private final String role;

  public RoleAuthorizationDecision( @NonNull AuthorizationRequest request, @NonNull String role ) {
    super( request, true );

    this.role = Objects.requireNonNull( role );
  }

  @NonNull
  public String getRole() {
    return role;
  }

  @Override
  public String toString() {
    // TODO: implement to string of role decision.
    return super.toString();
  }
}
