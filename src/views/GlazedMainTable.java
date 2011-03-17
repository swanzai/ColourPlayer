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

import glazedlists.TrackTableFormat;
import helpers.ColourRegistry;
import models.PlayerCallBackData;
import models.PlaylistDao;
import models.Track;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swt.EventTableViewer;
import ca.odell.glazedlists.swt.TableComparatorChooser;
import controllers.Player;
import data.GlazedDatabaseDao;

public class GlazedMainTable extends ColourRegistry implements PlayerListener {
    public static final int PLAYLIST_TABLE = 42342;
    public static final int MAIN_TABLE = 234234;

    private Table table;
    private GlazedDatabaseDao dao;
    private EventList<Track> eventList;
    // private HashMap<Track, Integer> trackHashMap = new HashMap<Track,
    // Integer>();
    private TableItem lastHighlightedItem;
    private TableComparatorChooser tableSorter;
    private SortedList<Track> sortedSource;
    private EventTableViewer tableViewer;
    private PlaylistDao nowPlaying;
    private Player player;

    private Runnable redrawingTask2;
    private Runnable redrawingTask;

    private EventList<Track> nowPlayingEventList;
    private EventTableViewer playlistTableViewer;
    private Table playlistTable;
    private StackLayout stackLayout;
    private Composite stackComposite;
    // private TableRedrawingTask redrawingTask2;

    private TableItem lastItem = null;
    private TableItem lastItemPlaylist = null;

    protected boolean redrawEnabled = true;

