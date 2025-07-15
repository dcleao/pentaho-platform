package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;

import java.util.LinkedHashSet;
import java.util.Set;

public class AnyAuthorizationDecision extends AbstractCompositeAuthorizationDecision {

  public AnyAuthorizationDecision( boolean granted, @NonNull Set<IAuthorizationDecision> decisions ) {
    super( granted, decisions );
  }

  @Override
  public String getDescription() {
    // TODO: implement a description for the ANY decision
    return "";
  }

  public static class Builder extends AbstractCompositeAuthorizationDecisionBuilder<Builder> {
    @NonNull
    @Override
    protected IAuthorizationDecision buildDecisionCore() {
      return new AnyAuthorizationDecision( isGranted(),  new LinkedHashSet<>( getDecisions() ) );
    }
  }
}
