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
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.UnsupportedAudioFileException;

import models.PlaylistDao;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import controllers.Player;
import data.MusicScanner;

public class StreamOpenAction extends Action {
    private final PlayAction playAction;
    private final PlaylistDao nowPlaying;
    private final MusicScanner scanner;

    public StreamOpenAction(PlaylistDao nowPlaying, MusicScanner scanner,
            PlayAction playAction) {
        this.nowPlaying = nowPlaying;
        this.scanner = scanner;
        this.playAction = playAction;

        setText("Open File/Stream");
    }

    // public void handleEvent(Event event) {
    // openDialog();
    // }

    @Override
    public void run() {
        // openDialog();

        Player p = new Player();
        try {
            p.play("http://mublog.co.uk/finalyear/music/Dethbed.mp3");
        } catch (Exception e) {         
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Playback Error", "There was an error opening the stream");
        }
    }

    private void openDialog() {
        FileDialog dialog = new FileDialog(Display.getDefault()
                .getActiveShell());
        String url = dialog.open();
        if (url != null) {
            try {
                if (new File(url).isFile()) {
                    url = "file://" + url;
                }
                startPlaying(url);
            } catch (MalformedURLException mue) {
                MessageBox messageBox = new MessageBox(Display.getDefault()
                        .getActiveShell(), SWT.OK | SWT.ICON_ERROR);
                messageBox.setMessage("The file you specified is invalid");
                messageBox.open();
            } catch (UnsupportedAudioFileException uafe) {
                MessageBox messageBox = new MessageBox(Display.getDefault()
                        .getActiveShell(), SWT.OK | SWT.ICON_ERROR);
                messageBox
                        .setMessage("The file you specified is not a valid audio file");
                messageBox.open();
            }
        }
    }

    private void startPlaying(String url) throws MalformedURLException,
            UnsupportedAudioFileException {
        // add to playlist and start playing
        System.out.println(url + ";");
        URL urlObject = new URL(url);

        scanner.addSong(urlObject);
    }
}
