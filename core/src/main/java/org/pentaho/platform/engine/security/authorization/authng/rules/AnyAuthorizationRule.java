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
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
public class AnyAuthorizationRule extends AbstractCompositeAuthorizationRule {

  public AnyAuthorizationRule( @NonNull List<IAuthorizationRule> rules ) {
    super( rules );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context )
    throws AuthorizationException {

    Optional<IAuthorizationDecision> combinedResultOptional = Optional.empty();
    Set<IAuthorizationDecision> decisions = null;
    boolean isGranted = false;

    for ( IAuthorizationRule rule : getRules() ) {
      Optional<IAuthorizationDecision> ruleResultOptional = authorizeRule( rule, request, context );
      if ( ruleResultOptional.isPresent() ) {

        if ( combinedResultOptional.isEmpty() ) {
          combinedResultOptional = ruleResultOptional;
          decisions = new LinkedHashSet<>();
        } else {
          // Combine the current rule result with the accumulated result.
          IAuthorizationDecision combinedResult = combineResults( combinedResultOptional.get(),
            ruleResultOptional.get() );
          combinedResultOptional = Optional.of( combinedResult );
        }
        if ( ruleResultOptional.get().isGranted()
          && context.getOptions().getDecisionReportingMode().equals( AuthorizationDecisionReportingMode.SETTLED ) ) {
          // If no need for extra decision info, no need to consult other rules to collect it.
          // Performance matters. Also, simplify result by skipping the wrapper AnyAuthorizationDecision object which
          // would otherwise contain a single element.
          return ruleResultOptional;
        }

        // Collect denied decisions, until we find a granted one, then collect granted only.
      }
    }

    return combinedResultOptional;
  }

  // TODO: define an AnyAuthorizationDecision and a backing builder class... which handles the logic of combining
  // new decisions with the accumulated ones.

  /**
   * Combines the accumulated result with the rule result.
   *
   * @param accumulatedResult The accumulated result so far.
   * @param ruleResult        The result of the current rule .
   * @return The combined result.
   */
  @NonNull
  protected IAuthorizationDecision combineResults(
    @NonNull IAuthorizationDecision accumulatedResult,
    @NonNull IAuthorizationDecision ruleResult ) {

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
