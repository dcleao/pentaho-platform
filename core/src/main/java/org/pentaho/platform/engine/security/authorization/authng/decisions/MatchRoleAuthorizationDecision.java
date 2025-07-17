package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.engine.security.messages.Messages;

import java.util.Objects;

/**
 * The {@code MatchRoleAuthorizationDecision} class represents an authorization decision that is granted when the user
 * of an authorization request having a specific role.
 */
public class MatchRoleAuthorizationDecision extends AbstractAuthorizationDecision {
  private static final String JUSTIFICATION =
    Messages.getInstance().getString( "RoleAuthorizationDecision.JUSTIFICATION" );

  @NonNull
  private final String role;

  public MatchRoleAuthorizationDecision( @NonNull AuthorizationRequest request, @NonNull String role ) {
    super( request, true );

    this.role = Objects.requireNonNull( role );
  }

  @NonNull
  public String getRole() {
    return role;
  }

  @Override
  public String getShortJustification() {
    // Example: "Has role 'Administrator'".
    return String.format( JUSTIFICATION, role );
  }

  @Override
  public String toString() {
    // Example: "MatchRole[Granted, name: 'Administrator']"
    return String.format( "MatchRole[%s, name: '%s']", getGrantedLogText(), role );
  }
}
