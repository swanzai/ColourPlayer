/* Copyright (C) 2006 Michael Voong

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */

package actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import models.PlaylistDao;
import models.Track;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import views.MainView;
import controllers.PlaybackError;
import controllers.Player;

public class PlayAction extends ButtonAction {
    private final PlaylistDao playlist;
    private final Player player;

    private boolean isPlaying = false;

    private ImageDescriptor playImageDescriptor;
    private ImageDescriptor pauseImageDescriptor;

    private final FileScanningAction fileScanner;

    public PlayAction(Button button, Player player, PlaylistDao playlist,
            FileScanningAction fileScanner) throws MalformedURLException {
        super(button);
        this.player = player;
        this.playlist = playlist;
        this.fileScanner = fileScanner;

        playImageDescriptor = ImageDescriptor.createFromURL(new URL(
                "file:images/play_f.png"));
        pauseImageDescriptor = ImageDescriptor.createFromURL(new URL(
                "file:images/pause_f.png"));

        isPlaying = false;
        
        updateImage();
    }

    /*
     * Play a track. This is only ever called by manual interaction by the user
     */
    @Override
    public void run() {
        // false to toggle playing/pause instead of forcing a play
        play(true, false);
    }

    public void play(boolean userChanged, boolean force) {
        player.setInAdvancingMode(true);

        Track track = playlist.getTrack();
        if (track == null)
            return;

        if (!force && (player.isPaused() || player.isPlaying())) {
            // this means that we are playing or the player is in pause mode, so
            // we toggle playback
            player.pause();
            isPlaying = !player.isPaused();
        } else if (force || !player.isPlaying()) {
            // if the user is forcing a playback, or the player is stopped, then
            // simply play the track
            String url = track.getPath();
            try {
                // update the database with new tag data if available
                fileScanner.scanFilesQuiet(new String[] { new File(url)
                        .getAbsolutePath() });
                // TODO: if this updates the database, refresh the table
                player.play(url);
            } catch (FileNotFoundException e) {
                MessageBox messageBox = new MessageBox(MainView.shell, SWT.OK
                        | SWT.ICON_ERROR);
                messageBox.setText("Track Not Found");
                messageBox
                        .setMessage("The track you selected could not be played.\nIt may have been deleted or removed from your computer.\n\n"
                                + track.getArtist() + " - " + track.getTitle());
                messageBox.open();
            } catch (MalformedURLException murl) {
                MessageBox messageBox = new MessageBox(MainView.shell, SWT.OK
                        | SWT.ICON_ERROR);
                messageBox.setText("Invalid Stream");
                messageBox
                        .setMessage("The stream you tried to play is not valid.\n\n"
                                + track.getPath());
                messageBox.open();
            } catch (PlaybackError pe) {
                MessageBox messageBox = new MessageBox(MainView.shell, SWT.OK
                        | SWT.ICON_ERROR);
                messageBox.setText("Playback Error");
                messageBox
                        .setMessage("There was a player decoder error in the following file:\n\n"
                                + track.getPath() + "\n\n" + "The file may be corrupt.");
                messageBox.open();
            }
            isPlaying = true;
        }

        // ensure the image is updated on the SWT thread
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                updateImage();
            }
        });
    }

    /**
     * Not thread safe
     */
    private void updateImage() {
        if (isPlaying) {
            setImageDescriptor(pauseImageDescriptor);
            setToolTipText("Pause");
            setText("&Pause");
        } else {
            setImageDescriptor(playImageDescriptor);
            setToolTipText("Play");
            setText("&Play");
        }
        button.redraw();
    }

    /**
     * Allows different objects to tell PlayAction whether the player is in play
     * mode or not. This allows the icon to be changed to reflect changes
     * 
     * @param playing
     */
    public void setPlaying(boolean playing) {
        isPlaying = playing;
        updateImage();
    }
}
