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

package data;

import java.util.List;

import models.Track;
import exceptions.DataAccessException;

public interface Dao {
    public final int TRACK_INSERTED = 45;
    public final int TRACK_UPDATED = 46;
    
    /**
     * Get a specific track by track id
     * 
     * @param id
     * @return TRACK_INSERTED or TRACK_UPDATED
     */
    public Track getTrack(int id);

    public int addTrack(Track track) throws DataAccessException;

    /**
     * Add a specific track to the persistent model. If the track exists
     * already, update it
     * 
     * @param trackId
     * @param playlist
     *            The name of the playlist
     * @throws DataAccessException
     */
    public void addTrackToPlaylist(int trackId, String playlist)
            throws DataAccessException;

    public void removeTrackFromPlaylist(String playlistName, int trackId) throws DataAccessException;
    
    public void removeTrack(int trackId) throws DataAccessException;    

    /**
     * Get all tracks with a limit and offset
     * 
     * @param limit
     * @param offset
     * @return
     * @throws DataAccessException
     */
    public List<Track> getTracks(int limit, int offset)
            throws DataAccessException;

    /**
     * Get the total number of tracks in the persistent model
     * 
     * @return
     * @throws DataAccessException
     */
    public int getNoOfTracks() throws DataAccessException;

    
    public int getNoOfTrackColours() throws DataAccessException;

    /**
     * Stores a track colour in the persistent model for a given track id
     * 
     * @param trackId
     * @param r
     * @param g
     * @param b
     * @throws DataAccessException
     */
    public void setTrackColour(int trackId, int r, int g, int b)
            throws DataAccessException;

    /**
     * Removes all tracks from a given playlist
     * 
     * @param playlist
     * @throws DataAccessException
     */
    public void clearPlaylist(String playlist) throws DataAccessException;

    public int getPlaylistId(String playlist) throws DataAccessException;

    public int getArtistId(String artist) throws DataAccessException;

    public int getGenreId(String genre) throws DataAccessException;

    public int getAlbumId(String album) throws DataAccessException;
}
