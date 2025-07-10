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

package org.pentaho.platform.api.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;

/**
 * The {@code IAuthorizationRule} interface defines a rule for evaluating whether a user can perform a specific action.
 * Authorization rules provide a means to distribute the authorization logic across different components.
 * An authorization engine combines the results of multiple authorization rules to determine if a user is authorized to
 * perform an action. Authorization rules may provide grant or deny decisions for only certain cases, abstaining in
 * others.
 */
public interface IAuthorizationRule {
  /**
   * Evaluates if a user can perform an action.
   * <p>
   * Returns an empty {@link Optional} to abstain.
   * Returns an optional with a present {@link AuthorizationEvaluationResult} value for granting or denying permission.
   *
   * @param user       The user for whom the authorization is being evaluated.
   * @param actionName The name of the action (e.g. {@code org.pentaho.di.repository.create}).
   * @param context    The authorization context to evaluate against.
   * @return The optional result of the rule evaluation.
   * @throws IllegalArgumentException              if the action name is {@code null} or empty.
   * @throws AuthorizationEvaluationCycleException if an evaluation cycle is detected for the specified user and
   *                                               action name.
   * @throws AuthorizationEvaluationException      if an evaluation error occurs during the evaluation process.
   */
  @NonNull
  Optional<AuthorizationEvaluationResult> evaluate(
    @NonNull IAuthorizationUser user,
    // ??? Not using IAuthorizationAction here to support evaluating unregistered actions
    @NonNull String actionName,
    @NonNull IAuthorizationEvaluationContext context )
    throws AuthorizationEvaluationException;
}
