package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

// TODO: Alternative names: Or, Affirmative, Positive, Disjunction

/**
 * The {@code IAnyAuthorizationDecision} interface represents a composite authorization decision resulting from the
 * <i>disjunction</i> of the contained decisions.
 * <p>
 * The composite decision is either granted, if at least one of the contained decisions is granted, or, denied, if all
 * contained decisions are denied.
 */
public interface IAnyAuthorizationDecision extends ICompositeAuthorizationDecision {
}
