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
 *  $$Revision: 3402 $$
 */

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.PlaylistFile;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.PlayHighlighterPredicate;
import org.jajuk.ui.helpers.PlaylistEditorTransferHandler;
import org.jajuk.ui.helpers.PlaylistTableModel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;

/**
 * Adapter for playlists editors *
 */
public class QueueView extends PlaylistView {

  private static final long serialVersionUID = -2851288035506442507L;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    plf = new PlaylistFile(PlaylistFile.PLAYLIST_TYPE_QUEUE, null, null, null);
    // Control panel
    jpEditorControl = new JPanel();
    jpEditorControl.setBorder(BorderFactory.createEtchedBorder());
    // Note : we don't use toolbar because it's buggy in Metal look and feel
    // : icon get bigger
    double sizeControl[][] = { { 5, TableLayout.PREFERRED, 15, TableLayout.FILL, 5 }, { 5, 25, 5 } };
    TableLayout layout = new TableLayout(sizeControl);
    layout.setHGap(2);
    jpEditorControl.setLayout(layout);
    jbSave = new JajukButton(IconLoader.ICON_SAVE);
    jbSave.setToolTipText(Messages.getString("AbstractPlaylistEditorView.3"));
    jbSave.addActionListener(this);
    jbRemove = new JajukButton(IconLoader.ICON_REMOVE);
    jbRemove.setToolTipText(Messages.getString("AbstractPlaylistEditorView.5"));
    jbRemove.addActionListener(this);
    jbUp = new JajukButton(IconLoader.ICON_UP);
    jbUp.setToolTipText(Messages.getString("AbstractPlaylistEditorView.6"));
    jbUp.addActionListener(this);
    jbDown = new JajukButton(IconLoader.ICON_DOWN);
    jbDown.setToolTipText(Messages.getString("AbstractPlaylistEditorView.7"));
    jbDown.addActionListener(this);
    jbAddShuffle = new JajukButton(IconLoader.ICON_ADD_SHUFFLE);
    jbAddShuffle.setToolTipText(Messages.getString("AbstractPlaylistEditorView.10"));
    jbAddShuffle.addActionListener(this);
    jlTitle = new JLabel(" [" + FIFO.getInstance().getFIFO().size() + "]");
    JToolBar jtb = new JToolBar();
    jtb.setRollover(true);
    jtb.setBorder(null);

    jtb.add(jbSave);
    jtb.add(jbRemove);
    jtb.add(jbAddShuffle);
    jtb.add(jbUp);
    jtb.add(jbDown);

