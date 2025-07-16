package org.pentaho.platform.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;
import org.pentaho.platform.engine.security.authorization.authng.AuthorizationEngine;

public class PentahoAuthorizationEngine extends AuthorizationEngine {

  public PentahoAuthorizationEngine( @NonNull IAuthorizationRule rootRule ) {
    super( rootRule );
  }

  // TODO: Implement the hard-coded custom rule for resource-based authorization, as a validation/throw?
}
