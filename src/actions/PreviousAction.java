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

import java.net.MalformedURLException;
import java.net.URL;

import models.PlaylistDao;
import models.Track;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Button;

import controllers.Player;

public class PreviousAction extends ButtonAction {
    private final PlaylistDao playlist;

    private final Player player;

    private final PlayAction playAction;

    public PreviousAction(Button button, Player player, PlaylistDao playlist, PlayAction playAction) throws MalformedURLException {
        super(button);
        this.player = player;
        this.playlist = playlist;
        this.playAction = playAction;

        setImageDescriptor(ImageDescriptor.createFromURL(new URL("file:images/prev_f.png")));
        setToolTipText("Previous");
        setText("P&revious");
    }

    @Override
    public void run() {
        if (playlist.previousTrack()) {
            Track track = playlist.getTrack();

            if (player.isPlaying()) {
                playAction.play(true, true);
            }
        }

    }
}
