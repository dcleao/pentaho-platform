package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;

import java.util.Optional;

public abstract class AbstractAuthorizationDecision implements IAuthorizationDecision {
  @Override
  public String toString() {
    return String.format(
      "%s [granted=`%s`]",
      getClass().getSimpleName(),
      isGranted() );
  }

  public abstract static class AbstractAuthorizationDecisionBuilder<B extends AbstractAuthorizationDecisionBuilder<B>> {

    private Boolean granted = null;

    public boolean isGranted() {
      return granted != null && granted;
    }

    public boolean isDenied() {
      return granted != null && !granted;
    }

    /**
     * Sets the granted status of the decision, to granted or denied.
     *
     * @param granted The granted status to set.
     * @return The builder instance, allowing for method chaining.
     */
    @SuppressWarnings( { "unchecked", "UnusedReturnValue" } )
    @NonNull
    public B granted( boolean granted ) {
      this.granted = granted;
      return (B) this;
    }

    /**
     * Turns the combined decision into a grant.
     *
     * @return The builder instance, allowing for method chaining.
     */
    @SuppressWarnings( { "unchecked", "UnusedReturnValue" } )
    public B grant() {
      this.granted = true;
      return (B) this;
    }

    /**
     * Turns the combined decision into a denial.
     *
     * @return The builder instance, allowing for method chaining.
     */
    @SuppressWarnings( { "unchecked", "UnusedReturnValue" } )
    public B deny() {
      this.granted = false;
      return (B) this;
    }

    /**
     * Changes the combined decision to be in a default state.
     * <p>
     * This method should revert the decision to a default state, after which {@link #isEmpty()} must return
     * {@code true}.
     *
     * @return The builder instance, allowing for method chaining.
     */
    @SuppressWarnings( { "unchecked", "UnusedReturnValue" } )
    public B reset() {
      this.granted = null;
      return (B) this;
    }

    /**
     * Indicates whether the combined decision is in an emtpy, default state.
     * <p>
     * When in an empty state, the result of {@link #build()} is an empty {@link Optional}.
     * Otherwise, the result of {@link #build()} is a present/non-empty {@link Optional}, containing the decision
     * instance built by {@link #buildDecision()}.
     *
     * @return {@code true} if the decision is in an empty, default state, {@code false} otherwise.
     */
    public boolean isEmpty() {
      return granted == null;
    }

    @NonNull
    public final Optional<IAuthorizationDecision> build() {
      return isEmpty() ? Optional.empty() : Optional.of( buildDecision() );
    }

    @NonNull
    protected abstract IAuthorizationDecision buildDecision();
  }
}
