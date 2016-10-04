package org.tn5250j.gui;

import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import java.awt.*;
import java.util.List;

import static java.lang.Class.forName;

class AppleApplicationTools {

  private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

  boolean tryToSetDockIconImages(List<Image> images) {
    return tryToSetDockIconImage(images.get(images.size() - 1));
  }

  private boolean tryToSetDockIconImage(Image image) {
    if (isAppleEnvironment()) {
      try {
        Class applicationClass = forName("com.apple.eawt.Application");
        Object application = applicationClass.getMethod("getApplication").invoke(applicationClass);
        applicationClass.getMethod("setDockIconImage", Image.class).invoke(application, image);
        return true;
      } catch (Exception e) {
        log.debug("Skipping to set application dock icon for Mac OS X, because didn't found 'com.apple.eawt.Application' class.", e);
      }
    }
    return false;
  }

  private boolean isAppleEnvironment() {
    try {
      forName("com.apple.eawt.Application");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
