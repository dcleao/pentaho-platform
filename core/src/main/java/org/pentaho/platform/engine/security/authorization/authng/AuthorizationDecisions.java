package org.pentaho.platform.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecisionFactory;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IImpliedAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IOpposingAuthorizationDecision;

import java.util.Set;

/**
 * The {@code AuthorizationDecisions} class provides static utility methods to create instances of various authorization
 * decisions.
 * <p>
 * The static methods roughly mirror those of the {@link IAuthorizationDecisionFactory} interface, allowing for easy
 * use of this basic functionality, possibly via static imports. For more advanced uses, or for more control or
 * modularity, an instance of {@link IAuthorizationDecisionFactory} can be used directly.
 * <p>
 * This class uses a singleton instance of {@link AuthorizationDecisionFactory} class to create decisions, which can be
 * obtained via {@link #getFactory()}.
 */
public class AuthorizationDecisions {

  private AuthorizationDecisions() {
    // static utility class.
  }

  private static final IAuthorizationDecisionFactory decisionFactory = new AuthorizationDecisionFactory();

  @NonNull
  public static IAuthorizationDecisionFactory getFactory() {
    return decisionFactory;
  }

  @NonNull
  public static IAuthorizationDecision grant() {
    return decisionFactory.grant();
  }

  @NonNull
  public static IAuthorizationDecision deny() {
    return decisionFactory.deny();
  }

  @NonNull
  public static IAuthorizationDecision valueOf( boolean granted ) {
    return decisionFactory.valueOf( granted );
  }

  @NonNull
  public static IAnyAuthorizationDecision anyOf( boolean granted, @NonNull Set<IAuthorizationDecision> decisions ) {
    return decisionFactory.anyOf( granted, decisions );
  }

  @NonNull
  public static IAllAuthorizationDecision allOf( boolean granted, @NonNull Set<IAuthorizationDecision> decisions ) {
    return decisionFactory.allOf( granted, decisions );
  }

  @NonNull
  public static IOpposingAuthorizationDecision opposingTo( @NonNull IAuthorizationDecision opposingToDecision ) {
    return decisionFactory.opposingTo( opposingToDecision );
  }

  @NonNull
  public static IImpliedAuthorizationDecision impliedBy( @NonNull IAuthorizationDecision impliedByDecision ) {
    return decisionFactory.impliedBy( impliedByDecision );
  }
}
