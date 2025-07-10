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
import org.pentaho.platform.api.engine.security.authorization.rulesng.IAuthorizationUser;

import java.util.Map;
import java.util.Set;

public class PentahoAuthorizationUser implements IAuthorizationUser {
  @NonNull
  @Override
  public String getName() {
    return "";
  }

  @NonNull
  @Override
  public Map<String, Object> getAttributes() {
    return Map.of();
  }

  @NonNull
  @Override
  public Set<String> getRoles() {
    return Set.of();
  }

  @Override
  public String toString() {
    return getName();
  }
}
