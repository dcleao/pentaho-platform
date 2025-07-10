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

package org.pentaho.platform.engine.security.authorization.rulesng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationEvaluationCycleException;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationEvaluationException;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationEvaluationResult;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * The {@code AuthorizationEvaluationPath} class is used to track the evaluation path of authorization checks.
 * It is used to track the evaluation steps and enable detecting cycles in the evaluation process.
 */
public class AuthorizationEvaluationPath {
  @FunctionalInterface
  public interface ISettledAuthorizationEvaluator {
    @NonNull
    AuthorizationEvaluationResult evaluate(
      @NonNull IAuthorizationUser user,
      @NonNull String actionName )
      throws AuthorizationEvaluationException;
  }

  /**
   * Represents a step in the evaluation path, consisting of a user and an action name.
   */
  private static class EvaluationStep {
    @NonNull
    public final IAuthorizationUser user;
    @NonNull
    public final String actionName;

    public EvaluationStep( @NonNull IAuthorizationUser user, @NonNull String actionName ) {

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

    @Override
    public boolean equals( Object o ) {
      if ( o == null || getClass() != o.getClass() ) {
        return false;
      }

      EvaluationStep that = (EvaluationStep) o;
      return Objects.equals( user, that.user )
        && Objects.equals( actionName, that.actionName );
    }

    @Override
    public int hashCode() {
      return Objects.hash( user, actionName );
    }
  }

  @NonNull
  private final Deque<EvaluationStep> evaluationPath = new ArrayDeque<>();

  @NonNull
  public AuthorizationEvaluationResult step( @NonNull IAuthorizationUser user,
                                             @NonNull String actionName,
                                             @NonNull ISettledAuthorizationEvaluator evaluator )
    throws AuthorizationEvaluationException {

    // Arguments' validation is done in the EvaluationStepKey constructor.
    EvaluationStep newStep = new EvaluationStep( user, actionName );
    if ( evaluationPath.contains( newStep ) ) {
      throw new AuthorizationEvaluationCycleException(
        String.format( "Evaluation cycle detected for user: '%s', action: '%s'", user.getName(), actionName ) );
    }

    evaluationPath.push( newStep );
    try {
      return evaluator.evaluate( user, actionName );
    } finally {
      // Pop the evaluation step from the path.
      evaluationPath.pop();
    }
  }
}
