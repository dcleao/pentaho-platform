package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractCompositeAuthorizationDecision extends AbstractAuthorizationDecision {

  private final boolean granted;

  @NonNull
  private final Set<IAuthorizationDecision> decisions;

  protected AbstractCompositeAuthorizationDecision( boolean granted, @NonNull Set<IAuthorizationDecision> decisions ) {
    this.granted = granted;
    this.decisions = Objects.requireNonNull( decisions );
  }

  @Override
  public String toString() {
    return String.format(
      "%s [granted=`%s`]",
      getClass().getSimpleName(),
      isGranted() );
  }

  @Override
  public boolean isGranted() {
    return granted;
  }

  @NonNull
  public Set<IAuthorizationDecision> getDecisions() {
    return decisions;
  }

  public abstract static class AbstractCompositeAuthorizationDecisionBuilder<
    B extends AbstractCompositeAuthorizationDecisionBuilder<B>>
    extends AbstractAuthorizationDecisionBuilder<B> {

    // Take a frugal approach to avoid unnecessary allocations.
    // Start with an immutable, singleton, empty set.
    @NonNull
    private List<IAuthorizationDecision> decisions;

    AbstractCompositeAuthorizationDecisionBuilder() {
      resetDecisions();
    }

    /**
     * Gets the set of contained decisions.
     *
     * @return A non-null, possibly empty, set of {@link IAuthorizationDecision} objects.
     */
    @NonNull
    protected List<IAuthorizationDecision> getDecisions() {
      return decisions;
    }

    /**
     * Adds a decision to the composite decision.
     *
     * @param decision The decision to add.
     * @return The builder instance, allowing for method chaining.
     */
    @SuppressWarnings( { "unchecked", "UnusedReturnValue" } )
    @NonNull
    public B withDecision( @NonNull IAuthorizationDecision decision ) {
      Objects.requireNonNull( decision );

      if ( decisions.isEmpty() ) {
        // Replace with a mutable set if it was the empty set.
        decisions = new ArrayList<>();
      }

      decisions.add( decision );

      return (B) this;
    }

    /**
     * Resets the contained decisions to an empty, default state.
     */
    protected void resetDecisions() {
      decisions = List.of();
    }

    @SuppressWarnings( { "unchecked", "UnusedReturnValue" } )
    @Override
    public B reset() {
      super.reset();
      resetDecisions();
      return (B) this;
    }

    @Override
    public boolean isEmpty() {
      // Adding decisions also makes it non-empty. Note in this case, isGranted() may be uninitialized and defaults to
      // false, when queried. Consumers should explicitly initialize the granted state to avoid any issues.
      return super.isEmpty() && decisions.isEmpty();
    }

    @NonNull
    @Override
    protected IAuthorizationDecision buildDecision() {
      // If there is a single contained decision, simplify the result and return it directly.
      return getDecisions().size() == 1
        ? getDecisions().get( 0 )
        : buildDecisionCore();
    }

    @NonNull
    protected abstract IAuthorizationDecision buildDecisionCore();
  }
}
