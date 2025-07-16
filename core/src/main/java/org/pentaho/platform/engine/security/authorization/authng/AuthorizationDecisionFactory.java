package org.pentaho.platform.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecisionFactory;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.ICompositeAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IImpliedAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IOpposingAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizationDecisionFactory implements IAuthorizationDecisionFactory {
  private static final IAuthorizationDecision GRANTED = new AuthorizationDecision( true );
  private static final IAuthorizationDecision DENIED = new AuthorizationDecision( false );

  @NonNull
  @Override
  public IAuthorizationDecision grant() {
    return GRANTED;
  }

  @NonNull
  @Override
  public IAuthorizationDecision deny() {
    return DENIED;
  }

  @NonNull
  @Override
  public IAuthorizationDecision valueOf( boolean granted ) {
    return granted ? GRANTED : DENIED;
  }

  @NonNull
  @Override
  public IAnyAuthorizationDecision anyOf( boolean granted, @NonNull Set<IAuthorizationDecision> decisions ) {
    return new AnyAuthorizationDecision( granted, decisions );
  }

  @NonNull
  @Override
  public IAllAuthorizationDecision allOf( boolean granted, @NonNull Set<IAuthorizationDecision> decisions ) {
    return new AllAuthorizationDecision( granted, decisions );
  }

  @NonNull
  @Override
  public IOpposingAuthorizationDecision opposingTo( @NonNull IAuthorizationDecision opposingToDecision ) {
    return new OpposingAuthorizationDecision( opposingToDecision );
  }

  @NonNull
  @Override
  public IImpliedAuthorizationDecision impliedBy( @NonNull IAuthorizationDecision impliedByDecision ) {
    return new ImpliedAuthorizationDecision( impliedByDecision );
  }

  // region Standard decision types' implementations
  private static class AuthorizationDecision implements IAuthorizationDecision {

    private static final String GRANTED_DESCRIPTION =
      Messages.getInstance().getString( "AuthorizationDecision.GRANTED" );
    private static final String DENIED_DESCRIPTION = Messages.getInstance().getString( "AuthorizationDecision.DENIED" );

    private final boolean granted;

    public AuthorizationDecision( boolean granted ) {
      this.granted = granted;
    }

    @Override
    public boolean isGranted() {
      return granted;
    }

    protected String getGrantedText() {
      return isGranted() ? GRANTED_DESCRIPTION : DENIED_DESCRIPTION;
    }
  }

  private static class OpposingAuthorizationDecision extends AuthorizationDecision
    implements IOpposingAuthorizationDecision {

    private static final String OPPOSING_TO_TEXT =
      Messages.getInstance().getString( "AuthorizationDecisionFactory.OPPOSING_TO" );

    @NonNull
    private final IAuthorizationDecision opposedToDecision;

    public OpposingAuthorizationDecision( @NonNull IAuthorizationDecision opposedToDecision ) {
      // Negate the granted state of the opposed decision.
      super( !opposedToDecision.isGranted() );

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

  private static class ImpliedAuthorizationDecision extends AuthorizationDecision
    implements IImpliedAuthorizationDecision {

    private static final String IMPLIED_BY_TEXT =
      Messages.getInstance().getString( "AuthorizationDecisionFactory.IMPLIED_BY" );

    @NonNull
    private final IAuthorizationDecision impliedByDecision;

    public ImpliedAuthorizationDecision( @NonNull IAuthorizationDecision impliedByDecision ) {
      // Same granted state of the implied by decision.
      super( impliedByDecision.isGranted() );

      this.impliedByDecision = impliedByDecision;
    }

    @NonNull
    @Override
    public IAuthorizationDecision getImpliedByDecision() {
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

  private abstract static class CompositeAuthorizationDecision extends AuthorizationDecision
    implements ICompositeAuthorizationDecision {

    private static final String COMPOSITE_SEPARATOR_TEXT =
      Messages.getInstance().getString( "AuthorizationDecisionFactory.COMPOSITE_SEPARATOR" );

    @NonNull
    private final Set<IAuthorizationDecision> decisions;

    protected CompositeAuthorizationDecision( boolean granted, @NonNull Set<IAuthorizationDecision> decisions ) {
      super( granted );
      this.decisions = Objects.requireNonNull( decisions );
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
        getGrantedText(),
        super.toString(),
        decisionsText );
    }
  }

  private static class AllAuthorizationDecision extends CompositeAuthorizationDecision
    implements IAllAuthorizationDecision {

    private static final String ALL_OF_TEXT = Messages.getInstance().getString( "AuthorizationDecisionFactory.ALL_OF" );

    public AllAuthorizationDecision( boolean granted, @NonNull Set<IAuthorizationDecision> decisions ) {
      super( granted, decisions );
    }

    @NonNull
    @Override
    protected String getTextPattern() {
      return ALL_OF_TEXT;
    }
  }

  private static class AnyAuthorizationDecision extends CompositeAuthorizationDecision
    implements IAnyAuthorizationDecision {

    private static final String ANY_OF_TEXT = Messages.getInstance().getString( "AuthorizationDecisionFactory.ANY_OF" );

    public AnyAuthorizationDecision( boolean granted, @NonNull Set<IAuthorizationDecision> decisions ) {
      super( granted, decisions );
    }

    @NonNull
    @Override
    protected String getTextPattern() {
      return ANY_OF_TEXT;
    }
  }
  // endregion
}
