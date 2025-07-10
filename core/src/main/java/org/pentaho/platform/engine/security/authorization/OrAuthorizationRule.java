package org.pentaho.platform.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationEvaluationException;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationEvaluationResult;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationEvaluationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrAuthorizationRule implements IAuthorizationRule {
  @NonNull
  private final List<IAuthorizationRule> rules;

  public OrAuthorizationRule( @NonNull List<IAuthorizationRule> rules ) {
    this.rules = List.copyOf( Objects.requireNonNull( rules ) );
  }

  @NonNull @Override
  public Optional<AuthorizationEvaluationResult> evaluate( @NonNull IAuthorizationUser user, @NonNull String actionName,
                                                           @NonNull IAuthorizationEvaluationContext context )
    throws AuthorizationEvaluationException {
    return Optional.empty();
  }
}
