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

package org.pentaho.platform.api.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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
  private static final AuthorizationEvaluationResult DENIED_BY_DEFAULT_RESULT =
    new AuthorizationEvaluationResult(
      false,
      List.of( new AuthorizationEvaluationReason(
        "org.pentaho.authorization.reason.denied-by-default",
        "Denied by default" ) ) );

  private final boolean granted;

  @NonNull
  private final List<AuthorizationEvaluationReason> reasons;

  public AuthorizationEvaluationResult( boolean granted ) {
    this( granted, List.of() );
  }

  public AuthorizationEvaluationResult( boolean granted, @NonNull List<AuthorizationEvaluationReason> reasons ) {
    this.granted = granted;
    this.reasons = Objects.requireNonNull( reasons );
  }

  /**
   * Indicates whether the authorization was granted.
   *
   * @return {@code true} if the authorization was granted; {@code false} if it was denied.
   */
  public boolean isGranted() {
    return granted;
  }

  /**
   * Indicates whether the authorization was denied.
   *
   * @return {@code true} if the authorization was denied; {@code false} if it was granted.
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

  // region static interface

  // Default result, when no rules, or all rules abstain.
  @NonNull
  public static AuthorizationEvaluationResult getDeniedByDefaultResult() {
    return DENIED_BY_DEFAULT_RESULT;
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
        combine( first.getReasons(), second.getReasons() ) );
    }

    // Different decisions, one granted and one denied.
    // Denied is stronger than granted, so we return denied result.
    return first.isDenied() ? first : second;
  }

  private static List<AuthorizationEvaluationReason> combine(
    @NonNull List<AuthorizationEvaluationReason> first,
    @NonNull List<AuthorizationEvaluationReason> second ) {

    return Stream.concat( first.stream(), second.stream() ).collect( Collectors.toList() );
  }
  // endregion
}
