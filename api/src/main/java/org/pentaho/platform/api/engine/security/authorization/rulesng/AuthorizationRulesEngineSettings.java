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

public class AuthorizationRulesEngineSettings {
  @NonNull
  private static final AuthorizationRulesEngineSettings DEFAULT = new AuthorizationRulesEngineSettings();

  private final boolean ignoresRuleErrors;

  public AuthorizationRulesEngineSettings() {
    this( false );
  }

  public AuthorizationRulesEngineSettings( boolean ignoresRuleErrors ) {
    this.ignoresRuleErrors = ignoresRuleErrors;
  }

  public static AuthorizationRulesEngineSettings getDefault() {
    return DEFAULT;
  }

  public boolean getIgnoresRuleErrors() {
    return ignoresRuleErrors;
  }
}
