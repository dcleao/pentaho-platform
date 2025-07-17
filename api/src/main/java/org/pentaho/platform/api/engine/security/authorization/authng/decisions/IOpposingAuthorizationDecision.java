package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;

// TODO: Alternate names: Opposite, Opposing, Opposition, Negation, Not, Inverse

/**
 * The {@code IOpposingAuthorizationDecision} interface represents an authorization decision that opposes (is the
 * opposite of) another decision for the <i>same</i> authorization request.
 * <p>
 * The decision is granted if the {@link #getOpposedToDecision() opposed-to decision} is denied, or denied if the
 * opposing-to decision is granted. Abstentions are preserved.
 * <p>
 * The value of {@link #getRequest()} must be the same as that of the {@link #getOpposedToDecision() opposed-to
 * decision}.
 */
public interface IOpposingAuthorizationDecision extends IAuthorizationDecision {
  /**
   * Gets the decision that this one is the opposite of.
   *
   * @return The decision that this one is based on.
   */
  @NonNull
  IAuthorizationDecision getOpposedToDecision();
}
