package org.pentaho.platform.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecisionFactory;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.ICompositeAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IImpliedAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IOpposedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.authng.decisions.AbstractAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizationDecisionFactory implements IAuthorizationDecisionFactory {
  @NonNull
  @Override
  public IAuthorizationDecision grant( @NonNull AuthorizationRequest request ) {
    return new AbstractAuthorizationDecision( request, true );
  }

  @NonNull
  @Override
  public IAuthorizationDecision deny( @NonNull AuthorizationRequest request ) {
    return new AbstractAuthorizationDecision( request, false );
  }

  @NonNull
  @Override
  public IAnyAuthorizationDecision anyOf( @NonNull AuthorizationRequest request,
                                          boolean granted,
                                          @NonNull Set<IAuthorizationDecision> decisions ) {
    return new AnyAuthorizationDecision( request, granted, decisions );
  }

  @NonNull
  @Override
  public IAllAuthorizationDecision allOf( @NonNull AuthorizationRequest request,
                                          boolean granted,
                                          @NonNull Set<IAuthorizationDecision> decisions ) {
    return new AllAuthorizationDecision( request, granted, decisions );
  }

  @NonNull
  @Override
  public IOpposedAuthorizationDecision opposedTo( @NonNull IAuthorizationDecision opposedToDecision ) {
    return new OpposedAuthorizationDecision( opposedToDecision );
  }

  @NonNull
  @Override
  public IImpliedAuthorizationDecision impliedFrom( @NonNull AuthorizationRequest request,
                                                    @NonNull IAuthorizationDecision impliedByDecision ) {
    return new ImpliedAuthorizationDecision( request, impliedByDecision );
  }

  private static class OpposedAuthorizationDecision extends AbstractAuthorizationDecision
    implements IOpposedAuthorizationDecision {

    private static final String OPPOSED_TO_JUSTIFICATION =
      Messages.getInstance().getString( "AuthorizationDecisionFactory.OPPOSED_TO_JUSTIFICATION" );

    @NonNull
    private final IAuthorizationDecision opposedToDecision;

    public OpposedAuthorizationDecision( @NonNull IAuthorizationDecision opposedToDecision ) {
      // Negate the granted state of the opposed decision.
      super( opposedToDecision.getRequest(), !opposedToDecision.isGranted() );

      this.opposedToDecision = opposedToDecision;
    }

    @NonNull
    @Override
    public IAuthorizationDecision getOpposedToDecision() {
      return opposedToDecision;
    }

    @Override
    public String getShortJustification() {
      // Example: "Opposing: <opposed decision justification>"
      return String.format( OPPOSED_TO_JUSTIFICATION, opposedToDecision );
    }

    @Override
    public String toString() {
      // Example: "Opposed(Granted, to: DerivedFromAction[Denied, ...])"
      return String.format( "Opposed[%s, to: %s]", getGrantedLogText(), opposedToDecision );
    }
  }

  private static class ImpliedAuthorizationDecision extends AbstractAuthorizationDecision
    implements IImpliedAuthorizationDecision {

    private static final String IMPLIED_FROM_JUSTIFICATION =
      Messages.getInstance().getString( "AuthorizationDecisionFactory.IMPLIED_FROM_JUSTIFICATION" );

    @NonNull
    private final IAuthorizationDecision impliedFromDecision;

    public ImpliedAuthorizationDecision( @NonNull AuthorizationRequest request,
                                         @NonNull IAuthorizationDecision impliedFromDecision ) {
      // Same granted state of the implied from decision.
      super( request, impliedFromDecision.isGranted() );

      this.impliedFromDecision = impliedFromDecision;

      if ( request.equals( impliedFromDecision.getRequest() ) ) {
        throw new IllegalArgumentException(
          "Argument 'request' cannot be equal to the request of argument 'impliedFromDecision'." );
      }
    }

    @NonNull
    @Override
    public IAuthorizationDecision getImpliedFromDecision() {
      return impliedFromDecision;
    }

    @Override
    public String getShortJustification() {
      // Example: "From <implied-from decision justification>"
      return String.format( IMPLIED_FROM_JUSTIFICATION, impliedFromDecision );
    }

    @Override
    public String toString() {
      // Example: "Implied[Granted, from: GeneralRoleBased[Granted, role=Administrator]]"
      return String.format( "Implied[%s, from: %s]", getGrantedLogText(), impliedFromDecision );
    }
  }

  private abstract static class AbstractCompositeAuthorizationDecision extends AbstractAuthorizationDecision
    implements ICompositeAuthorizationDecision {

    @NonNull
    private final Set<IAuthorizationDecision> decisions;

    protected AbstractCompositeAuthorizationDecision( @NonNull AuthorizationRequest request,
                                                      boolean granted,
                                                      @NonNull Set<IAuthorizationDecision> decisions ) {
      super( request, granted );
      this.decisions = Collections.unmodifiableSet( decisions );
    }

    @NonNull
    public Set<IAuthorizationDecision> getDecisions() {
      return decisions;
    }

    @NonNull
    protected String getDecisionsLogText() {
      return getDecisions()
        .stream()
        .map( Object::toString )
        .collect( Collectors.joining( ", " ) );
    }
  }

  private static class AllAuthorizationDecision extends AbstractCompositeAuthorizationDecision
    implements IAllAuthorizationDecision {

    public AllAuthorizationDecision( @NonNull AuthorizationRequest request,
                                     boolean granted,
                                     @NonNull Set<IAuthorizationDecision> decisions ) {
      super( request, granted, decisions );
    }

    @Override
    public String toString() {
      // Example: "All[Denied, of: <contained decision 1 text>, <contained decision 2 text>]"
      return String.format( "All[%s, of: %s]", getGrantedLogText(), getDecisionsLogText() );
    }
  }

  private static class AnyAuthorizationDecision extends AbstractCompositeAuthorizationDecision
    implements IAnyAuthorizationDecision {

    public AnyAuthorizationDecision( @NonNull AuthorizationRequest request,
                                     boolean granted,
                                     @NonNull Set<IAuthorizationDecision> decisions ) {
      super( request, granted, decisions );
    }

    @Override
    public String toString() {
      // Example: "Any[Granted, of: <contained decision 1 text>, <contained decision 2 text>]"
      return String.format( "Any[%s, of: %s]", getGrantedLogText(), getDecisionsLogText() );
    }
  }
  // endregion
}
