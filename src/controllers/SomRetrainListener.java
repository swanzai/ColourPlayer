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

import java.util.List;

import listeners.ColourAssociationListener;
import models.Track;
import som.Som;
import data.Dao;

public class SomRetrainListener implements ColourAssociationListener {
    private final Dao dao;

    private Som som;

    public SomRetrainListener(Dao dao) {
        this.dao = dao;
    }

    /*
     * Retrain the som as we have associated a bunch of new tracks
     */
    public void updatedColours(int noOfColours, List<Track> updatedTracks) {
        if (som == null)
            return;

        som.initRetrain();
        som.train(Som.RETRAIN_LEARNING_RATE, Som.RETRAIN_MAP_RADIUS);
    }

    public void setSom(Som som) {
        this.som = som;
    }
}
