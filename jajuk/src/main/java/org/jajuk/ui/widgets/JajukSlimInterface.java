/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 * $Revision: 3612 $
 */
package org.jajuk.ui.widgets;

import static org.jajuk.ui.actions.JajukAction.FAST_FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukAction.MUTE_STATE;
import static org.jajuk.ui.actions.JajukAction.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukAction.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukAction.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.actions.JajukAction.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukAction.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukAction.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukAction.STOP_TRACK;
import ext.JScrollingText;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.Timer;

import org.jajuk.Main;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

/**
 * Jajuk Slim Interface
 */
public class JajukSlimInterface extends JFrame implements ITechnicalStrings, Observer,
    MouseWheelListener {

  private static final long serialVersionUID = 1L;

  JButton jbPrevious;

  JButton jbNext;

  JPressButton jbRew;

  JButton jbPlayPause;

  JButton jbStop;
  
  JPressButton jbFwd;

  JButton jbBestof;
  
  JButton jbNovelties;
  
  JButton jbRandom;

  JajukToggleButton jbVolume;

  JScrollingText scrollingText;

  JPanel slimJajuk;

  /** Swing Timer to refresh the component */
  private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      update(new Event(EventSubject.EVENT_HEART_BEAT));
    }
  });

  public JajukSlimInterface() {
    JToolBar jtbPlay = new JToolBar();
    jtbPlay.setBorder(null);
    //jtbPlay.setFloatable(false);
    jtbPlay.setRollover(true);
    ActionUtil.installKeystrokes(jtbPlay, ActionManager.getAction(NEXT_ALBUM), ActionManager
        .getAction(PREVIOUS_ALBUM));
    jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
    jbPrevious.setIcon(IconLoader.ICON_PREVIOUS);
    jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
    jbNext.setIcon(IconLoader.ICON_NEXT);
    jbRew = new JPressButton(ActionManager.getAction(REWIND_TRACK));
    jbRew.setIcon(IconLoader.ICON_REW_16x16);
    jbPlayPause = new JajukButton(ActionManager.getAction(PLAY_PAUSE_TRACK));
    jbPlayPause.setIcon(IconLoader.ICON_PAUSE_16x16);
    jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));
    jbStop.setIcon(IconLoader.ICON_STOP_16x16);
    jbFwd = new JPressButton(ActionManager.getAction(FAST_FORWARD_TRACK));
    jbFwd.setIcon(IconLoader.ICON_FWD_16x16);
    
    jtbPlay.add(jbPrevious);
    jtbPlay.add(jbRew);
    jtbPlay.add(jbPlayPause);
    jtbPlay.add(jbStop);
    jtbPlay.add(jbFwd);
    jtbPlay.add(jbNext);
        
    JToolBar jtbTools = new JToolBar();
    jtbTools.setBorder(null);
    jtbTools.addSeparator();
    
    jbBestof = new JajukButton(ActionManager.getAction(JajukAction.BEST_OF));
    jbBestof.setIcon(IconLoader.ICON_BESTOF_16x16);
    jbNovelties = new JajukButton(ActionManager.getAction(JajukAction.NOVELTIES));
    jbNovelties.setIcon(IconLoader.ICON_NOVELTIES_16x16);
    jbRandom = new JajukButton(ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL));
    jbRandom.setIcon(IconLoader.ICON_SHUFFLE_GLOBAL_16x16);
    
    // Volume Icon
    int iVolume = (int) (100 * ConfigurationManager.getFloat(CONF_VOLUME));
    if (iVolume > 100) { // can occur in some undefined cases
      iVolume = 100;
    }
    jbVolume = new JajukToggleButton(ActionManager.getAction(MUTE_STATE));
    jbVolume.addMouseWheelListener(this);
    setVolumeIcon(iVolume);

    jtbTools.add(jbBestof);
    jtbTools.add(jbNovelties);
    jtbTools.add(jbRandom);
    jtbTools.addSeparator();
    jtbTools.add(jbVolume);
        
    JToolBar jtbText = new JToolBar();
    jtbText.setBorder(null);
    jtbText.addSeparator();

    scrollingText = new JScrollingText(getPlayerInfo(), -3);
    scrollingText.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
    scrollingText.setPreferredSize(new Dimension(200, 15));
    scrollingText.start();

    jtbText.add(scrollingText);
    
    slimJajuk = new JPanel();
    slimJajuk.setLayout(new BorderLayout());

    slimJajuk.add(jtbPlay, BorderLayout.WEST);
    slimJajuk.add(jtbText, BorderLayout.CENTER);
    slimJajuk.add(jtbTools, BorderLayout.EAST);
    
    slimJajuk.setBorder(BorderFactory.createRaisedBevelBorder());

    add(slimJajuk);
    ObservationManager.register(this);

    setUndecorated(true);
    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    setVisible(true);
    pack();
  }

  /**
   * @return Player Info : current and next track
   */
  public String getPlayerInfo() {
    try{
      String currentTrack = Main.getWindow().buildTitle(FIFO.getInstance().getCurrentFile());
      String nextTrack = "";
      try {
        nextTrack = Main.getWindow().buildTitle(
            FIFO.getInstance().getItem(FIFO.getInstance().getIndex() + 1).getFile());
      } catch (Exception e) {
        nextTrack = Main.getWindow().buildTitle(FIFO.getInstance().getPlanned().get(0).getFile());
      }
      return "  |  Playing: " + currentTrack + "  |  Next: " + nextTrack;
    }catch (Exception e){
      return Messages.getString("JajukWindow.17");
    }
  }

  /**
   * Set Volume Icon
   */
  public void setVolumeIcon(final float fVolume) {
    if (fVolume <= 0) {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/mute_16x16.png"));
      jbVolume.setIcon(icon);
    } else if (fVolume <= 25) {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/volume1.png"));
      jbVolume.setIcon(icon);
    } else if (fVolume <= 50) {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/volume2.png"));
      jbVolume.setIcon(icon);
    } else if (fVolume <= 75) {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/volume3.png"));
      jbVolume.setIcon(icon);
    } else {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/volume4.png"));
      jbVolume.setIcon(icon);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource() == jbVolume) {
      int oldVolume = (int) (100 * Player.getCurrentVolume());
      int newVolume = oldVolume - (e.getUnitsToScroll() * 3);
      jbVolume.removeMouseWheelListener(this);
      // if user move the volume slider, unmute
      if (Player.isMuted()) {
        Player.mute(false);
      }

      if (newVolume > 100)
        newVolume = 100;
      else if (newVolume < 0)
        newVolume = 0;

      Player.setVolume((float) newVolume / 100);
      jbVolume.addMouseWheelListener(this);
      jbVolume.setToolTipText(newVolume + " %");
      setVolumeIcon(newVolume);
    }
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_WEBRADIO_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_PAUSE);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_RESUME);
    eventSubjectSet.add(EventSubject.EVENT_QUEUE_NEED_REFRESH);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
    return eventSubjectSet;
  }

  public void update(final Event event) {
    EventSubject subject = event.getSubject();
    if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)) {
      scrollingText.setText(getPlayerInfo());
    }else if (EventSubject.EVENT_PLAYER_STOP.equals(subject)) {
      scrollingText.setText(Messages.getString("JajukWindow.17"));
    }else if (EventSubject.EVENT_PLAYER_PAUSE.equals(subject)) {
      jbPlayPause.setIcon(IconLoader.ICON_PLAY_16x16);
    }else if (EventSubject.EVENT_PLAYER_RESUME.equals(subject)) {
      jbPlayPause.setIcon(IconLoader.ICON_PAUSE_16x16);
    }
  }
}