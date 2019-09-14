/** This code is licenced under the GPL version 2. */
package pcap.common.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import pcap.common.annotation.Inclubating;
import pcap.common.logging.Logger;
import pcap.common.logging.LoggerFactory;

/** @author <a href="mailto:contact@ardikars.com">Ardika Rommy Sanjaya</a> */
@Inclubating
public final class Properties {

  private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

  /**
   * Returns the value of the Java system property with the specified {@code key}, while falling
   * back to the specified default value if the property access fails.
   *
   * @param key key.
   * @return returns the property value. null if there's no such property or if an access to the
   *     specified property is not allowed.
   * @since 1.2.3
   */
  public static String getProperty(final String key) {
    return getProperty(key, null);
  }

  /**
   * Returns the value of the Java system property with the specified {@code key}, while falling
   * back to the specified default value if the property access fails.
   *
   * @param key key.
   * @param defaultValue default value.
   * @return the property value. {@code defaultValue} if there's no such property or if an access to
   *     the specified property is not allowed.
   * @since 1.2.3
   */
  public static String getProperty(final String key, String defaultValue) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Property key should be not null");
    }
    String value = null;
    try {
      if (System.getSecurityManager() == null) {
        value = System.getProperty(key);
      } else {
        value =
            AccessController.doPrivileged(
                new PrivilegedAction<String>() {
                  @Override
                  public String run() {
                    return System.getProperty(key);
                  }
                });
      }
    } catch (SecurityException e) {
      LOGGER.warn(
          "Unable to retrieve a system property '{}'; default values will be used: {}",
          key,
          e.getMessage());
    }
    if (value != null) {
      return value;
    }
    return defaultValue;
  }

  /**
   * Returns the value of the Java system property with the specified {@code key}, while falling
   * back to the specified default value if the property access fails.
   *
   * @param key key.
   * @param defaultValue default value.
   * @return the property value. {@code defaultValue} if there's no such property or if an access to
   *     the specified property is not allowed.
   * @since 1.2.6
   */
  public static boolean getBoolean(final String key, boolean defaultValue) {
    String value = getProperty(key);
    if (value == null) {
      return defaultValue;
    }
    value = value.trim().toLowerCase();
    if (value.isEmpty()) {
      return defaultValue;
    }

    if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
      return true;
    }

    if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
      return false;
    }
    return defaultValue;
  }

  /**
   * Returns the value of the Java system property with the specified {@code key}, while falling
   * back to the specified default value if the property access fails.
   *
   * @param key key.
   * @param defaultValue default value.
   * @return the property value. {@code defaultValue} if there's no such property or if an access to
   *     the specified property is not allowed.
   * @since 1.2.6
   */
  public static int getInt(String key, int defaultValue) {
    String value = getProperty(key);
    if (value == null) {
      return defaultValue;
    }

    value = value.trim();
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      // Ignore
    }
    return defaultValue;
  }

  /**
   * Returns the value of the Java system property with the specified {@code key}, while falling
   * back to the specified default value if the property access fails.
   *
   * @param key key.
   * @param defaultValue default value.
   * @return the property value. {@code defaultValue} if there's no such property or if an access to
   *     the specified property is not allowed.
   * @since 1.2.6
   */
  public static long getLong(String key, long defaultValue) {
    String value = getProperty(key);
    if (value == null) {
      return defaultValue;
    }

    value = value.trim();
    try {
      return Long.parseLong(value);
    } catch (Exception e) {
      // Ignore
    }
    return defaultValue;
  }
}
