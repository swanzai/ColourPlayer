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

package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import listeners.PlaylistStarvingListener;
import ca.odell.glazedlists.BasicEventList;
import data.GlazedDatabaseDao;
import exceptions.DataAccessException;

public class PlaylistDao {
    private int pointer = -1;

    private Random random;

    private HashSet<Track> history = new LinkedHashSet<Track>();

    private final GlazedDatabaseDao dao;

    private final String name;

    private Track currentTrack;

    private int playlistId;

    private BasicEventList<Track> eventList;

    public static final int PLAYBACK_SHUFFLE = 51231;

    public static final int PLAYBACK_NORMAL = 41234;

    private int mode = PLAYBACK_NORMAL;

    private List<PlaylistStarvingListener> starvingListeners = new ArrayList<PlaylistStarvingListener>();

    private int starvingFetchAmount = 5;

    private boolean enableStarvingProducer = false;

    public PlaylistDao(GlazedDatabaseDao dao, String name) throws DataAccessException {
        this.dao = dao;
        this.name = name;

        updatePlaylistId();
        initEventList();

        // point to first track if there exists one
        if (eventList.size() > 0) {
            nextTrack();
        }

        setMode(PLAYBACK_NORMAL);
    }

    private void initEventList() throws DataAccessException {
        this.eventList = new BasicEventList<Track>();
        List<Integer> ids = this.dao.getPlaylistTrackIds(this.name);
        Track track;

        for (Integer id : ids) {
            track = dao.getTrackById(id.intValue());
            eventList.add(track);
        }
    }

    private synchronized void updatePlaylistId() {
        try {
            this.playlistId = dao.getPlaylistId(name);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Note: doesnt add to dao.. we have to do that separately
     * 
     * @param tracks
     */
    public synchronized void addAllTracks(Collection<Track> tracks) {
        // if it's a new track then set current pointer to it
        if (pointer == -1) {
            setPointer(0);
        }

        eventList.addAll(tracks);
    }

    public synchronized void addTrack(Track track) throws DataAccessException {
        // if it's a new track then set current pointer to it
        if (pointer == -1) {
            setPointer(0);
        }

        eventList.add(track);
        addTrackToDao(track);
    }

    public synchronized void addTrackToDao(Track track) throws DataAccessException {
        dao.addTrackToPlaylist(track.getId(), this.name);
    }

    public synchronized void clear() throws DataAccessException {
        eventList.clear();
        history.clear();
        setPointer(-1);
        // remove all from playlist

        dao.clearPlaylist(this.name);
    }

    /**
     * Set pointer and therefore we need to update the currentTrack cached in
     * this object
     * 
     * @param index
     */
    public synchronized void setPointer(int index) {
        history.add(getTrack());
        
        pointer = index;

        if (pointer == -1) {
            currentTrack = null;
        } else {
            currentTrack = getTrack(index);
        }

    }

    public synchronized boolean nextTrack() {
        switch (mode) {
        case PLAYBACK_NORMAL:            
            if (!hasNextTrack() && enableStarvingProducer) {
                firePlaylistStarvingEvent();
            }

            // if not pointing to a track or pointing to
            // last track already
            if (!hasNextTrack()) {
                System.out.println("No track. Cannot advance pointer in nextTrack()");
                return false;
            } else {
                setPointer(pointer + 1);
                return true;
            }

        case PLAYBACK_SHUFFLE:
            setPointer((int) (getRandom().nextDouble() * eventList.size()));
            return true;
        }

        return false;
    }

    public synchronized boolean hasNextTrack() {
        return (mode == PLAYBACK_SHUFFLE) || !(pointer < -1 || pointer == eventList.size() - 1);
    }

    public synchronized boolean hasPreviousTrack() {
        return (mode == PLAYBACK_SHUFFLE) || !(pointer == -1 || pointer == 0);
    }

    public synchronized boolean previousTrack() {
        switch (mode) {
        case PLAYBACK_NORMAL:
            // if not pointing to a track or pointing to first track already
            if (!hasPreviousTrack()) {
                System.out.println("No track. Cannot unadvance pointer in previousTrack()");
                return false;
            } else {
                pointer--;
                return true;
            }
        case PLAYBACK_SHUFFLE:
            // set pointer to last item in history
            // TODO: implement this using a history
            return false;
        }

        return false;
    }

    private synchronized Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }

    public synchronized Track getTrack() {
        return getTrack(pointer);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public synchronized boolean remove(int index) throws DataAccessException {
        if (index < eventList.size()) {
            eventList.remove(index);
            dao.removeTrackFromPlaylist(name, getTrack(index).getIdp());
            if (pointer==index) {
                setPointer(-1);
            } else if (pointer > index) {
                pointer--;
            }
        }

        return true;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized Track getTrack(int index) {
        if (index >= eventList.size() || index < 0) {
            return null;
        }

        return eventList.get(index);
    }

    public BasicEventList<Track> getEventList() {
        return eventList;
    }

    private void firePlaylistStarvingEvent() {
        for (PlaylistStarvingListener l : starvingListeners) {
            l.playlistStarving(starvingFetchAmount);
        }
    }

    public void addPlaylistStarvingListener(PlaylistStarvingListener l) {
        starvingListeners.add(l);
    }

    public HashSet<Track> getHistory() {
        return history;
    }

    public void setEnableStarvingProducer(boolean enableStarvingProducer) {
        this.enableStarvingProducer = enableStarvingProducer;
    }
}
