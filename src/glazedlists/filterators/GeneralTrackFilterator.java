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

package glazedlists.filterators;

import java.util.ArrayList;
import java.util.List;

import models.Track;
import ca.odell.glazedlists.TextFilterator;

public class GeneralTrackFilterator implements TextFilterator<Track> {
    private boolean allFilter = true;
    private boolean artistFilter = false;
    private boolean albumFilter = false;
    private boolean titleFilter = false;

    private List<TextFilteratorListener> listeners = new ArrayList<TextFilteratorListener>();

    public void getFilterStrings(List<String> baseList, Track track) {
        if (allFilter || artistFilter)
            baseList.add(track.getArtist());
        if (allFilter || albumFilter)
            baseList.add(track.getAlbum());
        if (allFilter || titleFilter)
            baseList.add(track.getTitle());
    }

    public void setAllFilter(boolean allFilter) {
        this.allFilter = allFilter;
        fireFilterChanged();
    }

    public void setAlbumFilter(boolean albumFilter) {
        this.albumFilter = albumFilter;
        fireFilterChanged();
    }

    public void setArtistFilter(boolean artistFilter) {
        this.artistFilter = artistFilter;
        fireFilterChanged();
    }

    public void setTitleFilter(boolean titleFilter) {
        this.titleFilter = titleFilter;
        fireFilterChanged();
    }

    public void fireFilterChanged() {
        if (listeners != null)
            for (TextFilteratorListener l : listeners)
                l.filteratorChanged();
    }

    public void addTextFilteratorListener(TextFilteratorListener listener) {
        listeners.add(listener);
    }

    public void clearFilters() {
        allFilter = false;
        artistFilter = false;
        albumFilter = false;
        titleFilter = false;
    }
}