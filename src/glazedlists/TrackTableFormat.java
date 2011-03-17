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

package glazedlists;

import glazedlists.comparators.ColourComparator;
import glazedlists.comparators.IntegerComparator;
import glazedlists.comparators.StringComparator;
import helpers.StringUtilities;

import java.util.Comparator;

import models.Track;
import ca.odell.glazedlists.gui.AdvancedTableFormat;

public class TrackTableFormat implements AdvancedTableFormat {
    public Class getColumnClass(int col) {
        if (col == 0) {
            return BlankStringColour.class;
        }
        if (col == 2)
            return Integer.class;
        else
            return Object.class;
    }

    public Comparator getColumnComparator(int col) {
        if (col == 2)
            return new IntegerComparator();
        else if (col == 0)
            return new ColourComparator();

        return new StringComparator();
    }

    String[] cols = { "", "Name", "Track #", "Artist", "Album", "Duration", "Location" };

    public int getColumnCount() {
        return cols.length;
    }

    public String getColumnName(int arg0) {
        return cols[arg0];
    }

    public Object getColumnValue(Object obj, int col) {
        Track track = (Track) obj;
        switch (col) {
        case 0:
            return new BlankStringColour(track.getColour());
        case 1:
            return track.getTitle();
        case 2:
            return new Integer(track.getTrackNo());
        case 3:
            return track.getArtist();
        case 4:
            return track.getAlbum();
        case 5:
            return StringUtilities.msToString(track.getDuration());
        case 6:
            return track.getPath();
        }

        return "";
    }
}