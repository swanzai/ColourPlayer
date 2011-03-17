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

package glazedlists.matchers;

import java.util.HashSet;

import models.PlaylistDao;
import models.Track;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.Matcher;

public class PlaylistTrackMatcher implements Matcher<Track> {
    private PlaylistDao playlist;
    private HashSet<Track> playlistTracks;

    public synchronized void setPlaylist(PlaylistDao playlist) {
        this.playlist = playlist;

        if (playlist != null) {
            EventList<Track> eventList = playlist.getEventList();
            playlistTracks = new HashSet<Track>();
            for (Track track : eventList) {
                playlistTracks.add(track);
            }
        } else {
            if (playlistTracks!=null)
                playlistTracks.clear();
        }
    }

    public boolean matches(Track track) {
        if (playlist == null) {
            return true;
        }

        return playlistTracks.contains(track);
    }
}
