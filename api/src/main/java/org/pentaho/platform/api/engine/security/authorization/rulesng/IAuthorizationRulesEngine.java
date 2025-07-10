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
 * The {@code IAuthorizationRulesEngine} interface provides top-level authorization operations.
 * <p>
 * Authorization evaluation combines the results of authorization evaluation of multiple rules.
 * Individual authorization rules can abstain, or grant or deny permission, as well as optionally
 * include reasons for the decision.
 */
public interface IAuthorizationRulesEngine {

  /**
   * Gets the settings for the authorization rules engine.
   * @return The settings.
   */
  @NonNull
  AuthorizationRulesEngineSettings getSettings();

  /**
   * Evaluates if a specified authorization request is allowed, with the given options.
   *
   * @param request The authorization request.
   * @param options The evaluation options.
   * @return The authorization evaluation result.
   * @throws AuthorizationEvaluationCycleException if an evaluation cycle is detected during the evaluation process.
   * @throws AuthorizationEvaluationException      if an evaluation error occurs during the evaluation process.
   */
  @NonNull
  AuthorizationEvaluationResult evaluate(
    @NonNull AuthorizationEvaluationRequest request,
    @NonNull AuthorizationEvaluationOptions options )
    throws AuthorizationEvaluationException;

  // region Sugar methods
  /**
   * Evaluates if a specified authorization request is allowed, with default options.
   * <p>
   * This method is a convenience method equivalent to calling
   * {@code instance.evaluate(request, AuthorizationEvaluationOptions.getDefault())}.
   *
   * @param request The authorization request.
   * @return The authorization evaluation result.
   * @throws AuthorizationEvaluationCycleException if an evaluation cycle is detected during the evaluation process.
   * @throws AuthorizationEvaluationException      if an evaluation error occurs during the evaluation process.
   */
  @NonNull
  default AuthorizationEvaluationResult evaluate( @NonNull AuthorizationEvaluationRequest request )
    throws AuthorizationEvaluationException {
    return evaluate( request, AuthorizationEvaluationOptions.getDefault() );
  }
  // endregion
}
