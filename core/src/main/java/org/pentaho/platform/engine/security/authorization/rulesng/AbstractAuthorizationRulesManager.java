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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationContractException;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationException;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationRequest;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationResult;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationEvaluationContext;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationRulesManager;

import java.util.List;
import java.util.Objects;

public abstract class AbstractAuthorizationRulesManager extends AbstractAuthorizationRule
  implements IAuthorizationRulesManager {

  private static final Log logger = LogFactory.getLog( AbstractAuthorizationRulesManager.class );

  @NonNull
  private final List<IAuthorizationRule> rules;

  public AbstractAuthorizationRulesManager( @NonNull List<IAuthorizationRule> rules ) {
    this.rules = List.copyOf( Objects.requireNonNull( rules ) );
  }

  @Override
  @NonNull
  public List<IAuthorizationRule> getRules() {
    return rules;
  }

  @NonNull
  protected AuthorizationEvaluationResult evaluateRule( @NonNull IAuthorizationRule rule,
                                                        @NonNull AuthorizationEvaluationRequest request,
                                                        @NonNull IAuthorizationEvaluationContext context )
    throws AuthorizationEvaluationException {

    try {
      AuthorizationEvaluationResult result = rule.evaluate( request, context );

      // noinspection ConstantValue
      if ( result == null ) {
        // Misbehaved rule...
        throw new AuthorizationEvaluationContractException(
          String.format(
            "Rule '%s' returned a `null` evaluation result for: %s.",
            rule,
            request ) );
      }

      if ( logger.isDebugEnabled() ) {
        logger.debug( String.format(
          "Rule '%s' evaluated: %s, with result: %s",
          rule,
          request,
          result ) );
      }

      return result;

    } catch ( AuthorizationEvaluationException e ) {
      // This exception may be the AuthorizationEvaluationCycleException, thrown by the context itself.
      // Or it may be a more specific exception, thrown by the rule.
      // Any unchecked exceptions are never caught, are considered unrecoverable from, and will cause an overall
      // failure of the evaluation.

      if ( !context.getEngine().getSettings().getIgnoresRuleErrors() ) {
        // Throw back the exception. Log it with an error level.
        logger.error( String.format(
          "Rule '%s' failed evaluation of: %s. Interrupting evaluation.",
          rule,
          request
        ), e );

        throw e;
      }

      // Skip the rule, but log the exception with a warning level.
      logger.warn( String.format(
        "Rule '%s' failed evaluation of: %s. Continuing evaluation, ignoring rule.",
        rule,
        request
      ), e );

      // TODO: Consider defining a DENIED_BY_FAILURE reason?
      return AuthorizationEvaluationResult.deny();
    }
  }
}
