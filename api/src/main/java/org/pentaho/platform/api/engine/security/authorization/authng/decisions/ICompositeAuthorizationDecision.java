package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Set;

/**
 * The {@code ICompositeAuthorizationDecision} interface represents an authorization decision which is justified in
 * part or in full by a set of other decisions.
 * <p>
 * The exact semantics and nature of the composition is defined by extension interfaces.
 */
public interface ICompositeAuthorizationDecision extends IAuthorizationDecision {
  /**
   * Gets the set of decisions that constitute this composite decision.
   *
   * @return A non-null, possibly empty, list of {@link IAuthorizationDecision} objects.
   */
  @NonNull
  Set<IAuthorizationDecision> getDecisions();
}
