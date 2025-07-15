package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

/**
 * The {@code IAuthorizationDecision} interface represents the result of an authorization process.
 * <p>
 * The {@link Object#toString()} method should provide a description of the decision that is suitable for logging and
 * debugging purposes. For example:
 * <pre><code>
 * public String toString() {
 *   return String.format(
 *     "%s [granted=`%s`]",
 *     getClass().getSimpleName(),
 *     isGranted() );
 * }
 * </code></pre>
 */
public interface IAuthorizationDecision {
  /**
   * Indicates whether the authorization was granted.
   *
   * @return {@code true} if the authorization was granted; {@code false} if it was denied.
   * @see #isDenied()
   */
  boolean isGranted();

  /**
   * Indicates whether the authorization was denied.
   * <p>
   * This method is just a convenience for {@code !isGranted()}.
   *
   * @return {@code true} if the authorization was denied; {@code false} if it was granted.
   * @see #isGranted()
   */
  default boolean isDenied() {
    return !isGranted();
  }

  /**
   * Gets a human-readable description of the authorization decision.
   * <p>
   * Should be localized in the current (thread's) system locale, regardless of the user for which the authorization
   * was evaluated.
   *
   * @return The description of the decision.
   */
  String getDescription();
}
