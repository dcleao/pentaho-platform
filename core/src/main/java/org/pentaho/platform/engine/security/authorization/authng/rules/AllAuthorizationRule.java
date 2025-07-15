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
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;
import org.pentaho.platform.engine.security.authorization.authng.decisions.AllAuthorizationDecision;

import java.util.List;
import java.util.Optional;

/**
 * The {@code AllAuthorizationRule} class is an authorization rules manager which combines the results of a list
 * of authorization rules using a logical "AND" operation.
 * <p>
 * When the evaluation options are set to {@link AuthorizationOptions#getDecisionReportingMode()} include reasons},
 * the combined result will include all reasons for the rule results with the same granted status as the combine
 * decision (either granted or denied).
 * If the combined decision is granted, the result includes the reasons of every rule result.
 * If the combined decision is denied, the result includes the reasons of every denied rule result.
 */
public class AllAuthorizationRule extends AbstractCompositeAuthorizationRule {

  public AllAuthorizationRule( @NonNull List<IAuthorizationRule> rules ) {
    super( rules );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context )
    throws AuthorizationException {

    // Grant by default.
    AllAuthorizationDecision.Builder allResultBuilder = new AllAuthorizationDecision.Builder();

    for ( IAuthorizationRule rule : getRules() ) {

      Optional<IAuthorizationDecision> ruleResultOptional = authorizeRule( rule, request, context );

      if ( ruleResultOptional.isPresent() ) {
        IAuthorizationDecision ruleDecision = ruleResultOptional.get();

        if ( allResultBuilder.isEmpty() ) {
          allResultBuilder
            .granted( ruleDecision.isGranted() )
            .withDecision( ruleDecision );
        } else if ( allResultBuilder.isGranted() == ruleDecision.isGranted() ) {
          // Accumulate decisions having the same granted status, granted or denied.
          allResultBuilder.withDecision( ruleDecision );
        } else if ( allResultBuilder.isGranted() ) {
          // Denied decisions override any granted ones.
          allResultBuilder
            .reset()
            .deny()
            .withDecision( ruleDecision );
        } // else ignore the granted decision, if already in a denied state, as it does not affect the outcome.

        if ( allResultBuilder.isDenied()
          && context.getOptions().getDecisionReportingMode().equals( AuthorizationDecisionReportingMode.SETTLED ) ) {
          // If the decision is settled/denied and the reporting mode is minimal, no need to evaluate other rules.
          break;
        }
      }
    }

    return allResultBuilder.build();
  }
}
