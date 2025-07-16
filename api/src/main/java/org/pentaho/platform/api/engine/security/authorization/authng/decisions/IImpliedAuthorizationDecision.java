package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

// TODO: Alternate names: Or, Affirmative, Positive

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationOptions;

import java.util.Set;

/**
 * The {@code IImpliedAuthorizationDecision} interface represents an authorization decision which was implied by
 * another decision - the <i>antecedent</i> decision, having the same granted status (whether granted or denied).
 * <p>
 * To be clear, it must be the case that the implied decision's {@link #isGranted()} status is the same as the
 * granted status of the decision returned by {@link #getImpliedByDecision()}.
 * <p>
 * Typically, decisions of this type are the result of "implication rules", having the form:
 * "if A is granted, then B is granted, else abstain".
 * It's important to note that when the antecedent is not granted (or is an abstention), the implication cannot conclude
 * anything about the consequent: whether B should be granted or denied; the result should be an abstention.
 * <p>
 * Because of this property, when the implied decision is built with a denied status, it must have been because the
 * decision was the result of an implication rule having the (less typical) form:
 * "if A is denied, then B is denied, else abstain".
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
