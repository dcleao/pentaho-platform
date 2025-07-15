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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractCompositeAuthorizationRule extends AbstractAuthorizationRule
  implements IAuthorizationRule {

  private static final Log logger = LogFactory.getLog( AbstractCompositeAuthorizationRule.class );

  @NonNull
  private final List<IAuthorizationRule> rules;

  public AbstractCompositeAuthorizationRule( @NonNull List<IAuthorizationRule> rules ) {
    this.rules = List.copyOf( Objects.requireNonNull( rules ) );
  }

  @NonNull
  public List<IAuthorizationRule> getRules() {
    return rules;
  }

  @NonNull
  protected Optional<IAuthorizationDecision> authorizeRule( @NonNull IAuthorizationRule rule,
                                                            @NonNull AuthorizationRequest request,
                                                            @NonNull IAuthorizationContext context )
    throws AuthorizationException {

    try {
      Optional<IAuthorizationDecision> decision = rule.authorize( request, context );

      if ( logger.isDebugEnabled() ) {
        logger.debug( String.format(
          "Rule '%s' authorize: %s, decision: %s",
          rule,
          request,
          decision ) );
      }

      return decision;

    } catch ( AuthorizationException e ) {
      // This exception may be the AuthorizationCycleException, thrown by the context itself.
      // Or it may be a more specific exception, thrown by the rule.
      // Any unchecked exceptions are never caught, are considered unrecoverable from, and will cause an overall
      // failure of the evaluation.

      // TODO: Consider defining an engine setting that controls whether to log and skip the rule, or to throw.

      // Throw back the exception. Log it with an error level.
      logger.error( String.format(
        "Rule '%s' failed authorize: %s. Interrupting evaluation.",
        rule,
        request
      ), e );

      throw e;
    }
  }
}
