package edu.ntnu.idi.idatt.millions.view.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Taskbar;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * Utility for applying the application icon to a {@link Stage} and the OS taskbar/dock.
 *
 * <p>The icon is clipped to a rounded rectangle that matches the macOS squircle proportions.
 * Taskbar support is checked at runtime — missing support is silently ignored, so this utility is
 * safe to call on all platforms.
 *
 * <p>This class cannot be instantiated; use {@link #apply(Stage)} directly.
 */
public final class AppIcon {

  private static final String ICON_PATH = "/images/icon.png";
  private static final int ICON_SIZE = 1024;
  private static final double ARC_RATIO = 0.35;
  private static final double PAD_RATIO = 0.08;

  private AppIcon() {}

  /**
   * Loads the bundled icon, clips it to a rounded rectangle, and applies it to both the given
   * {@link Stage} and the OS taskbar/dock (if supported).
   *
   * @param stage the primary stage to set the window icon on
   */
  public static void apply(Stage stage) {
    var stream = AppIcon.class.getResourceAsStream(ICON_PATH);
    if (stream != null) {
      stage.getIcons().add(new Image(stream));
    }
    applyTaskbarIcon();
  }

  private static void applyTaskbarIcon() {
    if (!Taskbar.isTaskbarSupported()) {
      return;
    }
    Taskbar taskbar = Taskbar.getTaskbar();
    if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
      return;
    }
    try {
      BufferedImage src = ImageIO.read(AppIcon.class.getResource(ICON_PATH));
      taskbar.setIconImage(roundedIcon(src));
    } catch (Exception ignored) {
    }
  }

  private static BufferedImage roundedIcon(BufferedImage src) {
    int arc = (int) (ICON_SIZE * ARC_RATIO);
    int pad = (int) (ICON_SIZE * PAD_RATIO);
    BufferedImage out = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = out.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setClip(
        new RoundRectangle2D.Float(pad, pad, ICON_SIZE - pad * 2, ICON_SIZE - pad * 2, arc, arc));
    g.drawImage(src, pad, pad, ICON_SIZE - pad * 2, ICON_SIZE - pad * 2, null);
    g.dispose();
    return out;
  }
}
