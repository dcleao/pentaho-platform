package org.pentaho.platform.engine.security.authorization.authng.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.authng.decisions.RoleAuthorizationDecision;

import java.util.Objects;
import java.util.Optional;

/**
 * The {@code RoleAuthorizationRule} class represents an authorization rule that checks if a user has a specific role.
 * <p>
 * The rule grants if the user has the role, with an instance of {@link RoleAuthorizationDecision}, and abstains
 * otherwise.
 * <p>
 * If multiple roles are needed, an "Any" composite rule can be created using several instances of this rule, one per
 * role. This approach has the advantage of automatically taking the authorization's
 * {@link AuthorizationOptions#getDecisionReportingMode() decision reporting mode} option into account for controlling
 * whether to test for the various roles, or if it is enough to test for and report the first one that matches.
 */
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
