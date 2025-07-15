package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationOptions;

import java.util.Set;

// TODO: Alternate names: Opposite, Opposing, Opposition, Negation, Not, Inverse

/**
 * The {@code IOpposingAuthorizationDecision} interface represents an authorization decision that opposes (is the
 * opposite of) another decision.
 * <p>
 * The decision is granted if the contained decision, {@link #getOpposingToDecision()} is denied, or denied if the
 * contained decision is granted.
 * <p>
 * The implementation of {@link #getDecisions()} must return a set having {@link #getOpposingToDecision()} as its single
 * element. This must be the case regardless of the {@link AuthorizationOptions#getDecisionReportingMode()} used for the
 * authorization process.
 */
public interface IOpposingAuthorizationDecision extends ICompositeAuthorizationDecision {
  /**
   * Gets the decision that this one is the opposite of.
   *
   * @return The decision that this one is based on.
   */
  @NonNull
  IAuthorizationDecision getOpposingToDecision();

  @NonNull
  @Override
  default Set<IAuthorizationDecision> getDecisions() {
    return Set.of( getOpposingToDecision() );
  }
}
