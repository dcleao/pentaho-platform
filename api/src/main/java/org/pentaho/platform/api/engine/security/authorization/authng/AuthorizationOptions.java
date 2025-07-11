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

package org.pentaho.platform.api.engine.security.authorization.authng;

/**
 * The {@code AuthorizationOptions} class encapsulates options that control the authorization evaluation process.
 * <p>
 * The authorization options do not change the final grant/deny decision, but can affect extra or auxiliary behaviors
 * of the evaluation process.
 */

public class AuthorizationOptions {

  private static final AuthorizationOptions DEFAULT = new AuthorizationOptions();

  public static AuthorizationOptions getDefault() {
    return DEFAULT;
  }

  private final boolean includesAllDecisions;

  /**
   * Constructs an {@code AuthorizationOptions} instance with default settings.
   * By default, it does not include all decisions in the authorization result.
   */
  public AuthorizationOptions() {
    this( false );
  }

  public AuthorizationOptions( boolean includesAllDecisions ) {
    this.includesAllDecisions = includesAllDecisions;
  }

  /**
   * Indicates whether the authorization result should include all decisions, even those that would not change the
   * final granted or denied status of the decision.
   *
   * @return {@code true} if all decisions should be included; {@code false}, otherwise.
   */
  public boolean getIncludesAllDecisions() {
    return includesAllDecisions;
  }
}
