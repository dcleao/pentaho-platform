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

package org.pentaho.platform.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationRulesManager;
import org.pentaho.platform.engine.security.authorization.rulesng.AbstractAuthorizationRulesEngine;
import org.pentaho.platform.api.engine.security.authorization.rulesng.AuthorizationRulesEngineSettings;

import java.util.List;

public class PentahoAuthorizationRulesEngine extends AbstractAuthorizationRulesEngine {

  public PentahoAuthorizationRulesEngine(
    @NonNull IAuthorizationRulesManager rulesManager,
    @NonNull AuthorizationRulesEngineSettings settings ) {
    super( rulesManager, settings );
  }

  public PentahoAuthorizationRulesEngine( @NonNull IAuthorizationRulesManager rulesManager ) {
    super( rulesManager );
  }

  public PentahoAuthorizationRulesEngine( @NonNull List<IAuthorizationRule> orRules,
                                          @NonNull AuthorizationRulesEngineSettings settings ) {
    super( orRules, settings );
  }

  public PentahoAuthorizationRulesEngine( @NonNull List<IAuthorizationRule> orRules ) {
    super( orRules );
  }
}
