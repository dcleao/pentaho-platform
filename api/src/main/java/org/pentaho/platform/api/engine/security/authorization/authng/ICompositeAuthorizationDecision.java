package org.pentaho.platform.api.engine.security.authorization.authng;

import java.util.List;

public interface ICompositeAuthorizationDecision extends IAuthorizationDecision {
  List<IAuthorizationDecision> getDecisions();
}
