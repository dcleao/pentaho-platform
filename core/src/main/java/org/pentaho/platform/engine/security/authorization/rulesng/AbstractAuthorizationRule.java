package org.pentaho.platform.engine.security.authorization.rulesng;

import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationRule;

/**
 * The {@code AbstractAuthorizationRule} class is an optional base class for implementing authorization rules.
 * <p>
 * It provides a default implementation of the {@link #toString()} method, which returns the fully qualified class name.
 */
public abstract class AbstractAuthorizationRule implements IAuthorizationRule {

  @Override
  public String toString() {
    return getClass().getTypeName();
  }
}
