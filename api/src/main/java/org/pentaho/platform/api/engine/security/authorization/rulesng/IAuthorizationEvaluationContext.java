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
 * The {@code IAuthorizationContext} interface defines the context for an authorization evaluation process initiated by
 * {@link IAuthorizationRulesEngine}. The context is passed as an argument to evaluation rules methods, such as
 * {@link IAuthorizationRule#evaluate(IAuthorizationUser, String, IAuthorizationEvaluationContext)}.
 */
public interface IAuthorizationEvaluationContext {
  /**
   * Gets the options for the authorization evaluation.
   *
   * @return The options for the authorization evaluation.
   */
  @NonNull
  AuthorizationEvaluationOptions getOptions();

  @NonNull
  IAuthorizationRulesEngine getEngine();

  /**
   * Evaluates if a specified authorization request is allowed, in the current context.
   *
   * @param request The authorization evaluation request.
   * @return The result of the evaluation.
   * @throws AuthorizationEvaluationCycleException if an evaluation cycle is detected for the specified request.
   * @throws AuthorizationEvaluationException      if an evaluation error occurs during the evaluation process.
   */
  @NonNull
  AuthorizationEvaluationResult evaluate( @NonNull AuthorizationEvaluationRequest request )
    throws AuthorizationEvaluationException;
}
