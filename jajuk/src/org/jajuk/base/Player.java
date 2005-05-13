/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

import java.util.Properties;

import javax.sound.sampled.LineUnavailableException;

import javazoom.jlgui.basicplayer.BasicPlayerException;

import org.jajuk.i18n.Messages;
import org.jajuk.players.IPlayerImpl;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

/**
 *  abstract class for music player, independent from real implementation
 *
 * @author     Bertrand Florat
 * @created    12 oct. 2003
 */
public class Player implements ITechnicalStrings{
    
    /**Current file read*/
    private static File fCurrent;
    /**Current player used*/
    private static IPlayerImpl pCurrentPlayerImpl;
    /**Mute flag */
    private static boolean bMute = false;
    /**Paused flag*/
    private static boolean bPaused = false;
    /**Playing ?*/
    private static boolean bPlaying = false;
    
    
    /**
     * Asynchronous play for specified file with specified time interval
     * @param file to play
     * @param position in % of the file length. ex 0.1 for 10%
     * @param length in ms
     * @return true if play is OK
     */
    public static boolean play(final File file,final float fPosition,final long length) {
        stop(); //stop any playing track
        fCurrent = file;
        pCurrentPlayerImpl = file.getTrack().getType().getPlayerImpl();
        try {
            bPlaying = true;
            bPaused = false;
            boolean bWaitingLine = true;
            while (bWaitingLine){
                try{
                    if (bMute){
                        pCurrentPlayerImpl.play(fCurrent,fPosition,length,0.0f);
                    }
                    else{
                        pCurrentPlayerImpl.play(fCurrent,fPosition,length,ConfigurationManager.getFloat(CONF_VOLUME));
                    }
                    bWaitingLine = false;
                }
                catch(BasicPlayerException bpe){
                    if (!(bpe.getCause() instanceof LineUnavailableException)){
                        throw bpe;
                    }
                    bWaitingLine = true;
                    Log.debug("Line occupied, waiting");
                    InformationJPanel.getInstance().setMessage(Messages.getString("Player.0"),InformationJPanel.WARNING);
                    try {
                        Thread.sleep(WAIT_AFTER_ERROR); //wait for the line
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return true;
        } catch (final Throwable t) {
            Properties pDetails = new Properties();
            pDetails.put(DETAIL_CURRENT_FILE,file);
            ObservationManager.notify(new Event(EVENT_PLAY_ERROR,pDetails)); //notify the error 
            Log.error("007",fCurrent.getAbsolutePath(), t); //$NON-NLS-1$
            try {
                Thread.sleep(WAIT_AFTER_ERROR); //make sure user has time to see this error message
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            //process playing error asynchonously to avoid loop problems when capscading errors
            new Thread(){
                public void run(){
                    Player.stop();
                    FIFO.getInstance().finished();
                }
            }.start();
            return false;
        }			
    }
    
    /**
     * Stop the played track
     * @param type
     */
    public static void stop() {
        try {
            if (pCurrentPlayerImpl == null){ //none current player, leave
                return;
            }
            if (fCurrent!=null){
                fCurrent.getTrack().getType().getPlayerImpl().stop();
                bPaused = false; //cancel any current pause
                bPlaying = false;
            }
        } catch (Exception e) {
            Log.error("008",fCurrent.getName(),e); //$NON-NLS-1$
        }
    }
    
    /**
     * Alternative Mute/unmute the player
     * @throws Exception
     */
    public static void mute() {
        try {
            if (pCurrentPlayerImpl == null){ //none current player, leave
                return;
            }
            if (Player.bMute){ //already muted, unmute it by setting the volume previous mute
                pCurrentPlayerImpl.setVolume(ConfigurationManager.getFloat(CONF_VOLUME));
            }
            else{
                pCurrentPlayerImpl.setVolume(0.0f);
            }
            Player.bMute = !Player.bMute;
            //notify UI
            ObservationManager.notify(new Event(EVENT_MUTE_STATE));
        } catch (Exception e) {
            Log.error(e); 
        }
    }
    
    
    /**
     * Mute/unmute the player
     * @param bMute
     * @throws Exception
     */
    public static void mute(boolean bMute) {
        try {
            if (pCurrentPlayerImpl == null){ //none current player, leave
                return;
            }
            if (bMute){
                pCurrentPlayerImpl.setVolume(0.0f);
            }
            else{
                pCurrentPlayerImpl.setVolume(ConfigurationManager.getFloat(CONF_VOLUME));
            }
            Player.bMute = bMute;
        } catch (Exception e) {
            Log.error(e); 
        }
    }
    
    /**
     * 
     * @return whether the player is muted or not
     * @throws Exception
     */
    public static boolean isMuted() {
        return bMute;
    }
    
    /**
     * Set the gain
     * @param fVolume : gain from 0 to 1
     * @throws Exception
     */
    public static void setVolume(float fVolume){
        try {
            ConfigurationManager.setProperty(CONF_VOLUME,Float.toString(fVolume));
            if (pCurrentPlayerImpl != null){
                pCurrentPlayerImpl.setVolume(fVolume);
            }
        } catch (Exception e) {
            Log.error(e); 
        }	
    }
    
    
    /**
     * @return Returns the lTime in ms
     */
    public static long getElapsedTime() {
        if (pCurrentPlayerImpl != null){
            return pCurrentPlayerImpl.getElapsedTime();
        }
        else{
            return 0;
        }
    }
    
    /**Pause the player*/
    public static void pause() {
        try {
            if (!bPlaying){ //ignore pause when not playing to avoid confusion between two tracks
                return;
            }
            if (pCurrentPlayerImpl != null){
                pCurrentPlayerImpl.pause();
            }
            bPaused = true;
        } catch (Exception e) {
            Log.error(e); 
        }
    }
    
    /**resume the player*/
    public static void resume(){
        try {
            if (pCurrentPlayerImpl == null){ //none current player, leave
                return;
            }
            pCurrentPlayerImpl.resume();
            bPaused = false;
        } catch (Exception e) {
            Log.error(e); 
        }
    }
    
    /**
     * @return whether player is paused
     */
    public static boolean isPaused() {
        return bPaused;
    }
    
    /**
     * Force the bPaused state to allow to cancel a pause without restarting the current played track (rew for exemple)  
     * @param bPaused
     */
    public static void setPaused(boolean bPaused){
        Player.bPaused = bPaused;
    }
    
    /**Seek to a given position in %. ex : 0.2 for 20% */
    public static void seek(float fPosition){
        if (pCurrentPlayerImpl == null){ //none current player, leave
            return;
        }
        // bound seek
        if (fPosition < 0.0f){
            fPosition = 0.0f;
        }
        else if (fPosition >= 1.0f){
            fPosition = 0.99f;
        }
        try{
            pCurrentPlayerImpl.seek(fPosition);
        }
        catch(Exception e){ //we can get some errors in unexpected cases
            Log.debug(e.toString());
        }
        
    }
    
    /**
     * @return position in track in %
     */
    public static float getCurrentPosition(){
        if (pCurrentPlayerImpl != null){
            return pCurrentPlayerImpl.getCurrentPosition();
        }
        else{
            return 0.0f;
        }
        
        
    }
    /**
     * @return Returns the bPlaying.
     */
    public static boolean isPlaying() {
        return bPlaying;
    }
}
