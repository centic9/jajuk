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
 *  $$Revision: 4113 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

public class HateSelectionAction extends SelectionAction {

  private static final long serialVersionUID = 1L;

  /**
   * Set hate preference to a selection
   * <p>
   * Selection action
   * </p>
   */
  HateSelectionAction() {
    super(Messages.getString("Preference.1"), IconLoader.getIcon(JajukIcons.PREFERENCE_HATE), true);
    setShortDescription(Messages.getString("Preference.1"));
  }

  @Override
  public void perform(ActionEvent e) throws Exception {
    super.perform(e);
    // Check selection is not void
    if (selection.size() == 0) {
      return;
    }
    // Extract tracks of each item
    List<Track> tracks = new ArrayList<Track>(selection.size());
    for (Item item : selection) {
      tracks.addAll(TrackManager.getInstance().getAssociatedTracks(item));
    }
    // Set the preference
    for (Track track : tracks) {
      track.setProperty(Const.XML_TRACK_PREFERENCE, -3);
    }
  }
}
