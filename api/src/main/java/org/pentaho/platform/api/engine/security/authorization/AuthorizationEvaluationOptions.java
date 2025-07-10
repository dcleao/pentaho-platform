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

/**
 * The {@code AuthorizationEvaluationOptions} class encapsulates options for the authorization evaluation process.
 */
public class AuthorizationEvaluationOptions {

  private static final AuthorizationEvaluationOptions DEFAULT = new AuthorizationEvaluationOptions();

  public static AuthorizationEvaluationOptions getDefault() {
    return DEFAULT;
  }

  private final boolean includesReasons;

  public AuthorizationEvaluationOptions() {
    this( false );
  }

  public AuthorizationEvaluationOptions( boolean includesReasons ) {
    this.includesReasons = includesReasons;
  }

  /**
   * Indicates whether the evaluation results should include reasons.
   * <p>
   * When set to {@code true}, the evaluation result should include reasons for their decision.
   * All rules that support a grant or deny decision should include reasons for their decision.
   * When combining reasons from multiple rules, the following rules apply:
   * <ul>
   *   <li>If the result is granted, only and all the reasons for the granted decision will be included.</li>
   *   <li>If the result is denied, only and all reasons for the denied decision will be included.</li>
   * </ul>
   * <p>
   * When set to {@code false}, the evaluation result may still include reasons. However, in general, these will be
   * incomplete, given that, for efficiency reasons, not all rules are consulted and evaluation is finished as soon
   * as the decision is settled.
   *
   * @return {@code true} if reasons should be included; {@code false}, otherwise.
   */
  public boolean getIncludesReasons() {
    return includesReasons;
  }
}
