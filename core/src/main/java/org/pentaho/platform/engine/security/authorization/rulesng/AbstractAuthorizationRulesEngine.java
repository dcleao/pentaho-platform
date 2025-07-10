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
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationCycleException;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationException;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationOptions;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationRequest;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationEvaluationResult;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationRulesEngineSettings;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationEvaluationContext;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationRulesEngine;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationRulesManager;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public abstract class AbstractAuthorizationRulesEngine implements IAuthorizationRulesEngine {
  /**
   * The {@code AuthorizationEvaluationContext} represents a single authorization evaluation process.
   * It holds the authorization evaluation request, as well as the options for the evaluation.
   * Additionally, it tracks the evaluation path, to detect cycles in the evaluation process.
   * <p>
   * For authorization rules implementations, the context provides evaluation methods allowing rules to base their
   * decisions on the results of the evaluation for other users/actions.
   * <p>
   * The context design also allows for the engine itself to be thread-safe, as each evaluation context is independent.
   */
  protected class AuthorizationEvaluationContext implements IAuthorizationEvaluationContext {

    @NonNull
    private final Deque<AuthorizationEvaluationRequest> evaluationPath = new ArrayDeque<>();

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
    public IAuthorizationRulesEngine getEngine() {
      return AbstractAuthorizationRulesEngine.this;
    }

    @NonNull
    @Override
    public final AuthorizationEvaluationResult evaluate( @NonNull AuthorizationEvaluationRequest request )
      throws AuthorizationEvaluationException {

      Objects.requireNonNull( request );

      if ( evaluationPath.contains( request ) ) {
        throw new AuthorizationEvaluationCycleException( evaluationPath, request );
      }

      evaluationPath.push( request );
      try {
        return getRulesManager().evaluate( request, this );
      } finally {
        evaluationPath.pop();
      }
    }
  }

  @NonNull
  private final IAuthorizationRulesManager rulesManager;

  @NonNull
  private final AuthorizationRulesEngineSettings settings;

  /***
   * Constructs an instance of the authorization rules engine with a rules manager and given settings.
   *
   * @param rulesManager The rules manager to be used by the engine.
   * @param settings The settings for the authorization rules engine.
   */
  protected AbstractAuthorizationRulesEngine(
    @NonNull IAuthorizationRulesManager rulesManager,
    @NonNull AuthorizationRulesEngineSettings settings ) {
    this.rulesManager = Objects.requireNonNull( rulesManager );
    this.settings = Objects.requireNonNull( settings );
  }

  /**
   * Constructs an instance of the authorization rules engine with a rules manager and default settings.
   * <p>
   * The default settings are determined by {@link AuthorizationRulesEngineSettings#getDefault()}.
   *
   * @param rulesManager The rules manager to be used by the engine.
   */
  protected AbstractAuthorizationRulesEngine( @NonNull IAuthorizationRulesManager rulesManager ) {
    this( rulesManager, AuthorizationRulesEngineSettings.getDefault() );
  }

  /**
   * Constructs an instance of the authorization rules engine with a list of OR rules and given settings.
   * <p>
   * The given rules are evaluated using an OR authorization rules manager.
   *
   * @param orRules  The list of OR rules.
   * @param settings The rules' engine settings.
   */
  protected AbstractAuthorizationRulesEngine( @NonNull List<IAuthorizationRule> orRules,
                                              @NonNull AuthorizationRulesEngineSettings settings ) {
    this( new OrAuthorizationRulesManager( orRules ), settings );
  }

  /**
   * Constructs an instance of the authorization rules engine with a list of OR rules and default settings.
   * <p>
   * The given rules are evaluated using an OR authorization rules manager.
   *
   * @param orRules The list of OR rules.
   */
  protected AbstractAuthorizationRulesEngine( @NonNull List<IAuthorizationRule> orRules ) {
    this( new OrAuthorizationRulesManager( orRules ) );
  }


  @NonNull
  @Override
  public final AuthorizationEvaluationResult evaluate( @NonNull AuthorizationEvaluationRequest request,
                                                       @NonNull AuthorizationEvaluationOptions options )
    throws AuthorizationEvaluationException {
    return createContext( options ).evaluate( request );
  }

  @NonNull
  protected AuthorizationEvaluationContext createContext( @NonNull AuthorizationEvaluationOptions options ) {
    return new AuthorizationEvaluationContext( options );
  }

  @NonNull
  protected IAuthorizationRule getRulesManager() {
    return rulesManager;
  }

  @NonNull
  public AuthorizationRulesEngineSettings getSettings() {
    return settings;
  }
}
