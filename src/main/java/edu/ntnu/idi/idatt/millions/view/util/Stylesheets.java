package edu.ntnu.idi.idatt.millions.view.util;

import java.util.Objects;

/**
 * <p>Utility class for resolving classpath stylesheet URLs.</p>
 *
 * <p>Centralises the boilerplate of converting a resource path to the
 * external form required by {@link javafx.scene.Scene#getStylesheets()},
 * so that each call site is a single, readable expression.</p>
 */
public final class Stylesheets {

  private Stylesheets() {}

  /**
   * <p>Resolves a classpath stylesheet path to the external URL string
   * expected by JavaFX.</p>
   *
   * @param path the absolute classpath path, e.g. {@code "/styles/main.css"}
   * @return the external form URL string
   * @throws NullPointerException if the resource cannot be found on the classpath
   */
  public static String load(String path) {
    return Objects.requireNonNull(
        Stylesheets.class.getResource(path),
        "Stylesheet not found on classpath: " + path
    ).toExternalForm();
  }
}
