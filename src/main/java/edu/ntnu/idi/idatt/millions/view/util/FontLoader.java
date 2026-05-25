package edu.ntnu.idi.idatt.millions.view.util;

import javafx.scene.text.Font;

/**
 * Utility that loads the bundled Inter font family at application startup so the CSS declaration
 * {@code -fx-font-family: "Inter"} resolves correctly across platforms.
 *
 * <p>Missing font files are silently ignored — JavaFX falls back to the next font in the CSS chain
 * when a weight cannot be loaded.
 *
 * <p>This class cannot be instantiated; use the static method directly.
 */
public final class FontLoader {

  private static final String[] WEIGHTS = {"Regular", "Medium", "SemiBold", "Bold"};
  private static final String FONT_PATH = "/fonts/Inter-%s.ttf";
  private static final int LOAD_SIZE = 14;

  private FontLoader() {}

  /**
   * Loads every bundled Inter weight into the JavaFX font registry. Safe to call multiple times —
   * JavaFX deduplicates fonts internally.
   */
  public static void loadInter() {
    for (String weight : WEIGHTS) {
      var stream = FontLoader.class.getResourceAsStream(String.format(FONT_PATH, weight));
      if (stream != null) {
        Font.loadFont(stream, LOAD_SIZE);
      }
    }
  }
}
