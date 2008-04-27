/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision: 3308 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComponent;

import org.jajuk.base.Playlist;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Action for saving as... an item
 */
public class SaveAsAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  SaveAsAction() {
    super(Messages.getString("PhysicalPlaylistRepositoryView.2"), IconLoader.ICON_SAVE_AS, true);
  }

  /**
   * Invoked when an action occurs.
   * 
   * @param evt
   */
  @SuppressWarnings("unchecked")
  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    //@TODO Do better here, accept a single playlist for ie
    Object o = source.getClientProperty(DETAIL_SELECTION);
    if (o instanceof ArrayList) {
      try {
        ArrayList<Playlist> playlists = (ArrayList<Playlist>)o;
    	  Playlist playlist = playlists.get(0);
    	  playlist.saveAs();
    	  InformationJPanel.getInstance().setMessage(
            Messages.getString("AbstractPlaylistEditorView.22"), InformationJPanel.INFORMATIVE);
        ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
      } catch (JajukException je) {
        Log.error(je);
        Messages.showErrorMessage(je.getCode());
      } catch (Exception ex) {
        Log.error(ex);
      }

    }
  }
}