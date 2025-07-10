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
import org.pentaho.platform.api.engine.security.authorization.AuthorizationEvaluationResult;

import java.util.Objects;

public class AuthorizationRulesEngineSettings {
  @NonNull
  private static final AuthorizationRulesEngineSettings DEFAULT = new AuthorizationRulesEngineSettings();

  @NonNull
  private final AuthorizationEvaluationResult defaultResult;

  private final boolean ignoresRuleErrors;

  public AuthorizationRulesEngineSettings() {
    this( AuthorizationEvaluationResult.getDeniedByDefaultResult(), false );
  }

  public AuthorizationRulesEngineSettings( @NonNull AuthorizationEvaluationResult defaultResult,
                                           boolean ignoresRuleErrors ) {
    this.defaultResult = Objects.requireNonNull( defaultResult );
    this.ignoresRuleErrors = ignoresRuleErrors;
  }

  public static AuthorizationRulesEngineSettings getDefault() {
    return DEFAULT;
  }

  @NonNull public AuthorizationEvaluationResult getDefaultResult() {
    return defaultResult;
  }

  public boolean getIgnoresRuleErrors() {
    return ignoresRuleErrors;
  }
}
