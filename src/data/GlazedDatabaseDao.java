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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.PlaylistDao;
import models.Track;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import exceptions.DataAccessException;
import exceptions.DatabaseInUseException;
import exceptions.DriverNotFoundException;
import glazedlists.matchers.ColouredTrackMatcher;

public class GlazedDatabaseDao extends DatabaseDao {

    EventList<Track> eventList;
    private HashMap<Integer, Track> trackMap;
    private FilterList<Track> colouredTracksEventList;

    private PlaylistDao currentPlaylist;

    public GlazedDatabaseDao() throws DriverNotFoundException,
            DatabaseInUseException {
        super();

        trackMap = new HashMap<Integer, Track>();

        try {
            loadData();
        } catch (DataAccessException dae) {
            dae.printStackTrace();
        }
    }

    /**
     * Load data into the event list
     */
    private void loadData() throws DataAccessException, DatabaseInUseException {
        List<Track> tracks = getTracks(0, 0);

        eventList = new BasicEventList<Track>();
        eventList.addAll(tracks);

        for (Track track : tracks) {
            trackMap.put(new Integer(track.getId()), track);
        }

        // maintain an event list with tracks that have a colour
        colouredTracksEventList = new FilterList<Track>(eventList,
                new ColouredTrackMatcher());
    }

    @Override
    public List<Track> getTracks(TrackFilter filter, boolean strict, int limit,
            int offset) throws DataAccessException {
        System.err.println("Depreciated method called");
        new Exception().printStackTrace();

        return null;
    }

    public List<Integer> getPlaylistTrackIds(String playlistName)
            throws DataAccessException {
        PreparedStatement statement = DatabaseDao.Statements.getTrackIdPlaylist;
        List<Integer> ids = new ArrayList<Integer>();
        try {
            statement.setString(1, playlistName);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                ids.add(new Integer(rs.getInt(1)));
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }

        return ids;
    }

    @Override
    public int addTrack(Track track) throws DataAccessException {
        int action = super.addTrack(track);

        Track existingTrack = getTrackById(track.getId());

        try {
            eventList.getReadWriteLock().writeLock().lock();

            if (existingTrack != null) {
                // already exists in data structure.. so update
                int index = eventList.indexOf(track);
                if (index != -1)
                    eventList.set(index, track);
            } else {
                // insert into data structure
                eventList.add(track);
                System.out.println("added");
            }
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }

        trackMap.put(new Integer(track.getId()), track);

        return action;
    }

    public EventList<Track> getTracksEventList() {
        return eventList;
    }

    public FilterList<Track> getColouredTracksEventList() {
        return colouredTracksEventList;
    }

    public Track getTrackById(int i) {
        return trackMap.get(new Integer(i));
    }
}
