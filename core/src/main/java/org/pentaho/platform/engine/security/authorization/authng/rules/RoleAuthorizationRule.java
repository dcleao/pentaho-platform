package org.pentaho.platform.engine.security.authorization.authng.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.authng.decisions.RoleAuthorizationDecision;

import java.util.Objects;
import java.util.Optional;

// Use spring configuration with AnyRoleAuthorizationRule to create a rule for multiple roles.
// Reporting mode controls whether its enough to stop...!
// TODO: docs
// TODO: toString?
public class RoleAuthorizationRule extends AbstractAuthorizationRule {
  @NonNull
  private final String role;

  public RoleAuthorizationRule( @NonNull String role ) {
    this.role = Objects.requireNonNull( role );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {
    return request.getUser().getRoles().contains( role )
      // Abstain.
      ? Optional.empty()
      : Optional.of( new RoleAuthorizationDecision( request, role ) );
  }
}
