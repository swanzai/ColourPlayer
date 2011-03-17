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

import java.util.ArrayList;
import java.util.List;

import listeners.ColourAssociationListener;
import listeners.ColourPickerListener;
import models.PlaylistDao;
import models.Track;
import ca.odell.glazedlists.EventList;
import data.Dao;
import exceptions.DataAccessException;

public class TrackAssociatingColourPickerListener implements
        ColourPickerListener {
    private final PlaylistDao playlist;

    private final Dao dao;

    private long lastPicked;

    /**
     * This mutex has to be static otherwise UpdateItem is a different class, so
     * would be able to access the lock even if it's held by
     * TrackAssociatingColourPickerListener
     */

    private String type = "track";

    private ArrayList<ColourAssociationListener> listeners;

    private static final int UPDATE_INTERVAL_MS = 400;

    private final EventList<Track> eventList;

    public TrackAssociatingColourPickerListener(PlaylistDao playlist, Dao dao,
            EventList<Track> eventList) {
        this.playlist = playlist;
        this.dao = dao;
        this.eventList = eventList;
        this.lastPicked = 0;

    }

    public void pickedColour(List<Track> referenceTracks, int r, int g, int b)
            throws IllegalArgumentException {
        try {
            if (referenceTracks == null || referenceTracks.size() == 0) {
                return;
            }

            for (Track track : referenceTracks) {
                dao.setTrackColour(track.getId(), r, g, b);
                if (r == 0 && g == 0 && b == 0) {
                    track.clearColour();
                } else {
                    track.setColour(r, g, b);
                }

                int index = eventList.indexOf(track);
                // only update row if showing in table
                if (index != -1) {

                    eventList.set(index, track);
                }
            }

            notifyColourAssociationListeners(referenceTracks.size(),
                    referenceTracks);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public void clearedColour(List<Track> referenceTracks) {
        // simply set to 0 to clear (int column in database is 0 when "null")
        pickedColour(referenceTracks, 0, 0, 0);
    }

    public void addColourAssociationListener(ColourAssociationListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ColourAssociationListener>();
        }
        listeners.add(listener);
    }

    private void notifyColourAssociationListeners(int noOfAssociations,
            List<Track> tracks) {
        for (ColourAssociationListener listener : listeners) {
            listener.updatedColours(noOfAssociations, tracks);
        }
    }

    void setApplicationType(String type) {
        this.type = type;
    }

    class UpdateItem {
        Track track;

        int rgb[];
    }
}
