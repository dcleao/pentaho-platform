package org.pentaho.platform.api.engine.security.authorization.authng;

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

  // Type name?
  // - org.pentaho.rbac-persistence
  // - org.pentaho.resource-acl-persistence
  // - org.pentaho.anyOf
  // - org.pentaho.allOf
  // - org.pentaho.not
  /*
    anyOf
      - admin.universal
      - allOf
        - resource-self-restriction
        - anyOf
          - administer-security-derived
          -
  */
  String getCode();
  String getDescription();
}