    public GlazedMainTable(Composite parent) {
        stackComposite = new Composite(parent, SWT.NONE);
        stackComposite.setLayout(stackLayout = new StackLayout());
        this.table = new Table(stackComposite, SWT.VIRTUAL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        this.playlistTable = new Table(stackComposite, SWT.VIRTUAL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);

        stackLayout.topControl = table;
        // table.addListener(SWT.SetData, this);
    }

    public void showTable(int table) {
        if (table == GlazedMainTable.MAIN_TABLE) {
            stackLayout.topControl = this.table;
        } else if (table == GlazedMainTable.PLAYLIST_TABLE) {
            stackLayout.topControl = this.playlistTable;
        }

        stackComposite.layout();
    }

    public void message(String message, Player player) {
        if (message.equals("playing")) {
            // highlight track
            if (nowPlaying != null && nowPlaying.getTrack() != null) {
                Track track = nowPlaying.getTrack();
                int index = eventList.indexOf(track);

                System.out.println("Last item: " + lastItem);
                if (index > -1) {
                    TableItem item = table.getItem(index);
                    highlightItem(item, MAIN_TABLE);
                }

                // now playing table
                index = nowPlayingEventList.indexOf(track);

                System.out.println("Last item pls: " + lastItemPlaylist);
                if (index > -1) {
                    TableItem item = playlistTable.getItem(index);
                    highlightItem(item, PLAYLIST_TABLE);
                }
            }
        } else if (message.equals("stopped")) {
            if (lastItem != null && !lastItem.isDisposed()) {
                // FIX THIS!
            }
        }
    }

    //
    // /* Handles the table item modification the first time they are made
    // *
    // * @see
    // org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
    // */
    // public void handleEvent(Event e) {
    // TableItem item = (TableItem) e.item;
    // int index = table.indexOf(item);
    //
    // if (index == -1)
    // return;
    // if (eventList.size() - 1 < index)
    // return;
    //
    // Track track = eventList.get(index);
    //
    // Color color = Display.getDefault().getSystemColor(
    // SWT.COLOR_LIST_BACKGROUND);
    // item.setForeground(0, color);
    // }

    public void setPlayer(Player player) {
        this.player = player;
        player.addListener(this);
    }

    public void initMain(EventList<Track> source, SortedList<Track> sortedSource) {
        this.eventList = source;

        tableViewer = new EventTableViewer(source, table, new TrackTableFormat());

        tableSorter = new TableComparatorChooser(tableViewer, sortedSource, false);
        this.sortedSource = sortedSource;

        initColumns(table);
        redrawingTask = new TableRedrawingTask(table, eventList);
        // new Timer(true).schedule(redrawTimerTask, 0, 500);
        redrawingTask.run();

        eventList.addListEventListener(new ListEventListener() {
            public void listChanged(ca.odell.glazedlists.event.ListEvent listChanges) {
                if (redrawEnabled)

                    redrawingTask.run();
            }
        });

        // table.addListener(SWT.SetData, new Listener() {
        // public void handleEvent(Event event) {
        // TableItem item = (TableItem) event.item;
        // int index = table.indexOf(item);
        // Track track = eventList.get(index);
        //
        // if (track.getColour() != null) {
        // updateColourBackground(item, track.getColour());
        // table.redraw();
        // }
        // }
        // });
    }

    public void initNowPlaying(EventList<Track> source) {
        this.nowPlayingEventList = source;

        playlistTableViewer = new EventTableViewer(source, playlistTable, new TrackTableFormat());
        initColumns(playlistTable);

        redrawingTask2 = new TableRedrawingTask(playlistTable, nowPlayingEventList);
        // new Timer(true).schedule(redrawTimerTask, 0, 500);
        redrawingTask2.run();

        nowPlayingEventList.addListEventListener(new ListEventListener() {
            public void listChanged(ca.odell.glazedlists.event.ListEvent listChanges) {
                if (redrawEnabled)
                    redrawingTask2.run();
            };
        });

        // playlistTable.addListener(SWT.SetData, new Listener() {
        // public void handleEvent(Event event) {
        // TableItem item = (TableItem) event.item;
        // int index = playlistTable.indexOf(item);
        // Track track = nowPlayingEventList.get(index);
        //
        // if (track.getColour() != null) {
        // updateColourBackground(item, track.getColour());
        // playlistTable.redraw();
        // }
        // }
        // });
    }

    private void highlightItem(TableItem item, int type) {
        TableItem lastItem = (type == MAIN_TABLE) ? this.lastItem : this.lastItemPlaylist;

        if (lastItem != null && !lastItem.isDisposed()) {
            lastItem.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
            lastItem.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        }

        if (type == MAIN_TABLE) {
            this.lastItem = item;
        } else {
            this.lastItemPlaylist = item;
        }

        if (item == null || item.isDisposed())
            return;

        item.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        item.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
    }

    public void updateColourBackground(TableItem item, int[] colour) {
        if (item == null || item.isDisposed()) {
            return;
        }

        if (colour != null) {
            Color colourObj = hitCache(colour);

            item.setBackground(0, colourObj);
            item.setForeground(0, colourObj);
        } else {
            Color color = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
            item.setBackground(0, color);
            item.setForeground(0, color);
        }
    }

    public void cleanUp() {
        super.cleanUp();
    }

    private void initColumns(Table table) {
        TableColumn[] cols = table.getColumns();
        cols[0].setWidth(30);
        cols[1].setWidth(150);
        cols[2].setWidth(60);
        cols[3].setWidth(150);
        cols[4].setWidth(150);
        cols[5].setWidth(60);

        for (TableColumn col : cols) {
            col.setMoveable(true);
        }
    }

    public Table getTable() {
        return table;
    }

    public void setNowPlaying(PlaylistDao nowPlaying) {
        this.nowPlaying = nowPlaying;
    }

    public EventList<Track> getEventList() {
        return eventList;
    }

    public SortedList<Track> getSortedSource() {
        return sortedSource;
    }

    public void callback(PlayerCallBackData data) {
    }

    class TableRedrawingTask implements Runnable {
        private final Table table;
        private final EventList<Track> eventList;

        public TableRedrawingTask(Table table, EventList<Track> eventList) {
            this.table = table;
            this.eventList = eventList;
        }

        public void run() {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    Shell shell = Display.getDefault().getActiveShell();
                    if (shell == null)
                        return;

                    // glass cursor
                    Cursor cursor = Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT);
                    shell.setCursor(cursor);

                    Color bg = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
                    Color bg2 = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
                    TableItem[] items = table.getItems();

                    int index;
                    Track track;
                    Color background;
                    for (TableItem item : items) {
                        index = table.indexOf(item);

                        if (index < eventList.size()) {
                            track = eventList.get(index);
                            background = item.getBackground();

                            if ((background.getRed() == bg.getRed() && background.getGreen() == bg.getGreen() && background.getBlue() == bg.getBlue())
                                    || (background.getRed() == bg2.getRed() && background.getGreen() == bg2.getGreen() && background.getBlue() == bg2.getBlue())
                                    && track.getColour() != null) {
                                updateColourBackground(item, track.getColour());
                            }
                        }
                    }

                    shell.setCursor(null);
                };
            });
        }
    }

    public Table getNowPlayingTable() {
        return playlistTable;
    }

    public EventList<Track> getPlaylistEventList() {
        return nowPlayingEventList;
    }

    public boolean isRedrawEnabled() {
        return redrawEnabled;
    }

    public void setRedrawEnabled(boolean redrawEnabled) {
        this.redrawEnabled = redrawEnabled;
    }
}