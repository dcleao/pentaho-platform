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

import java.util.List;

/**
 * The {@code IAuthorizationRuleManager} interface extends the {@link IAuthorizationRule} interface
 * to add the responsibility of evaluating multiple authorization rules, and combining their results.
 */
public interface IAuthorizationRulesManager extends IAuthorizationRule {
  List<IAuthorizationRule> getRules();
}
