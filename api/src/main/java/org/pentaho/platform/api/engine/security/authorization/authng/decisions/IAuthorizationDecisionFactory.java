package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Set;

public interface IAuthorizationDecisionFactory {
  @NonNull
  IAuthorizationDecision grant();

  @NonNull
  IAuthorizationDecision deny();

  @NonNull
  IAuthorizationDecision valueOf( boolean granted );

  @NonNull
  IAnyAuthorizationDecision anyOf( boolean granted, @NonNull Set<IAuthorizationDecision> decisions );

  @NonNull
  IAllAuthorizationDecision allOf( boolean granted, @NonNull Set<IAuthorizationDecision> decisions );

  @NonNull
  IOpposingAuthorizationDecision opposingTo( @NonNull IAuthorizationDecision opposingToDecision );

  @NonNull
  IImpliedAuthorizationDecision impliedBy( @NonNull IAuthorizationDecision impliedByDecision );
}
