package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

// TODO: Alternative names: And, Unanimous, Consensus, Conjunction
/**
 * The {@code IAllAuthorizationDecision} interface represents a composite authorization decision formed by the
 * <i>conjunction</i> of the contained decisions.
 * <p>
 * The composite decision is either granted, if all contained decisions are granted, or, denied, if at least one of the
 * contained decisions is denied.
 */
public interface IAllAuthorizationDecision extends ICompositeAuthorizationDecision {
}
