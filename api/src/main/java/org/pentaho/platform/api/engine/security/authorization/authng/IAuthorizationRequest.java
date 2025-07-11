package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.springframework.security.core.Authentication;

/**
 * The {@code IAuthorizationRequest} interface represents an authorization request for a user to perform an action.
 * <p>
 * The user is identified by the {@link Authentication} object, which contains the user's username and authorities.
 * <p>
 * This type can be derived to include any additional information characterizing an authorization request.
 * <p>
 * Equality is based on the authentication, action and any other key properties, meaning two requests are considered
 * equal if they have the same authentication, action and value of the other key properties.
 * The {@link Object#equals(Object)} and {@link Object#hashCode()} methods must be overridden to ensure this behavior.
 * <p>
 * The string representation of this request should be appropriate for logging and debugging purposes.
 */
public interface IAuthorizationRequest {
  @NonNull
  Authentication getAuthentication();

  @NonNull
  IAuthorizationAction getAction();
}
