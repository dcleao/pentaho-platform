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
import org.pentaho.platform.api.engine.security.authorization.AuthorizationEvaluationException;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationEvaluationOptions;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationEvaluationResult;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationEvaluationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRulesEngine;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractAuthorizationRulesEngine implements IAuthorizationRulesEngine {
  /**
   * The {@code AuthorizationEvaluationContext} represents a single authorization evaluation process.
   * It holds evaluations' user and action name, as well as the options for the evaluation.
   * Additionally, it tracks the evaluation path, to detect cycles in the evaluation process.
   * <p>
   * For authorization rules implementations, the context provides the evaluation methods, so that rules may base their
   * decisions on the results of that for other users/actions.
   * <p>
   * This design also allows for the engine itself to be thread-safe, as each evaluation context is independent.
   * <p>
   * Inheritors may override the {@link #evaluateRules(IAuthorizationUser, String)} method to provide a custom rules
   * result combination logic, for the rules provided by {@link AbstractAuthorizationRulesEngine#getRules()}.
   * The method {@link #evaluateRule(IAuthorizationRule, IAuthorizationUser, String)} must be called to handle each
   * rule's evaluation.
   * <p>
   * Inheritors may override {@link #evaluateRule(IAuthorizationRule, IAuthorizationUser, String)} to provide a custom
   * way to call and log rule evaluation, including error handling.
   */
  protected class AuthorizationEvaluationContext implements IAuthorizationEvaluationContext {

    @NonNull
    private final AuthorizationEvaluationPath evaluationPath = new AuthorizationEvaluationPath();

    @NonNull
    private final AuthorizationEvaluationOptions options;

    public AuthorizationEvaluationContext( @NonNull AuthorizationEvaluationOptions options ) {
      this.options = Objects.requireNonNull( options );
    }

    @NonNull
    @Override
    public AuthorizationEvaluationOptions getOptions() {
      return options;
    }

    @NonNull
    @Override
    public final AuthorizationEvaluationResult evaluate( @NonNull IAuthorizationUser user, @NonNull String actionName )
      throws AuthorizationEvaluationException {
      // Basic argument validation is done by EvaluationPath.
      return evaluationPath.step( user, actionName, this::evaluateRules );
    }

    @NonNull
    protected AuthorizationEvaluationResult evaluateRules( @NonNull IAuthorizationUser user,
                                                           @NonNull String actionName )
      throws AuthorizationEvaluationException {

      // NOTE: Opting not to use streams given that it then just makes it harder to deal with checked exceptions thrown
      // by individual rules..., forcing use of auxiliary wrapper unchecked exceptions to bring the original ones
      // safely out of the stream...
      //
      // TODO: remove the following after code review.
      // Apart from exception handling, it would look something like:
      // <code>
      //   rules.stream()
      //        .map( rule -> rule.evaluate( user, actionName, this ) )
      //        .filter( Optional::isPresent )
      //        .map( Optional::get )
      //        .reduce( AuthorizationEvaluationResult::combine )
      //
      //        // When not combining results, stick with the first value, if any.
      //        .filter( r -> !combineResults )
      //
      //        // Default result, when no rules, or all rules abstain.
      //        .orElseGet( settings::getDefaultResult );
      // </code>
      // I find this less readable than the imperative code below, anyway.

      // The combined result is granted if there is at least one rule that grants it,
      // and there are no rules that deny it.
      //
      // Denials are like vetoes: they are stronger than grants.

      // No rules should be able to veto an administrator having access to everything!
      // Need either invariant rules, checked first, or override the engine evaluate(...) method to ensure that.

      // Can be used to ensure certain invariants, such as:
      // - "a resource authorization grant requiring the global/self authorization grant as well"
      //   thus, if there is no global/self authorization grant, then deny the resource authorization grant.
      //   no grant rule would be able to override this, and in fact, a grant rule for this case could then
      //   even focus only on the resource-level authorization grant, leaving the global/self evaluation to the
      //   other rule.
      // Unfortunately, this cannot be used to define a rule that grants every permission to an Administrator user,
      // as another rule could deny it...
      // To that end, we'd need to define rules which enforce invariants whenever they produce a grant/deny result.
      // Those rules could be checked first. Only when abstaining, would the other rules be checked.
      //   Alternatively, an engine subclass could override the evaluate(...) method to ensure any invariants, before
      //   delegating control to rules!
      //   Might make more sense since some of these are true Pentaho System invariants, and not just business rules.
      //   Just have a PentahoAuthorizationRulesEngine that overrides a AbstractAuthorizationRulesEngine ?
      //
      // With either in place, would rule denial still be useful?
      //   - These carry a cost in performance and in api complexity, so we should avoid them if possible...
      //   - To integrate with more complex business rules systems in the future?
      //
      // Which cases would require rule denial and a variable final default value?
      //   - Admin-only access to very special resources?
      //     - A rule that denies access to a resource if the user is not an Administrator, but grants it otherwise
      //     (always need a grant).
      //       - Another rule could then deny access to the admin, which is dangerous!
      //     This is a plausible business rule, which would make sense to be defined as a rule (instead of an engine
      //     override).

      // Empty is the neutral/abstention generalized result.
      Optional<AuthorizationEvaluationResult> combinedResultOptional = Optional.empty();

      for ( IAuthorizationRule rule : getRules() ) {
        @NonNull
        Optional<AuthorizationEvaluationResult> ruleResultOptional = evaluateRule( rule, user, actionName );

        // When present: rule grant or deny decision.
        // When empty:   rule abstained or failed-and-was-ignored.
        if ( ruleResultOptional.isPresent() ) {
          // Combine the rule result with the previous combined result, if any.
          combinedResultOptional = combinedResultOptional
            .map( combinedResult ->
              AuthorizationEvaluationResult.combine( combinedResult, ruleResultOptional.get() ) )
            .or( () -> ruleResultOptional );

          // Surely combinedResultOptional.isPresent(), after combining with a present rule result.

          if ( combinedResultOptional.get().isDenied() && !options.getIncludesReasons() ) {
            // No way out of a denial result.
            // If reasons not to be included, no need to consult other rules to collect these.
            // Performance is very important...
            return combinedResultOptional.get();
          }
        }
      }

      // No rules, or all rules abstained/failed.
      // TODO: alternatively, we could support a default rule that could evaluate the call,
      // which would be required to always return a non-empty result (or an exception would be thrown).
      // Would be more dynamic than having a fixed default result.
      // But, are there any use cases where the default result should not be set as a constant?
      // Shouldn't the rule composition semantics be independent of the user and action being evaluated?
      // Some rules languages allow for a default result, but it affects only a single declarative rule,
      // like the "default" branch of a switch statement.
      // It's also related to supporting "deny" rules, which would be more complex to handle
      // if we had a default result that could be overridden by rules.
      return combinedResultOptional.orElseGet( getSettings()::getDefaultResult );
    }

    @NonNull
    protected Optional<AuthorizationEvaluationResult> evaluateRule( @NonNull IAuthorizationRule rule,
                                                                    @NonNull IAuthorizationUser user,
                                                                    @NonNull String actionName )
      throws AuthorizationEvaluationException {

      try {
        Optional<AuthorizationEvaluationResult> result = rule.evaluate( user, actionName, this );

        if ( logger.isDebugEnabled() ) {
          logger.debug( String.format(
            "Rule '%s' evaluated for user: '%s', action: '%s'. Result: %s",
            rule.getClass().getTypeName(),
            user.getName(),
            actionName,
            result.isPresent() ? result.get() : "abstained"
          ) );
        }

        return result;

      } catch ( AuthorizationEvaluationException e ) {
        // This exception may be the AuthorizationEvaluationCycleException, thrown by the context itself.
        // Or it may be a more specific exception, thrown by the rule.
        // Any unchecked exceptions are never caught, are considered unrecoverable from, and will cause an overall
        // failure of the evaluation.

        if ( !getSettings().getIgnoresRuleErrors() ) {
          // Throw back the exception. Log it in error level.
          logger.error( String.format(
            "Rule '%s' failed evaluation for user: '%s', action: '%s'. Interrupting evaluation.",
            rule.getClass().getTypeName(),
            user.getName(),
            actionName
          ), e );

          throw e;
        }

        // Skip the rule, but log the exception in warning level.
        logger.warn( String.format(
          "Rule '%s' failed evaluation for user: '%s', action: '%s'. Continuing evaluation, ignoring rule.",
          rule.getClass().getTypeName(),
          user.getName(),
          actionName
        ), e );

        return Optional.empty();
      }
    }
  }

  // endregion

  private static final Log logger = LogFactory.getLog( AbstractAuthorizationRulesEngine.class );

  @NonNull
  private final List<IAuthorizationRule> rules;

  @NonNull
  private final AuthorizationRulesEngineSettings settings;

  public AbstractAuthorizationRulesEngine( @NonNull List<IAuthorizationRule> rules ) {
    this( rules, AuthorizationRulesEngineSettings.getDefault() );
  }

  public AbstractAuthorizationRulesEngine(
    @NonNull List<IAuthorizationRule> rules,
    @NonNull AuthorizationRulesEngineSettings settings ) {
    this.rules = Objects.requireNonNull( rules );
    this.settings = Objects.requireNonNull( settings );
  }

  @NonNull
  @Override
  public final AuthorizationEvaluationResult evaluate( @NonNull IAuthorizationUser user,
                                                       @NonNull String actionName,
                                                       @NonNull AuthorizationEvaluationOptions options )
    throws AuthorizationEvaluationException {
    return createContext( options ).evaluate( user, actionName );
  }

  @NonNull
  protected AuthorizationEvaluationContext createContext( @NonNull AuthorizationEvaluationOptions options ) {
    return new AuthorizationEvaluationContext( options );
  }

  @NonNull
  protected List<IAuthorizationRule> getRules() {
    return rules;
  }

  @NonNull
  public AuthorizationRulesEngineSettings getSettings() {
    return settings;
  }
}
