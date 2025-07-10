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
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationException;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationOptions;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationRequest;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationResult;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationEvaluationContext;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationRule;

import java.util.List;

/**
 * The {@code OrAuthorizationRulesManager} class is an authorization rules manager which combines the results of a list
 * of authorization rules using a logical "OR" operation.
 * <p>
 * When the evaluation options are set to {@link AuthorizationEvaluationOptions#getIncludesReasons()} include reasons},
 * the combined result will include all reasons for the rule results with the same granted status as the combine
 * decision (either granted or denied).
 * If the combined decision is granted, the result includes the reasons of every granted rule result.
 * If the combined decision is denied, the result includes the reasons of every rule result.
 */
public class OrAuthorizationRulesManager extends AbstractAuthorizationRulesManager {

  public OrAuthorizationRulesManager( @NonNull List<IAuthorizationRule> rules ) {
    super( rules );
  }

  @NonNull
  @Override
  public AuthorizationEvaluationResult evaluate( @NonNull AuthorizationEvaluationRequest request,
                                                 @NonNull IAuthorizationEvaluationContext context )
    throws AuthorizationEvaluationException {
    // Deny by default.
    AuthorizationEvaluationResult combinedResult = AuthorizationEvaluationResult.deny();

    for ( IAuthorizationRule rule : getRules() ) {
      AuthorizationEvaluationResult ruleResult = evaluateRule( rule, request, context );

      combinedResult = combineResults( combinedResult, ruleResult );

      if ( combinedResult.isGranted() && !context.getOptions().getIncludesReasons() ) {
        // If reasons not to be included, no need to consult other rules to collect these.
        // Performance matters.
        break;
      }
    }

    return combinedResult;
  }

  /**
   * Combines the accumulated result with the rule result.
   *
   * @param accumulatedResult The accumulated result so far.
   * @param ruleResult        The result of the current rule evaluation.
   * @return The combined result.
   */
  @NonNull
  protected AuthorizationEvaluationResult combineResults(
    @NonNull AuthorizationEvaluationResult accumulatedResult,
    @NonNull AuthorizationEvaluationResult ruleResult ) {

    if ( accumulatedResult == ruleResult ) {
      return accumulatedResult;
    }

    // Same decision, granted or denied.
    // Combine the reasons.
    if ( accumulatedResult.isGranted() == ruleResult.isGranted() ) {
      return accumulatedResult.addReasons( ruleResult.getReasons() );
    }

    // Different decisions, one granted, another denied.
    // Granted wins over denied, given this is an Or manager.
    return accumulatedResult.isGranted() ? accumulatedResult : ruleResult;
  }
}
