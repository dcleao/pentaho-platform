package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

// TODO: Alternate names: Or, Affirmative, Positive

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationOptions;

import java.util.Set;

/**
 * The {@code IImpliedAuthorizationDecision} interface represents an authorization decision which was implied by
 * another decision - the <i>antecedent</i> decision.
 * <p>
 * The implementation of {@link #getDecisions()} must return a set having {@link #getImpliedByDecision()} as its single
 * element. This must be the case regardless of the {@link AuthorizationOptions#getDecisionReportingMode()} used for the
 * authorization process.
 */
public interface IImpliedAuthorizationDecision extends ICompositeAuthorizationDecision {
  /**
   * Gets the decision that implies this one.
   *
   * @return The decision that implies this one.
   */
  @NonNull
  IAuthorizationDecision getImpliedByDecision();

  @NonNull
  @Override
  default Set<IAuthorizationDecision> getDecisions() {
    return Set.of( getImpliedByDecision() );
  }
}
