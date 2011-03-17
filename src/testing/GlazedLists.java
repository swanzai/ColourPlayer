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

package testing;

import java.util.Comparator;
import java.util.List;

import models.Track;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.swt.EventListViewer;
import ca.odell.glazedlists.swt.EventTableViewer;
import ca.odell.glazedlists.swt.TableComparatorChooser;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;
import data.Database;
import data.DatabaseDao;
import exceptions.DataAccessException;
import glazedlists.TrackTableFormat;
import glazedlists.comparators.DefaultTrackComparator;
import glazedlists.comparators.EnglishComparator;
import glazedlists.filterators.AlbumTrackFilterator;
import glazedlists.filterators.ArtistTrackFilterator;
import glazedlists.filterators.GeneralTrackFilterator;
import glazedlists.matchereditors.ListWidgetMatcherEditor;
import glazedlists.transformers.AlbumTrackTransformedList;
import glazedlists.transformers.ArtistTrackTransformedList;

public class GlazedLists extends ApplicationWindow {
    private DatabaseDao dao;

    public GlazedLists(Shell shell) {
        super(shell);

        try {
            dao = new DatabaseDao();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        getShell().setSize(500, 500);

        Composite composite = new Composite(parent, SWT.NONE);

        composite.setLayout(new FillLayout());

        try {
            init(composite);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parent;
    }

    private void init(Composite parent) throws DataAccessException {
        // text
        Text text = new Text(parent, SWT.BORDER);

        // list
        org.eclipse.swt.widgets.List artistList = new org.eclipse.swt.widgets.List(
                parent, SWT.V_SCROLL);
        org.eclipse.swt.widgets.List albumList = new org.eclipse.swt.widgets.List(
                parent, SWT.V_SCROLL);

        // table
        Table table = new Table(parent, SWT.V_SCROLL | SWT.VIRTUAL);

        // get tracks from db
        List<Track> tracks = dao.getTracks(0, 0);

        // create event list
        EventList<Track> eventList = new BasicEventList<Track>();
        eventList.addAll(tracks);

        // sorted list
        SortedList<Track> sortedList = new SortedList<Track>(eventList,
                new DefaultTrackComparator());

        // filtered list by text box
        FilterList<Track> filterList = new FilterList<Track>(sortedList,
                new TextWidgetMatcherEditor(text, new GeneralTrackFilterator()));

        // filtered list by artist list
        FilterList<Track> artistMatchedFilterList = new FilterList<Track>(
                filterList, new ListWidgetMatcherEditor(artistList,
                        new ArtistTrackFilterator()));

        // filtered list
        FilterList<Track> albumArtistMatchedFilterList = new FilterList<Track>(
                artistMatchedFilterList, new ListWidgetMatcherEditor(albumList,
                        new AlbumTrackFilterator()));

        // apply transformations
        Comparator englishComparator =  new EnglishComparator();
        TransformedList artistFilteredList = new SortedList<String>(new UniqueList<String>(
                new ArtistTrackTransformedList(filterList)), englishComparator);

        TransformedList albumFilteredList = new SortedList<String>(new UniqueList<String>(
                new AlbumTrackTransformedList(artistMatchedFilterList)), englishComparator);

        // //filter album list by artist
        // FilterList<Track> albumFilteredList2 = new FilterList<Track>(
        // albumFilteredList, new ListWidgetMatcherEditor(artistList,
        // new StringFilterator()));

        // apply to list
        EventListViewer artistListViewer = new EventListViewer(
                artistFilteredList, artistList);
        EventListViewer albumListViewer = new EventListViewer(
                albumFilteredList, albumList);

        // table viewer
        EventTableViewer tableViewer = new EventTableViewer(
                albumArtistMatchedFilterList, table, new TrackTableFormat());

        TableComparatorChooser tableSorter = new TableComparatorChooser(
                tableViewer, sortedList, false);
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        ApplicationWindow w = new GlazedLists(shell);
        w.setBlockOnOpen(true);
        w.open();
        shell.dispose();
        display.dispose();
        Database.cleanUp();
    }
}