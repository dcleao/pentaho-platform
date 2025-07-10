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

package org.pentaho.platform.api.engine.security.authorization.rulesng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * The {@code AuthorizationEvaluationRequest} class encapsulates the details of an authorization evaluation request.
 */
public class AuthorizationEvaluationRequest {
  @NonNull
  private final IAuthorizationUser user;

  // TODO: Not using IAuthorizationAction here to support evaluating unregistered actions
  @NonNull
  private final String actionName;

  /**
   * Constructs an {@code AuthorizationEvaluationRequest} with the specified user and action name.
   *
   * @param user       The user for whom the authorization is being evaluated.
   * @param actionName The name of the action to be evaluated (e.g. {@code org.pentaho.di.repository.create}).
   * @throws IllegalArgumentException if the user is null, or the action name is null or empty.
   */
  public AuthorizationEvaluationRequest( @NonNull IAuthorizationUser user, @NonNull String actionName ) {

    Objects.requireNonNull( user, "Argument 'user' cannot be null." );

    // J.I.C. validation...
    if ( StringUtils.isEmpty( user.getName() ) ) {
      throw new IllegalArgumentException( "Argument 'user' must have a `getName()` which is not null or empty." );
    }

    if ( StringUtils.isEmpty( actionName ) ) {
      throw new IllegalArgumentException( "Argument 'actionName' cannot be null or empty." );
    }

    this.user = user;
    this.actionName = actionName;
  }

  // Helper method so that rules can easily evaluate dependent permissions, for the same user.

  /**
   * Creates a new instance of {@code AuthorizationEvaluationRequest} with the same user but with a different action.
   *
   * @param actionName The name of the action to be evaluated (e.g. {@code org.pentaho.di.repository.create}).
   * @return The new instance.
   * @throws IllegalArgumentException if the action name is null or empty.
   */
  @NonNull
  public AuthorizationEvaluationRequest withAction( @NonNull String actionName ) {
    return new AuthorizationEvaluationRequest( user, actionName );
  }

  @Override
  public boolean equals( Object o ) {
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    AuthorizationEvaluationRequest that = (AuthorizationEvaluationRequest) o;
    return Objects.equals( user, that.user )
      && Objects.equals( actionName, that.actionName );
  }

  @Override
  public int hashCode() {
    return Objects.hash( user, actionName );
  }

  @Override
  public String toString() {
    return String.format(
      "AuthorizationEvaluationRequest{ user=`%s`, action='%s' }",
      user.getName(),
      actionName );
  }
}