    jpEditorControl.add(jtb, "1,1");
    jpEditorControl.add(jlTitle, "3,1,r,c");
    editorModel = new PlaylistTableModel(true);
    editorTable = new JajukTable(editorModel, CONF_QUEUE_COLUMNS);
    editorModel.populateModel(editorTable.getColumnsConf());
    editorTable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // multi-row
    // selection
    editorTable.setSortable(false);
    editorTable.setDragEnabled(true);
    editorTable.setTransferHandler(new PlaylistEditorTransferHandler(editorTable));
    setRenderers();
    // just an icon
    editorTable.getColumnModel().getColumn(0).setPreferredWidth(20);
    editorTable.getColumnModel().getColumn(0).setMaxWidth(20);
    editorTable.getTableHeader().setPreferredSize(new Dimension(0, 20));
    editorTable.showColumns(editorTable.getColumnsConf());
    ListSelectionModel lsm = editorTable.getSelectionModel();
    lsm.addListSelectionListener(this);
    double size[][] = { { 0.99 }, { TableLayout.PREFERRED, 0.99 } };
    setLayout(new TableLayout(size));
    add(jpEditorControl, "0,0");
    add(new JScrollPane(editorTable), "0,1");
    // menu items
    jmiFilePlay = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_SELECTION));
    jmiFilePlay.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFilePush = new JMenuItem(ActionManager.getAction(JajukAction.PUSH_SELECTION));
    jmiFilePush.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFileAddFavorites = new JMenuItem(ActionManager.getAction(JajukAction.BOOKMARK_SELECTION));
    jmiFileAddFavorites.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFileProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiFileProperties.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    editorTable.getMenu().add(jmiFilePlay);
    editorTable.getMenu().add(jmiFilePush);
    editorTable.getMenu().add(jmiFileAddFavorites);
    editorTable.getMenu().add(jmiFileProperties);

    ColorHighlighter colorHighlighter = new ColorHighlighter(Color.ORANGE, null,
        new PlayHighlighterPredicate(editorModel));
    Highlighter alternate = Util.getAlternateHighlighter();
    editorTable.setHighlighters(alternate, colorHighlighter);
    // register events
    ObservationManager.register(this);
    // -- force a refresh --
    refreshQueue();
    // Add key listener to enable row suppression using SUPR key
    editorTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        // The fact that a selection can be removed or not is
        // in the jbRemove state
        if (e.getKeyCode() == KeyEvent.VK_DELETE && jbRemove.isEnabled()) {
          removeSelection();
          // Refresh table
          refreshQueue();
        }
      }
    });
    // Add specific behavior on left click
    editorTable.setCommand(new ILaunchCommand() {
      public void launch(int nbClicks) {
        if (nbClicks == 2) {
          // double click, launches selected track and all after
          StackItem item = editorModel.getStackItem(editorTable.getSelectedRow());
          if (item.isPlanned()) {
            // we can't launch a planned
            // track, leave
            item.setPlanned(false);
            item.setRepeat(ConfigurationManager.getBoolean(CONF_STATE_REPEAT));
            item.setUserLaunch(true);
            FIFO.getInstance().push(item,
                ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
          } else { // non planned items
            if (ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK)) {
              FIFO.getInstance().push(item, true);
            } else {
              FIFO.getInstance().goTo(editorTable.getSelectedRow());
              // remove selection for planned tracks
              ListSelectionModel lsm = editorTable.getSelectionModel();
              bSettingSelection = true;
              editorTable.getSelectionModel().removeSelectionInterval(lsm.getMinSelectionIndex(),
                  lsm.getMaxSelectionIndex());
              bSettingSelection = false;
            }
          }
        }
      }
    });
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_QUEUE_NEED_REFRESH);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
    eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_ADD);
    eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("QueueView.0");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(final Event event) {
    SwingUtilities.invokeLater(new Runnable() {
      public synchronized void run() { // NEED TO SYNC to avoid out out
        // bound exceptions
        try {
          EventSubject subject = event.getSubject();
          editorTable.acceptColumnsEvents = false; // flag reloading to avoid wrong
          if (EventSubject.EVENT_QUEUE_NEED_REFRESH.equals(subject)
              || EventSubject.EVENT_DEVICE_REFRESH.equals(subject)
              || EventSubject.EVENT_PLAYER_STOP.equals(subject)) {
            editorModel.alItems.clear();
            editorModel.alPlanned.clear();
            refreshQueue();
          } else if (EventSubject.EVENT_CUSTOM_PROPERTIES_ADD.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) {
              // can be null at view populate
              return;
            }
            // create a new model
            editorModel = new PlaylistTableModel(true);
            editorModel.populateModel(editorTable.getColumnsConf());
            editorTable.setModel(editorModel);
            setRenderers();
            editorTable.addColumnIntoConf((String) properties.get(DETAIL_CONTENT));
            editorTable.showColumns(editorTable.getColumnsConf());
          } else if (EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) { // can be null at view
              // populate
              return;
            }
            editorModel = new PlaylistTableModel(true);
            editorModel.populateModel(editorTable.getColumnsConf());
            editorTable.setModel(editorModel);
            setRenderers();
            // remove item from configuration cols
            editorTable.removeColumnFromConf((String) properties.get(DETAIL_CONTENT));
            editorTable.showColumns(editorTable.getColumnsConf());
          }
        } catch (Exception e) {
          Log.error(e);
        } finally {
          editorTable.acceptColumnsEvents = true; 
          // Update number of tracks remaining
          jlTitle.setText(" [" + FIFO.getInstance().getFIFO().size() + "]");
        }
      }
    });

  }

  private void refreshQueue() {
    // when nothing is selected, set default button state
    if (editorTable.getSelectionModel().getMinSelectionIndex() == -1) {
      setDefaultButtonState();
    }
    editorModel.alItems = FIFO.getInstance().getFIFO();
    editorModel.alPlanned = FIFO.getInstance().getPlanned();
    ((JajukTableModel) editorTable.getModel()).populateModel(editorTable.getColumnsConf());
    int[] rows = editorTable.getSelectedRows();
    // save selection
    editorModel.fireTableDataChanged();// refresh
    bSettingSelection = true;
    for (int i = 0; i < rows.length; i++) {
      // set saved selection after a refresh
      editorTable.getSelectionModel().addSelectionInterval(rows[i], rows[i]);
    }
    bSettingSelection = false;
  }

  /**
   * Set default button state
   * 
   */
  private void setDefaultButtonState() {
    // set buttons
    jbUp.setEnabled(false); // set it to false just for startup
    // because nothing is selected
    jbDown.setEnabled(false); // set it to false just for startup
    // because nothing is selected
    jbAddShuffle.setEnabled(true);// add at the FIFO end by
    // default even with no
    // selection
    jbRemove.setEnabled(false); // set it to false just for startup
    // because cursor is over first track
    // and it can't be removed in queue mode
    // disable prepare party for queue playlist
  }

  public void actionPerformed(ActionEvent ae) {
    try {
      if (ae.getSource() == jbSave) {
        // special playlist, same behavior than a save as
        plf.saveAs();
        // notify playlist repository to refresh
        ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
      } else if (ae.getSource() == jbDown || ae.getSource() == jbUp) {
        int iRow = editorTable.getSelectedRow();
        if (iRow != -1) { // -1 means nothing is selected
          if (ae.getSource() == jbDown) {
            plf.down(iRow);
            if (iRow < editorTable.getModel().getRowCount() - 1) {
              // force immediate table refresh
              refreshQueue();
              editorTable.getSelectionModel().setSelectionInterval(iRow + 1, iRow + 1);
            }
          } else if (ae.getSource() == jbUp) {
            plf.up(iRow);
            if (iRow > 0) {
              // force immediate table refresh
              refreshQueue();
              editorTable.getSelectionModel().setSelectionInterval(iRow - 1, iRow - 1);
            }
          }
        }
      } else if (ae.getSource() == jbRemove) {
        removeSelection();
        refreshQueue();
      } else if (ae.getSource() == jbAddShuffle) {
        int iRow = editorTable.getSelectedRow();
        if (iRow < 0
        // no row is selected, add to the end
            || iRow > FIFO.getInstance().getFIFO().size()) {
          // row can be on planned track if user select a planned track and if
          // fifo is reduced after tracks have been played
          iRow = FIFO.getInstance().getFIFO().size();
        }
        File file = FileManager.getInstance().getShuffleFile();
        try {
          plf.addFile(iRow, file);
          jbRemove.setEnabled(true);
        } catch (JajukException je) {
          Messages.showErrorMessage(je.getCode());
          Log.error(je);
        }
        refreshQueue();
      }
    } catch (Exception e2) {
      Log.error(e2);
    }
  }

  private void removeSelection() {
    int[] iRows = editorTable.getSelectedRows();
    if (iRows.length > 1) {// if multiple selection, remove
      // selection
      editorTable.getSelectionModel().removeIndexInterval(0, editorTable.getRowCount() - 1);
    }
    for (int i = 0; i < iRows.length; i++) {
      // don't forget that index changes when removing
      plf.remove(iRows[i] - i);
    }
    // set selection to last line if end reached
    int iLastRow = editorTable.getRowCount() - 1;
    if (iRows[0] == editorTable.getRowCount()) {
      editorTable.getSelectionModel().setSelectionInterval(iLastRow, iLastRow);
    }
  }

  /**
   * Called when table selection changed
   */
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting() || bSettingSelection) {
      // leave during normal refresh
      return;
    }
    ListSelectionModel selection = (ListSelectionModel) e.getSource();
    if (!selection.isSelectionEmpty()) {
      int selectedRow = selection.getMaxSelectionIndex();
      // true if selected line is a planned track
      boolean bPlanned = false;
      if (selectedRow > editorModel.alItems.size() - 1) {
        // means it is a planned track
        bPlanned = true;
      }
      // -- now analyze each button --
      // Remove button
      if (bPlanned) {// not for planned track
        jbRemove.setEnabled(true);
      } else {
        // check for first row remove case : we can't remove currently
        // played track
        if (selection.getMinSelectionIndex() == 0) {
          // neither for bestof nor novelties playlist
          jbRemove.setEnabled(false);
        } else {
          jbRemove.setEnabled(true);
        }
      }
      // Add shuffle button
      if (// multiple selection not supported
      selection.getMinSelectionIndex() == 0
      // can't add track at current track position
          || selectedRow > FIFO.getInstance().getFIFO().size()
      // no add for planned track but user can add over first planned
      // track to expand FIFO
      ) {
        jbAddShuffle.setEnabled(false);
      } else {
        jbAddShuffle.setEnabled(true);
      }
      // Up button
      if (selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()
      // check if several rows have been selected :
          // doesn't supported yet
          || (FIFO.getInstance().containsRepeat())) {
        // neither for bestof nor novelties playlist
        jbUp.setEnabled(false);
      } else {
        // still here ?
        if (bPlanned) {
          // No up/down buttons for planned tracks
          jbUp.setEnabled(false);
        } else { // normal item
          if (selection.getMinSelectionIndex() == 0 || (selection.getMinSelectionIndex() == 1)) {
            // check if we selected second track just after current
            // tracks
            jbUp.setEnabled(false); // already at the top
          } else {
            jbUp.setEnabled(true);
          }
        }
      }
      // Down button
      if (selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()
      // check if several rows have been selected :
          // doesn't supported yet
          || (FIFO.getInstance().containsRepeat())) {
        jbDown.setEnabled(false);
      } else { // yet here ?
        if (bPlanned) {
          // No up/down buttons for planned tracks
          jbDown.setEnabled(false);
        } else { // normal item
          if (selection.getMaxSelectionIndex() == 0) {
            // current track can't go down
            jbDown.setEnabled(false);
          } else if (selection.getMaxSelectionIndex() < editorModel.alItems.size() - 1) {
            // a normal item can't go in the planned items
            jbDown.setEnabled(true);
          } else {
            jbDown.setEnabled(false);
          }
        }
      }
    }
  }


}