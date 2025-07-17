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
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IOpposingAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;
import org.pentaho.platform.engine.security.authorization.authng.AuthorizationDecisions;

import java.util.Objects;
import java.util.Optional;

/**
 * The {@code OpposingAuthorizationRule} class represents an authorization rule whose decision is always opposite
 * of that of another rule. Abstentions are preserved.
 * <p>
 * The decisions made by this rule are always of type {@link IOpposingAuthorizationDecision}, and have as its
 * {@link IOpposingAuthorizationDecision#getOpposedToDecision() opposing-to decision} the result of authorizing the
 * opposing to rule for the same request.
 */
public class OpposingAuthorizationRule extends AbstractAuthorizationRule {

  @NonNull
  private final IAuthorizationRule opposingToRule;

  public OpposingAuthorizationRule( @NonNull IAuthorizationRule opposingToRule ) {
    this.opposingToRule = Objects.requireNonNull( opposingToRule );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context )
    throws AuthorizationException {

    return opposingToRule.authorize( request, context )
      .map( AuthorizationDecisions::opposingTo );
  }
}
