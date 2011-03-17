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

package views;

import glazedlists.EventListContainer;
import glazedlists.comparators.EnglishComparator;
import glazedlists.filterators.ArtistTrackFilterator;
import glazedlists.matchereditors.ListWidgetMatcherEditor;
import glazedlists.transformers.ArtistTrackTransformedList;

import java.util.Comparator;

import models.Track;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swt.EventListViewer;

public class GlazedArtistList extends GlazedFilterList implements EventListContainer<Track> {
    private EventList<Track> eventList;

    public GlazedArtistList(Composite parent) {
        super(parent);
    }

    public void init(EventList<Track> source) {
        Comparator englishComparator = new EnglishComparator();

        // add list selection filter to eventlist
        matcherEditor = new ListWidgetMatcherEditor(list,
                new ArtistTrackFilterator());
        this.eventList = new FilterList<Track>(source, matcherEditor);

        // eventlist to present
        SortedList<String> sortedList = new SortedList<String>(
                new UniqueList<String>(new ArtistTrackTransformedList(source)),
                englishComparator);

        EventListViewer artistListViewer = new EventListViewer(sortedList, list);
    }

    public EventList<Track> getEventList() {
        return eventList;
    }

    public TextMatcherEditor getMatcherEditor() {
        return matcherEditor;
    }
    
    public List getList() {
        return list;
    }

    
    
    @Override
    protected String getClearListLabel() {
        return "Show All Artists";
    }
}
