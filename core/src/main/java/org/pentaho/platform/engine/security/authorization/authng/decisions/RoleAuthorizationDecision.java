package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.engine.security.messages.Messages;

import java.util.Objects;

/**
 * The {@code RoleAuthorizationDecision} class represents an authorization decision that is granted based on a
 * user having a specific role.
 */
public class RoleAuthorizationDecision extends AbstractAuthorizationDecision {
  private static final String JUSTIFICATION =
    Messages.getInstance().getString( "RoleAuthorizationDecision.JUSTIFICATION" );

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
  public String getShortJustification() {
    // Example: "From role 'Administrator'".
    return String.format( JUSTIFICATION, role );
  }

  @Override
  public String toString() {
    // Example: "Role[Granted, name: 'Administrator']"
    return String.format( "Role[%s, name: '%s']", getGrantedLogText(), role );
  }
}
