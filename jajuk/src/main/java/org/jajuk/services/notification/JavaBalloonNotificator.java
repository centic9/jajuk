/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
 *  http://jajuk.info
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision: 3132 $
 */
package org.jajuk.services.notification;

import java.awt.TrayIcon;

import org.jajuk.base.File;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.windows.JajukSystray;
import org.jajuk.util.Messages;

/**
 * Implementation of @link INotificator which uses the standard Java Systray for
 * displaying notifications to the user. *
 * <p>
 * Singleton
 * </p>
 * 
 */
public class JavaBalloonNotificator implements INotificator {
  // the Systray is used to display the notification
  TrayIcon trayIcon;

  /** Self instance **/
  private static JavaBalloonNotificator self;

  /**
   * 
   * Return an instance of this singleton
   * 
   * @return an instance of this singleton
   */
  public static JavaBalloonNotificator getInstance() {
    if (self == null) {
      self = new JavaBalloonNotificator();
    }
    return self;
  }

  /**
   * Creates an instance, the link to tray provides the necessary Java Systray
   * implementation.
   * 
   * @param tray
   *          The initialized system tray. isAvailable() will return false, if
   *          this is passed null.
   */
  private JavaBalloonNotificator() {
    this.trayIcon = JajukSystray.getInstance().getTrayIcon();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#isAvailable()
   */
  @Override
  public boolean isAvailable() {
    return (trayIcon != null);
  }

  @Override
  public void notify(WebRadio webradio) {
    String title = Messages.getString("Notificator.track_change.webradio_title");
    String text = webradio.getName();
    // simply call the display method on the tray icon that is provided
    trayIcon.displayMessage(title, text, TrayIcon.MessageType.INFO);
  }

  @Override
  public void notify(File file) {
    String title = Messages.getString("Notificator.track_change.track_title");
    String text = file.buildTitle();
    // simply call the display method on the tray icon that is provided
    trayIcon.displayMessage(title, text, TrayIcon.MessageType.INFO);
  }

}