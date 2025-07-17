package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

import java.util.Objects;

// region Standard decision types' implementations
public class AbstractAuthorizationDecision implements IAuthorizationDecision {

  private static final String GRANTED_DESCRIPTION =
    Messages.getInstance().getString( "AuthorizationDecision.GRANTED" );
  private static final String DENIED_DESCRIPTION = Messages.getInstance().getString( "AuthorizationDecision.DENIED" );

  @NonNull
  private final AuthorizationRequest request;
  private final boolean granted;

  public AbstractAuthorizationDecision( @NonNull AuthorizationRequest request, boolean granted ) {
    this.request = Objects.requireNonNull( request );
    this.granted = granted;
  }

  @NonNull
  @Override
  public AuthorizationRequest getRequest() {
    return request;
  }

  @Override
  public boolean isGranted() {
    return granted;
  }

  protected String getGrantedText() {
    return isGranted() ? GRANTED_DESCRIPTION : DENIED_DESCRIPTION;
  }

  @Override
  public String toString() {
    // Example: "Granted"
    return getGrantedText();
  }
}
