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

package org.pentaho.platform.api.engine.security.authorization.rulesng;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The {@code AuthorizationEvaluationCycleException} class is thrown when a cycle is detected during the authorization
 * evaluation process. This indicates that the evaluation logic has entered an infinite loop or recursive cycle, which
 * should not happen in a well-configured authorization system. A cycle occurs when an authorization rule evaluates
 * the permission for a user to perform an action, using the
 * {@link IAuthorizationEvaluationContext#evaluate(IAuthorizationUser, String)} method, for a combination of user and
 * action which
 * is already being evaluated in the current evaluation process.
 */
public class AuthorizationEvaluationCycleException extends AuthorizationEvaluationException {
  @NonNull
  private final Collection<AuthorizationEvaluationRequest> pathRequests;

  @NonNull
  private final AuthorizationEvaluationRequest cycleRequest;

  public AuthorizationEvaluationCycleException(
    @NonNull Collection<AuthorizationEvaluationRequest> pathRequests,
    @NonNull AuthorizationEvaluationRequest cycleRequest ) {

    super( createMessage( pathRequests, cycleRequest ) );

    this.pathRequests = pathRequests;
    this.cycleRequest = cycleRequest;
  }

  @NonNull
  public Collection<AuthorizationEvaluationRequest> getPathRequests() {
    return pathRequests;
  }

  @NonNull
  public AuthorizationEvaluationRequest getCycleRequest() {
    return cycleRequest;
  }

  private static String createMessage( @NonNull Collection<AuthorizationEvaluationRequest> pathRequests,
                                       @NonNull AuthorizationEvaluationRequest request ) {
    Objects.requireNonNull( pathRequests );
    Objects.requireNonNull( request );

    // TODO: ideally, this would include a description of the rule in each step...

    StringBuilder builder = new StringBuilder();
    builder
      .append( "Authorization evaluation cycle detected for request " )
      .append( request )
      .append( ".\n" )
      .append( "Evaluation path contains the following preceding requests:\n" );

    List<AuthorizationEvaluationRequest> pathRequestsReversed = new ArrayList<>( pathRequests );
    Collections.reverse( pathRequestsReversed );

    int position = pathRequestsReversed.size();
    for ( AuthorizationEvaluationRequest stepRequest : pathRequestsReversed ) {
      builder.append( position );
      builder.append( ": " );
      builder.append( stepRequest );
      builder.append( "\n" );

      position--;
    }

    return builder.toString();
  }
}
