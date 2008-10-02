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
 *  $Revision$
 */
package org.jajuk.base;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.jajuk.services.players.FIFO;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * A music file to be played
 * <p>
 * Physical item
 */
public class File extends PhysicalItem implements Comparable<File>, Const {

  private static final long serialVersionUID = 1L;

  /** Parent directory */
  protected final Directory directory;

  /** Associated track */
  protected Track track;

  /** IO file associated with this file */
  private java.io.File fio;

  /**
   * File instanciation
   * 
   * @param sId
   * @param sName
   * @param directory
   * @param track
   * @param lSize
   * @param sQuality
   */
  public File(String sId, String sName, Directory directory, Track track, long lSize, long lQuality) {
    super(sId, sName);
    this.directory = directory;
    setProperty(XML_DIRECTORY, directory.getID());
    this.track = track;
    setProperty(XML_TRACK, track.getID());
    setProperty(XML_SIZE, lSize);
    setProperty(XML_QUALITY, lQuality);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public final String getLabel() {
    return XML_FILE;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "File[ID=" + getID() + " Name={{" + getName() + "}} Dir=" + directory + " Size="
        + getSize() + " Quality=" + getQuality() + "]";
  }

  /**
   * String representation as displayed in a search result
   */
  public String toStringSearch() {
    StringBuilder sb = new StringBuilder(track.getStyle().getName2()).append('/').append(
        track.getAuthor().getName2()).append('/').append(track.getAlbum().getName2()).append('/')
        .append(track.getName()).append(" [").append(directory.getName()).append('/').append(
            getName()).append(']');
    return sb.toString();
  }

  /**
   * Return true is the specified directory is an ancestor for this file
   * 
   * @param directory
   * @return
   */
  public boolean hasAncestor(Directory directory) {
    Directory dirTested = getDirectory();
    while (true) {
      if (dirTested.equals(directory)) {
        return true;
      } else {
        dirTested = dirTested.getParentDirectory();
        if (dirTested == null) {
          return false;
        }
      }
    }
  }

  /**
   * @return
   */
  public long getSize() {
    return getLongValue(XML_SIZE);
  }

  /**
   * @return
   */
  public Directory getDirectory() {
    return directory;
  }

  /**
   * @return associated device
   */
  public Device getDevice() {
    return directory.getDevice();
  }

  /**
   * @return associated type
   */
  public Type getType() {
    String extension = UtilSystem.getExtension(this.getName()); // getName() is
    // better
    // here as it will do
    // less and not create
    // java.io.File in
    // File
    if (extension != null) {
      return TypeManager.getInstance().getTypeByExtension(extension);
    }
    return null;
  }

  /**
   * @return
   */
  public long getQuality() {
    return getLongValue(XML_QUALITY);
  }

  /**
   * @return
   */
  public Track getTrack() {
    return track;
  }

  /**
   * Return absolute file path name
   * 
   * @return String
   */
  public String getAbsolutePath() {
    StringBuilder sbOut = new StringBuilder(getDevice().getUrl()).append(
        getDirectory().getRelativePath()).append(java.io.File.separatorChar).append(this.getName());
    return sbOut.toString();
  }

  /**
   * Alphabetical comparator used to display ordered lists of files
   * <p>
   * Sort ignoring cases but different items with different cases should be
   * distinct before being added into bidimap
   * </p>
   * 
   * @param other
   *          file to be compared
   * @return comparaison result
   */
  public int compareTo(File otherFile) {
    // Perf: leave if directories are equals
    if (otherFile.equals(this)) {
      return 0;
    }
    // Begin by comparing file parent directory for perf
    if (directory.equals(otherFile.getDirectory())) {
      // If both files are in the same directory, sort by track order
      int iOrder = (int) getTrack().getOrder();
      int iOrderOther = (int) otherFile.getTrack().getOrder();
      if (iOrder != iOrderOther) {
        return iOrder - iOrderOther;
      }
      // if same order too, simply compare file names
      String sAbs = getName();
      String sOtherAbs = otherFile.getName();
      // never return 0 here, because bidimap needs to distinct items
      int comp = sAbs.compareToIgnoreCase(sOtherAbs);
      if (comp == 0) {
        return sAbs.compareTo(sOtherAbs);
      }
      return comp;
    } else {
      // Files are in different directories, sort by parent directory
      return this.getDirectory().compareTo(otherFile.getDirectory());
    }
  }

  /**
   * Return true if the file can be accessed right now
   * 
   * @return true the file can be accessed right now
   */
  public boolean isReady() {
    if (getDirectory().getDevice().isMounted()) {
      return true;
    }
    return false;
  }

  /**
   * Return true if the file is currently refreshed or synchronized
   * 
   * @return true if the file is currently refreshed or synchronized
   */
  public boolean isScanned() {
    if (getDirectory().getDevice().isRefreshing() || getDirectory().getDevice().isSynchronizing()) {
      return true;
    }
    return false;
  }

  /**
   * Return Io file associated with this file
   * 
   * @return
   */
  public java.io.File getIO() {
    if (fio == null) {
      fio = new java.io.File(getAbsolutePath());
    }
    return fio;
  }

  /**
   * Return whether this item should be hidden with hide option
   * 
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getDirectory().getDevice().isMounted()
        || !Conf.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)) {
      return false;
    }
    return true;
  }

  /**
   * @param track
   *          The track to set.
   */
  public void setTrack(Track track) {
    this.track = track;
    setProperty(XML_TRACK, track.getID());
  }

  /**
   * Get item description
   */
  @Override
  public String getDesc() {
    return Messages.getString("Item_File") + " : " + getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(String sKey) {
    if (XML_DIRECTORY.equals(sKey)) {
      Directory dParent = DirectoryManager.getInstance().getDirectoryByID(getStringValue(sKey));
      return dParent.getFio().getAbsolutePath();
    } else if (XML_TRACK.equals(sKey)) {
      return getTrack().getName();
    } else if (XML_SIZE.equals(sKey)) {
      return (Math.round(getSize() / 10485.76) / 100f) + Messages.getString("FilesTreeView.54");
    } else if (XML_QUALITY.equals(sKey)) {
      return getQuality() + Messages.getString("FIFO.13");
    } else if (XML_ALBUM.equals(sKey)) {
      return getTrack().getAlbum().getName2();
    } else if (XML_STYLE.equals(sKey)) {
      return getTrack().getStyle().getName2();
    } else if (XML_AUTHOR.equals(sKey)) {
      return getTrack().getAuthor().getName2();
    } else if (XML_TRACK_LENGTH.equals(sKey)) {
      return UtilString.formatTimeBySec(getTrack().getDuration());
    } else if (XML_TRACK_RATE.equals(sKey)) {
      return Long.toString(getTrack().getRate());
    } else if (XML_DEVICE.equals(sKey)) {
      return getDirectory().getDevice().getName();
    } else if (XML_ANY.equals(sKey)) {
      return getAny();
    } else {// default
      return super.getHumanValue(sKey);
    }
  }

  /**
   * @return a human representation of all concatenated properties
   */
  @Override
  public String getAny() {
    // rebuild any
    StringBuilder sb = new StringBuilder(100);
    File file = this;
    Track lTrack = file.getTrack();
    sb.append(super.getAny()); // add all files-based properties
    // now add others properties
    sb.append(file.getDirectory().getDevice().getName());
    sb.append(lTrack.getName());
    sb.append(lTrack.getStyle().getName2());
    sb.append(lTrack.getAuthor().getName2());
    sb.append(lTrack.getAlbum().getName2());
    sb.append(lTrack.getDuration());
    sb.append(lTrack.getRate());
    sb.append(lTrack.getValue(XML_TRACK_COMMENT));// custom properties now
    sb.append(lTrack.getValue(XML_TRACK_ORDER));// custom properties now
    return sb.toString();
  }

  /** Reset pre-calculated paths* */
  protected void reset() {
    // sAbs = null;
    fio = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIconRepresentation()
   */
  @Override
  public ImageIcon getIconRepresentation() {
    ImageIcon icon = null;
    String ext = UtilSystem.getExtension(getName()); // getName() is better
    // here as
    // it will do less and not
    // create java.io.File in File
    Type type = TypeManager.getInstance().getTypeByExtension(ext);
    // Find associated icon with this type
    URL iconUrl = null;
    String sIcon;
    if (type != null) {
      sIcon = (String) type.getProperties().get(XML_TYPE_ICON);
      try {
        iconUrl = new URL(sIcon);
      } catch (MalformedURLException e) {
        Log.error(e);
      }
    }
    if (iconUrl == null) {
      icon = IconLoader.getIcon(JajukIcons.TYPE_WAV);
    } else {
      icon = new ImageIcon(iconUrl);
    }
    return icon;
  }

  /**
   * Set name (useful for Windows because same object can have different cases)
   * 
   * @param name
   *          Item name
   */
  protected void setName(String name) {
    setProperty(XML_NAME, name);
    this.name = name;
  }

  /**
   * 
   * @return text to be displayed in the tray ballon and tooltip with HTML
   *         formating that is used correctly under Linux
   */
  public String getHTMLFormatText() {
    String sOut = "";
    sOut += "<HTML><br>";
    String size = "100x100";
    int maxSize = 30;
    ThumbnailManager.refreshThumbnail(FIFO.getCurrentFile().getTrack().getAlbum(), size);
    java.io.File cover = UtilSystem.getConfFileByPath(FILE_THUMBS + '/' + size + '/'
        + FIFO.getCurrentFile().getTrack().getAlbum().getID() + '.' + EXT_THUMB);
    if (cover.canRead()) {
      sOut += "<p ALIGN=center><img src='file:" + cover.getAbsolutePath() + "'/></p><br>";
    }
    // We use gray color for font because, due to a JDIC bug under
    // Linux, the
    // balloon background is white even if the the look and feel is dark
    // (like ebony)
    // but the look and feel makes the text white is it is black
    // initialy
    // so text is white on white is the balloon. It must be displayed in
    // the tooltip too
    // and this issue doesn't affect the tray tooltip. This color is the
    // only one to be correctly displayed
    // in a dark and a light background at the same time
    sOut += "<p><font color='#484848'><b>"
        + UtilString.getLimitedString(getTrack().getName(), maxSize) + "</b></font></p>";
    String sAuthor = UtilString.getLimitedString(getTrack().getAuthor().getName(), maxSize);
    if (!sAuthor.equals(UNKNOWN_AUTHOR)) {
      sOut += "<p><font color='#484848'>" + sAuthor + "</font></p>";
    }
    String sAlbum = UtilString.getLimitedString(getTrack().getAlbum().getName(), maxSize);
    if (!sAlbum.equals(UNKNOWN_ALBUM)) {
      sOut += "<p><font color='#484848'>" + sAlbum + "</font></p>";
    }
    sOut += "</HTML>";
    return sOut;
  }

  /**
   * 
   * @return Text to be displayed in the tootip and baloon under windows.
   * 
   */
  public final String getBasicFormatText() {
    String sOut = "";
    sOut = "";
    String sAuthor = getTrack().getAuthor().getName();
    if (!sAuthor.equals(UNKNOWN_AUTHOR)) {
      sOut += sAuthor + " / ";
    }
    String sAlbum = getTrack().getAlbum().getName();
    if (!sAlbum.equals(UNKNOWN_ALBUM)) {
      sOut += sAlbum + " / ";
    }
    sOut += getTrack().getName();
    return sOut;
  }

}
