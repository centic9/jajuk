/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

import java.util.ArrayList;

/**
 *  A playlist
 *<p> Logical item
 * @Playlist     bflorat
 * @created    17 oct. 2003
 */
public class Playlist extends PropertyAdapter {

	/**ID. Ex:1,2,3...*/
	private String sId;
	/**Associated playlist files**/
	private ArrayList alFiles = new ArrayList(2);
	
	/**
	 * Playlist constructor
	 * @param sId
	 * @param file :an associated playlist file
	 */
	public Playlist(String sId,PlaylistFile plFiles){
		this.sId = sId;
		this.alFiles.add(plFiles);
	}

	/**
	 * toString method
	 */
	public String toString() {
		StringBuffer sbOut = new StringBuffer("Playlist[ID="+sId+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0;i<alFiles.size();i++){
			sbOut.append('\n').append(alFiles.get(i).toString());
		}
		return sbOut.toString();
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<playlist id='" + sId);
		sb.append("' playlist_files='");
		for (int i=0;i<alFiles.size();i++){
			sb.append(((PlaylistFile)alFiles.get(i)).getId()).append(',');
		}
		sb.deleteCharAt(sb.length()-1); //remove the last ','
		sb.append("' ");
		sb.append(getPropertiesXml());
		sb.append("/>\n");
		return sb.toString();
	}

	
	/**
	 * Equal method to check two playlists are identical
	 * @param otherPlaylist
	 * @return
	 */
	public boolean equals(Object otherPlaylist){
		return this.getId().equals(((Playlist)otherPlaylist).getId());
	}	

	
	/**
	 * hashcode ( used by the equals method )
	 */
	public int hashCode(){
		return getId().hashCode();
	}


	/**
	 *  Add a playlist file 
	 * @return
	 */
	public ArrayList getFiles() {
		return alFiles;
	}
	
	/**
	 * @return
	 */
	public void addFile(PlaylistFile plFile) {
		if (!alFiles.contains(plFile)) {
			alFiles.add(plFile);
		}
	}

	/**
	 * Remove a playlist file *
	 * 
	 * @return
	 */
	public void removeFile(PlaylistFile plFile) {
		alFiles.remove(plFile);
	}

	/**
	 * @return
	 */
	public String getId() {
		return sId;
	}
	
	/**
	 * Return true if this playlist map to specified playlist file id 
	 * @param sId
	 * @return
	 */
	private boolean mapPlaylistFile(String sId){
		for (int i=0;i<alFiles.size();i++){
			if (((PlaylistFile)alFiles.get(i)).getId().equals(sId)){
				return true;
			}	
		}
		return false;
	}	
	
	/**
	 * Get playlist name
	 * @return playlist name
	 */
	public String getName(){
		String sOut = "";
		if ( alFiles.size() > 0){
			sOut =((PlaylistFile)alFiles.get(0)).getName(); 
		}
		return sOut; 
	}

}
