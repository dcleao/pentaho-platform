package org.pentaho.platform.api.engine.security.authorization.rulesng;

public class AuthorizationEvaluationContractException extends AuthorizationEvaluationException {
  public AuthorizationEvaluationContractException( String message ) {
    super( message );
  }

  public AuthorizationEvaluationContractException( Throwable cause ) {
    super( cause );
  }

  public AuthorizationEvaluationContractException( String message, Throwable cause ) {
    super( message, cause );
  }
}
