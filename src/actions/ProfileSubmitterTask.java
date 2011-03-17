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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;

import listeners.ColourAssociationListener;
import models.Track;
import controllers.ProfileWriter;

public class ProfileSubmitterTask extends TimerTask implements ColourAssociationListener {
    private final Properties properties;

    public ProfileSubmitterTask(Properties properties) {
        this.properties = properties;
    }

    /**
     * Tracks to update
     */
    List<Track> tracks;

    public void run() {
        synchronized (this) {
            String allow = properties.getProperty("allow_sending_associations");
            if (allow == null || allow.equals("true")) {
                if (tracks != null && tracks.size() > 0) {
                    ProfileWriter writer = new ProfileWriter(Integer.parseInt(properties.get("unique_id").toString()));

                    Track[] a = new Track[tracks.size()];
                    writer.process(tracks.toArray(a));
                    writer.submit();

                    // System.out.println("Submitted " + a.length
                    // + " colour associations");
                    tracks.clear();
                }
            }
        }
    }

    public void updatedColours(int noOfColours, List<Track> tracks) {
        synchronized (this) {
            if (this.tracks == null) {
                this.tracks = new ArrayList<Track>();
            }

            Iterator existingIterator;
            Track t2;

            // loop through the new tracks
            for (Track t : tracks) {
                boolean found = false;
                existingIterator = this.tracks.iterator();

                // loop through existing tracks to search for current track
                while (existingIterator.hasNext()) {
                    t2 = (Track) existingIterator.next();

                    // if the track exists, remove it and add the new one
                    if (t.getId() == t2.getId()) {
                        existingIterator.remove();
                        this.tracks.add(0, t);

                        // break out of the while loop
                        break;
                    }
                }

                // if wasn't found, add to end
                if (!found) {
                    this.tracks.add(t);
                }
            }
        }
    }
}
