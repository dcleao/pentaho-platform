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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@code AuthorizationEvaluationResult} class encapsulates the result of an authorization evaluation. It contains
 * information about whether the authorization was granted or denied, as well as, optionally, the reasons for the
 * decision.
 * <p>
 * Whenever necessary to express a vote of abstention, an empty {@link Optional <AuthorizationEvaluationResult>}
 * instance should be used
 *
 * @see AuthorizationEvaluationOptions#getIncludesReasons()
 */
public class AuthorizationEvaluationResult {
  private static final AuthorizationEvaluationResult DENIED_BY_DEFAULT = deny( List.of() );
  private static final AuthorizationEvaluationResult GRANTED_BY_DEFAULT = grant( List.of() );

  private final boolean granted;

  @NonNull
  private final List<AuthorizationEvaluationReason> reasons;

  public AuthorizationEvaluationResult( boolean granted ) {
    this( granted, List.of() );
  }

  public AuthorizationEvaluationResult( boolean granted, @NonNull List<AuthorizationEvaluationReason> reasons ) {
    this.granted = granted;
    this.reasons = List.copyOf( Objects.requireNonNull( reasons ) );
  }

  /**
   * Indicates whether the authorization was granted.
   *
   * @return {@code true} if the authorization was granted; {@code false} if it was denied.
   * @see #isDenied()
   */
  public boolean isGranted() {
    return granted;
  }

  /**
   * Indicates whether the authorization was denied.
   *
   * @return {@code true} if the authorization was denied; {@code false} if it was granted.
   * @see #isGranted()
   */
  public boolean isDenied() {
    return !granted;
  }

  /**
   * Gets the reasons for the authorization evaluation result.
   *
   * @return A non-null list of reasons for the authorization evaluation result. The list may be empty if no reasons
   * were provided, or if the evaluation option's {@link AuthorizationEvaluationOptions#getIncludesReasons()} option
   * was specified {@code false}.
   */
  @NonNull
  public List<AuthorizationEvaluationReason> getReasons() {
    return reasons;
  }

  /**
   * Creates an instance of {@code AuthorizationEvaluationResult} having the same granted value, but with
   * additional reasons.
   * <p>
   * If the specified additional reasons are empty, this result instance is returned unchanged.
   *
   * @param additionalReasons The additional reasons to be added to the existing reasons.
   * @return An instance of authorization evaluation result with the additional reasons.
   */
  public AuthorizationEvaluationResult addReasons(
    @NonNull List<AuthorizationEvaluationReason> additionalReasons ) {

    Objects.requireNonNull( additionalReasons );

    return additionalReasons.isEmpty()
      ? this
      : new AuthorizationEvaluationResult( granted, combineReasons( reasons, additionalReasons ) );

  }

  // Used for debugging and logging purposes.
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder
      .append( "AuthorizationEvaluationResult{" )
      .append( "granted=" )
      .append( granted )
      .append( ", reasons=[" );

    for ( var reason : reasons ) {
      builder.append( reason.toString() );
    }

    builder.append( "]" );

    return builder.toString();
  }

  // region static interface
  @NonNull
  public static AuthorizationEvaluationResult grant() {
    return GRANTED_BY_DEFAULT;
  }

  @NonNull
  public static AuthorizationEvaluationResult grant( @NonNull String reasonCode, @NonNull String reasonDescription ) {
    return grant( new AuthorizationEvaluationReason( reasonCode, reasonDescription ) );
  }

  @NonNull
  public static AuthorizationEvaluationResult grant( @NonNull AuthorizationEvaluationReason reason ) {
    return grant( List.of( reason ) );
  }

  @NonNull
  public static AuthorizationEvaluationResult grant( @NonNull List<AuthorizationEvaluationReason> reasons ) {
    return new AuthorizationEvaluationResult( true, reasons );
  }


  @NonNull
  public static AuthorizationEvaluationResult deny() {
    return DENIED_BY_DEFAULT;
  }

  @NonNull
  public static AuthorizationEvaluationResult deny( @NonNull String reasonCode, @NonNull String reasonDescription ) {
    return deny( new AuthorizationEvaluationReason( reasonCode, reasonDescription ) );
  }

  @NonNull
  public static AuthorizationEvaluationResult deny( @NonNull AuthorizationEvaluationReason reason ) {
    return deny( List.of( reason ) );
  }

  @NonNull
  public static AuthorizationEvaluationResult deny( @NonNull List<AuthorizationEvaluationReason> reasons ) {
    return new AuthorizationEvaluationResult( false, reasons );
  }

  /**
   * Combines two authorization evaluation results into a single result.
   * <p>
   * If both results are granted or both are denied, the combined result will have the same decision and the reasons
   * will be combined. If one result is granted and the other is denied, the denied result will be returned.
   *
   * @param first  The first authorization evaluation result.
   * @param second The second authorization evaluation result.
   * @return A new {@link AuthorizationEvaluationResult} that combines the two results.
   */
  public static AuthorizationEvaluationResult combine(
    @NonNull AuthorizationEvaluationResult first,
    @NonNull AuthorizationEvaluationResult second ) {

    Objects.requireNonNull( first );
    Objects.requireNonNull( second );

    // Same decision, granted or denied.
    if ( first.isGranted() == second.isGranted() ) {
      return new AuthorizationEvaluationResult(
        first.isGranted(),
        combineReasons( first.getReasons(), second.getReasons() ) );
    }

    // Different decisions, one granted and one denied.
    // Denied is stronger than granted, so we return denied result.
    return first.isDenied() ? first : second;
  }

  private static List<AuthorizationEvaluationReason> combineReasons(
    @NonNull List<AuthorizationEvaluationReason> first,
    @NonNull List<AuthorizationEvaluationReason> second ) {

    return Stream.concat( first.stream(), second.stream() ).collect( Collectors.toList() );
  }
  // endregion
}
