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

package controllers;

import models.PlayerCallBackData;
import models.PlaylistDao;
import views.PlayerListener;
import actions.PlayAction;

/**
 * A listener that when attached to a Player object, will use a playlist to
 * advance the track when needed.
 * 
 * <b>Note:</b> If the player has been manually stopped by the user, the track
 * will not be advanced when the listener receives the stop message
 * 
 * @author Michael Voong
 * 
 */
public class TrackChangeController implements PlayerListener {
    private final PlaylistDao playlist;

    private final PlayAction playAction;

    public TrackChangeController(PlaylistDao playlist, PlayAction playAction) {
        this.playlist = playlist;
        this.playAction = playAction;
    }

    public void message(String message, Player player) {
        if (message.equals("stopped")) {
            if (player.isInAdvancingMode()) {
                playlist.nextTrack();
                playAction.play(false, true);
            }
        }
    }

    public void callback(PlayerCallBackData data) {

    }
}
