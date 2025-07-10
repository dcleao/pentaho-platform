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

/**
 * The {@code IAuthorizationRule} interface defines a rule for evaluating whether a user can perform a specific action.
 * Authorization rules provide a means to distribute the authorization logic across different components.
 * An authorization engine combines the results of multiple authorization rules to determine if a user is authorized to
 * perform an action.
 * <p>
 * Implementations of this interface should be thread-safe.
 * <p>
 * Implementations should override the {@link Object#toString()} method to provide a meaningful description of the rule,
 * appropriate for including in exception messages, and for logging and debugging purposes.
 */
@FunctionalInterface
public interface IAuthorizationRule {
  /**
   * Evaluates if a specified authorization request is allowed, under a given authorization evaluation context.
   *
   * @param request The authorization evaluation request.
   * @param context The authorization context to evaluate against.
   * @return The result of the rule evaluation.
   * @throws AuthorizationEvaluationCycleException if an evaluation cycle is detected for the specified user and
   *                                               action name.
   * @throws AuthorizationEvaluationException      if an evaluation error occurs during the evaluation process.
   */
  @NonNull
  AuthorizationEvaluationResult evaluate(
    @NonNull AuthorizationEvaluationRequest request,
    @NonNull IAuthorizationEvaluationContext context )
    throws AuthorizationEvaluationException;
}