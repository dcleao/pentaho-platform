package org.pentaho.platform.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecisionFactory;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.ICompositeAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IImpliedAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IOpposingAuthorizationDecision;
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
  public IOpposingAuthorizationDecision opposingTo( @NonNull IAuthorizationDecision opposingToDecision ) {
    return new OpposingAuthorizationDecision( opposingToDecision );
  }

  @NonNull
  @Override
  public IImpliedAuthorizationDecision impliedFrom( @NonNull AuthorizationRequest request,
                                                    @NonNull IAuthorizationDecision impliedByDecision ) {
    return new ImpliedAuthorizationDecision( request, impliedByDecision );
  }

  private static class OpposingAuthorizationDecision extends AbstractAuthorizationDecision
    implements IOpposingAuthorizationDecision {

    private static final String OPPOSING_TO_TEXT =
      Messages.getInstance().getString( "AuthorizationDecisionFactory.OPPOSING_TO" );

    @NonNull
    private final IAuthorizationDecision opposedToDecision;

    public OpposingAuthorizationDecision( @NonNull IAuthorizationDecision opposedToDecision ) {
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
    public String toString() {
      // Example: "Granted (opposing to: <opposed decision description>)"
      return String.format(
        OPPOSING_TO_TEXT,
        getGrantedText(),
        opposedToDecision );
    }
  }

  private static class ImpliedAuthorizationDecision extends AbstractAuthorizationDecision
    implements IImpliedAuthorizationDecision {

    private static final String IMPLIED_BY_TEXT =
      Messages.getInstance().getString( "AuthorizationDecisionFactory.IMPLIED_BY" );

    @NonNull
    private final IAuthorizationDecision impliedByDecision;

    public ImpliedAuthorizationDecision( @NonNull AuthorizationRequest request,
                                         @NonNull IAuthorizationDecision impliedByDecision ) {
      // Same granted state of the implied by decision.
      super( request, impliedByDecision.isGranted() );

      this.impliedByDecision = impliedByDecision;

      if ( request.equals( impliedByDecision.getRequest() ) ) {
        throw new IllegalArgumentException(
          "Argument 'request' cannot be equal to the request of argument 'impliedByDecision'." );
      }
    }

    @NonNull
    @Override
    public IAuthorizationDecision getImpliedFromDecision() {
      return impliedByDecision;
    }

    @Override
    public String toString() {
      // Example: "Granted (implied by: <implied by decision description>)"
      return String.format(
        IMPLIED_BY_TEXT,
        getGrantedText(),
        impliedByDecision );
    }
  }

  private abstract static class AbstractCompositeAuthorizationDecision extends AbstractAuthorizationDecision
    implements ICompositeAuthorizationDecision {

    private static final String COMPOSITE_SEPARATOR_TEXT =
      Messages.getInstance().getString( "AuthorizationDecisionFactory.COMPOSITE_SEPARATOR" );

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
    protected abstract String getTextPattern();

    @Override
    public String toString() {

      String decisionsText = getDecisions()
        .stream()
        .map( Object::toString )
        .collect( Collectors.joining( COMPOSITE_SEPARATOR_TEXT ) );

      return String.format(
        getTextPattern(),
        getGrantedText(),
        decisionsText );
    }
  }

  private static class AllAuthorizationDecision extends AbstractCompositeAuthorizationDecision
    implements IAllAuthorizationDecision {

    private static final String ALL_OF_TEXT = Messages.getInstance().getString( "AuthorizationDecisionFactory.ALL_OF" );

    public AllAuthorizationDecision( @NonNull AuthorizationRequest request,
                                     boolean granted,
                                     @NonNull Set<IAuthorizationDecision> decisions ) {
      super( request, granted, decisions );
    }

    @NonNull
    @Override
    protected String getTextPattern() {
      return ALL_OF_TEXT;
    }
  }

  private static class AnyAuthorizationDecision extends AbstractCompositeAuthorizationDecision
    implements IAnyAuthorizationDecision {

    private static final String ANY_OF_TEXT = Messages.getInstance().getString( "AuthorizationDecisionFactory.ANY_OF" );

    public AnyAuthorizationDecision( @NonNull AuthorizationRequest request,
                                     boolean granted,
                                     @NonNull Set<IAuthorizationDecision> decisions ) {
      super( request, granted, decisions );
    }

    @NonNull
    @Override
    protected String getTextPattern() {
      return ANY_OF_TEXT;
    }
  }
  // endregion
}
