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

package listeners;

/**
 * A listener fired when the playlist is starving. That is, the playlist runs
 * out of tracks and the player requests a new track.
 * 
 * @author Mike
 */
public interface PlaylistStarvingListener {
    /**
     * A notification event to say that the playlist has run out of tracks
     * 
     * @param fetchAmount
     *            The number of tracks the caller wishes to fetch
     */
    public void playlistStarving(int fetchAmount);
}
