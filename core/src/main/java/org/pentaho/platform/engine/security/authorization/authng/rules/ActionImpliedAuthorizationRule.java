/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.engine.security.authorization.authng.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IImpliedAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.pentaho.platform.engine.security.authorization.authng.AuthorizationDecisions.impliedBy;

/**
 * The {@code ActionImpliedAuthorizationRule} class represents an authorization rule that grants permission to execute
 * one or more actions (the implied / consequent / derived ones) based on the existing permission to execute another
 * action (the implied-by / antecedent / base one).
 * <p>
 * If permission is granted to perform the base action, then permission is also granted to perform any of the derived
 * actions. In all other cases, the rule abstains from making a decision.
 * <p>
 * The decisions made by this rule are always of type {@link IImpliedAuthorizationDecision}, having as its
 * {@link IImpliedAuthorizationDecision#getImpliedByDecision() implied-by decision} the result of authorizing an equal
 * request but with the base action instead.
 */
public class ActionImpliedAuthorizationRule extends AbstractAuthorizationRule {

  @NonNull
  private final IAuthorizationAction baseAction;

  @NonNull
  private final Set<IAuthorizationAction> derivedActions;

  public ActionImpliedAuthorizationRule( @NonNull IAuthorizationAction baseAction,
                                         @NonNull Set<IAuthorizationAction> derivedActions ) {
    this.baseAction = Objects.requireNonNull( baseAction );
    this.derivedActions = Set.copyOf( derivedActions );
  }


  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context )
    throws AuthorizationException {

    // If not asking about one of the potentially derived actions, abstain.
    if ( !derivedActions.contains( request.getAction() ) ) {
      return Optional.empty();
    }

    var baseDecision = context.authorize( request.withAction( baseAction ) );
    return baseDecision.isDenied()
      // If denied for the base action, abstain.
      ? Optional.empty()
      // Else grant for the derived action.
      : Optional.of( impliedBy( baseDecision ) );
  }
}
