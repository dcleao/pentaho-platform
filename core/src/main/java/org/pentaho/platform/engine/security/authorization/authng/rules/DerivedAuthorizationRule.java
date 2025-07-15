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
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;
import org.pentaho.platform.engine.security.messages.Messages;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The {@code DerivedAuthorizationRule} class represents an authorization rule that derives permissions to actions from
 * the permission to a main action.
 * If permission is granted to perform the main action, then permission is also granted to perform any of the derived
 * actions.
 */
public class DerivedAuthorizationRule extends AbstractAuthorizationRule {

  public static final String REASON_CODE = "org.pentaho.derived-permission";
  private static final String REASON_TEXT = DerivedAuthorizationRule.class.getSimpleName() + "_reason";

  @NonNull
  private final IAuthorizationAction mainAction;

  @NonNull
  private final Set<IAuthorizationAction> derivedActions;

  public DerivedAuthorizationRule( @NonNull IAuthorizationAction mainAction,
                                   @NonNull Set<IAuthorizationAction> derivedActions ) {
    this.mainAction = Objects.requireNonNull( mainAction );
    this.derivedActions = Set.copyOf( derivedActions );
  }


  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context )
    throws AuthorizationException {

    if ( !derivedActions.contains( request.getAction() )
      || context.authorize( request.withAction( mainAction ) ).isDenied() ) {
      return deny();
    }

    if ( context.getOptions().getIncludesReasons() ) {
      return grant(
        REASON_CODE,
        Messages.getInstance().getString(
          REASON_TEXT,
          request.getUser().getName(),
          request.getAction().getLocalizedDisplayName(),
          mainAction.getLocalizedDisplayName() ) );
    }

    return grant();
  }
}
