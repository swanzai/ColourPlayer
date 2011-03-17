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

import data.GlazedDatabaseDao;
import data.PropertiesLoader;
import exceptions.DataAccessException;
import glazedlists.EventListContainer;
import glazedlists.comparators.DefaultTrackComparator;
import glazedlists.filterators.GeneralTrackFilterator;
import glazedlists.filterators.TextFilteratorListener;
import glazedlists.matchereditors.ColourMatcherEditor;
import glazedlists.matchereditors.MyTextWidgetMatcherEditor;
import helpers.StringUtilities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import models.MetaData;
import models.PlayerCallBackData;
import models.PlaylistDao;
import models.SubMenuItem;
import models.Track;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import som.Som;
import views.AlbumArtCanvas;
import views.ColourPickerCanvasWrapper;
import views.ColourPickerContextMenu;
import views.GlazedAlbumList;
import views.GlazedArtistList;
import views.GlazedMainTable;
import views.MainView;
import views.PlayerListener;
import views.PlaylistTableContextMenu;
import views.SomCanvasWrapper;
import views.StyledSongInfo;
import views.SubSectionList;
import views.TableContextMenu;
import views.VolumeBar;
import actions.AboutAction;
import actions.ExitAction;
import actions.FileScanningAction;
import actions.NextAction;
import actions.OnlineHelpAction;
import actions.PlayAction;
import actions.PreviousAction;
import actions.ProfileSubmitterTask;
import actions.RemoveOrphansAction;
import actions.ShowPreferencesDialogAction;
import actions.ShuffleToggleAction;
import actions.StopAction;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import config.Constants;

public class MainController {
    /**
     * Listens for mouse and enter key events on the given list, and starts
     * playback when the events are fired
     * 
     * @author Michael Voong
     */
    class FilteringListListener extends MouseAdapter implements KeyListener {
        private final EventList<Track> eventList;

        public FilteringListListener(EventListContainer<Track> container) {
            this.eventList = container.getEventList();
        }

        public void keyPressed(KeyEvent e) {
            if (e.character == 13) {
                selected();
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void mouseDoubleClick(MouseEvent e) {
            selected();
        }

        private void selected() {
            try {
                nowPlaying.clear();

                for (Track track : this.eventList) {
                    nowPlaying.addTrack(track);
                }
            } catch (DataAccessException ex) {
                ex.printStackTrace();
            }
            playAction.play(true, true);
        }
    }

    /**
     * Listens for mouse double click events and enter key events on the table
     * given in the constructor. These events trigger playback of tracks
     * selected in the table.
     * 
     * @author Michael Voong
     */
    class MainTableListener extends MouseAdapter implements KeyListener {
        private Table table;
        private final boolean isNowPlaying;
        private EventList<Track> eventList;

        public MainTableListener(Table table, boolean isNowPlaying) {
            this.table = table;
            this.isNowPlaying = isNowPlaying;

            if (!isNowPlaying) {
                this.eventList = glazedMainTable.getEventList();
            } else {
                this.eventList = nowPlaying.getEventList();
            }
        }

        public void keyPressed(KeyEvent e) {
            if (e.character == 13) {
                playSelected();
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void mouseDoubleClick(MouseEvent e) {
            playSelected();
        }

        private void playSelected() {
            int[] indices = table.getSelectionIndices();

            if (indices.length > 0) {
                try {
                    if (!isNowPlaying) {
                        nowPlaying.clear();

                        Track track;
                        ArrayList<Track> newTracks = new ArrayList<Track>(this.eventList.size() - indices[0]);

                        for (int i = indices[0]; i < this.eventList.size(); i++) {
                            newTracks.add(this.eventList.get(i));
                            nowPlaying.addTrackToDao(this.eventList.get(i));
                        }

                        nowPlaying.addAllTracks(newTracks);
                    } else {
                        nowPlaying.setPointer(indices[0]);
                    }
                } catch (DataAccessException dae) {
                    MessageDialog.openWarning(shell, "Error Adding Track", "There was an error adding the track to the playlist");
                }

                table.deselectAll();
                playAction.play(true, true);
            }
        }
    }

    /**
     * Listens to key events on the som canvas and adjusts the playback playlist
     * accordingly and starts playing.
     * 
     * @author Michael Voong
     */
    class SomCanvasKeyListener implements KeyListener {
        public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
            if (e.keyCode == 13) {
                try {
                    nowPlaying.clear();
                    EventList<Track> eventList = glazedMainTable.getEventList();

                    nowPlaying.addAllTracks(eventList);
                    for (int i = 0; i < eventList.size(); i++) {
                        nowPlaying.addTrackToDao(eventList.get(i));
                    }
                } catch (DataAccessException dae) {
                    dae.printStackTrace();
                }

                glazedMainTable.getTable().deselectAll();
                playAction.play(true, true);
            }
        }

        public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
        }
    }

    /**
     * Handles selections on the main section list and changes the view
     * accordingly to show the appropriate playlist
     * 
     * @author Michael Voong
     */
    class SubLibrarySelectionListener implements Listener {
        final Table subSectTable;
        final SubSectionList subSection;
        final Color COLOR_LIST_FG = Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
        final Color COLOR_LIST_BG = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        final Color COLOR_WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
        final Color COLOR_DARK_GRAY = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);

        public SubLibrarySelectionListener() {
            this.subSectTable = view.getSubSectList().getTable();
            this.subSection = view.getSubSectList();
            showItem(0);
        }

        public void handleEvent(Event event) {
            final EventList<SubMenuItem> menuItems = subSection.getEventList();

            int index = subSectTable.getSelectionIndex();
            final PlaylistDao playlist = menuItems.get(index).getPlaylist();

            if (playlist != null) {
                glazedMainTable.showTable(GlazedMainTable.PLAYLIST_TABLE);
                view.getTopSashComposite().setVisible(false);
                hideItem(0);
                showItem(1);
                view.getSearchField().setEnabled(false);
            } else {
                glazedMainTable.showTable(GlazedMainTable.MAIN_TABLE);
                view.getTopSashComposite().setVisible(true);
                hideItem(1);
                showItem(0);
                view.getSearchField().setEnabled(true);
            }

            subSectTable.deselectAll();
            view.getMainSash().layout();
        }

        private void hideItem(int index) {
            subSectTable.getItem(index).setForeground(COLOR_LIST_FG);
            subSectTable.getItem(index).setBackground(COLOR_LIST_BG);
        }

        private void showItem(int index) {
            subSectTable.getItem(index).setForeground(COLOR_WHITE);
            subSectTable.getItem(index).setBackground(COLOR_DARK_GRAY);
        }
    }

    /**
     * Changes the window title on playback events
     * 
     * @author Michael Voong
     */
    class TitleChangingPlayerListener implements PlayerListener {
        public void callback(PlayerCallBackData data) {
        }

        public void message(String message, Player player) {
            if (message.equals("playing")) {
                final MetaData data = player.getMetaData();
                if (data != null) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            shell.setText(StringUtilities.escapeAmpersands(data.getArtist() + " - " + data.getTitle()));
                        };
                    });

                }
            } else if (message.equals("stopped")) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        shell.setText(Constants.PROGRAM_NAME + " " + Constants.VERSION);
                    }
                });
            }
        }
    }

    // widget items
    private HashMap<String, Widget> widgetRegistry;
    private HashMap<String, MenuManager> menuRegistry;
    private MainView view;
    private TableContextMenu tableContextMenu;

    // models
    private PlaylistDao nowPlaying;

    // listeners/actions
    private PlayAction playAction;
    private NextAction nextAction;
    private PreviousAction previousAction;

    private StopAction stopAction;
    private IAction shuffleToggleAction;
    private FileScanningAction fileScanningAction;
    private TrackAssociatingColourPickerListener trackAssociatingColourPickerListener;
    private SomRetrainListener somRetrainListener;

    // controllers
    private Som som;
    private PropertiesLoader propertiesLoader;
    private ProfileSubmitterTask profileSubmitterTask;
    private Player player;
    private GlazedDatabaseDao dao;
    // other
    private Timer profileSubmitter;

    private ShowColourAssociationWizardAction colourAssociationWizardAction;
    private GlazedMainTable glazedMainTable;
    private GlazedArtistList glazedArtistList;
    private GlazedAlbumList glazedAlbumList;
    private ColourMatcherEditor colourMatcher;
    private GeneralTrackFilterator searchFilterator;
    private MyTextWidgetMatcherEditor searchMatcherEditor;

    private FilterList<Track> colourMatchedList;
    private FilterList<Track> playlistMatchedList;
    private SortedList<Track> sortedList;
    private PlaylistStarvingFeeder playlistFeeder;
    private PlaylistTableContextMenu playlistTableContextmenu;
    private Shell shell;

    public MainController(PropertiesLoader propertiesLoader, Player player, GlazedDatabaseDao dao, MainView view) throws DataAccessException {
        this.propertiesLoader = propertiesLoader;
        this.player = player;
        this.dao = dao;
        this.widgetRegistry = view.getWidgetRegistry();
        this.menuRegistry = view.getMenuRegistry();
        this.shell = view.getShell();
        this.view = view;
        this.nowPlaying = new PlaylistDao(dao, "Now Playing");
        this.glazedMainTable = view.getGlazedMainTable();
        this.glazedArtistList = view.getGlazedArtistList();
        this.glazedAlbumList = view.getGlazedAlbumList();

        initProfileSubmittingTimer();
        initActions();
        initDragAndDrop();
        initData();
        initListeners();
        shareControllers();
    }

    public void cleanUp() {
        view.cleanUp();
        colourAssociationWizardAction.cleanUp();
    }

    /**
     * Create a new som and train it using the default settings.
     */
    public void trainSom() {
        this.som = new Som(dao.getColouredTracksEventList());

        somRetrainListener.setSom(som);
        view.setSom(som);

        som.init();
        som.train(Som.INITIAL_LEARNING_RATE, Som.INITIAL_MAP_RADIUS);

        playlistFeeder.setSom(som);
    }

    /**
     * Initialise actions which are shared throughout the controller.
     */
    private void initActions() {
        this.fileScanningAction = new FileScanningAction(player, dao, shell, glazedMainTable);
        this.shuffleToggleAction = new ShuffleToggleAction(nowPlaying, propertiesLoader);

        try {
            // playback button actions
            Button playButton = (Button) widgetRegistry.get("playButton");
            playAction = new PlayAction(playButton, player, nowPlaying, fileScanningAction);

            Button nextButton = (Button) widgetRegistry.get("nextButton");
            nextAction = new NextAction(nextButton, player, nowPlaying, playAction);

            Button previousButton = (Button) widgetRegistry.get("previousButton");
            previousAction = new PreviousAction(previousButton, player, nowPlaying, playAction);

            Button stopButton = (Button) widgetRegistry.get("stopButton");
            stopAction = new StopAction(stopButton, player, nowPlaying, playAction);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        final MenuManager fileMenu = menuRegistry.get("file");

        fileMenu.add(fileScanningAction);
        fileMenu.add(new ExitAction(view));
        // fileMenu.add(new StreamOpenAction(nowPlaying, new
        // MusicScanner(player,
        // dao), playAction));

        final MenuManager playbackMenu = menuRegistry.get("playback");
        playbackMenu.add(shuffleToggleAction);
        playbackMenu.add(new Separator());
        playbackMenu.add(stopAction);
        playbackMenu.add(playAction);
        playbackMenu.add(previousAction);
        playbackMenu.add(nextAction);

        final MenuManager helpMenu = menuRegistry.get("help");
        helpMenu.add(new OnlineHelpAction());
        helpMenu.add(new AboutAction(shell));

        final MenuManager toolsMenu = menuRegistry.get("tools");
        // toolsMenu.add(new ShowProfileSubmittingDialogAction(shell,
        // mainTableModel, Integer.parseInt(propertiesLoader
        // .getProperties().get("unique_id").toString())));

        toolsMenu.add(colourAssociationWizardAction = new ShowColourAssociationWizardAction(shell, player, propertiesLoader));

        toolsMenu.add(new RemoveOrphansAction(dao));
        toolsMenu.add(new Separator());
        toolsMenu.add(new ShowPreferencesDialogAction(shell, propertiesLoader));

        // we need to call this in order for the menu to reflect the new changes
        menuRegistry.get("root").update(true);

        // search arrow menu
        final Button arrow = (Button) widgetRegistry.get("searchOptionArrow");
        final Menu menu = new Menu(arrow);
        final MenuItem allItem = new MenuItem(menu, SWT.CHECK);
        final MenuItem artistItem = new MenuItem(menu, SWT.CHECK);
        final MenuItem albumItem = new MenuItem(menu, SWT.CHECK);
        final MenuItem titleItem = new MenuItem(menu, SWT.CHECK);

        allItem.setText("All");
        allItem.setSelection(true);
        artistItem.setText("Artist");
        albumItem.setText("Album");
        titleItem.setText("Title");

        allItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                searchFilterator.clearFilters();
                allItem.setSelection(true);
                artistItem.setSelection(false);
                albumItem.setSelection(false);
                titleItem.setSelection(false);
            };
        });

        artistItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                searchFilterator.clearFilters();
                allItem.setSelection(false);
                artistItem.setSelection(true);
                albumItem.setSelection(false);
                titleItem.setSelection(false);
                searchFilterator.setArtistFilter(true);
            };
        });
        albumItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                searchFilterator.clearFilters();
                allItem.setSelection(false);
                artistItem.setSelection(false);
                albumItem.setSelection(true);
                titleItem.setSelection(false);
                searchFilterator.setAlbumFilter(true);
            };
        });
        titleItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                searchFilterator.clearFilters();
                allItem.setSelection(false);
                artistItem.setSelection(false);
                albumItem.setSelection(false);
                titleItem.setSelection(true);
                searchFilterator.setTitleFilter(true);
            };
        });

        arrow.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                menu.setVisible(true);
            };
        });
    }

    /**
     * Initialise the data views and GlazedLists that they use.
     * 
     * Flow of filters: <br />
     * eventList > sortedList > colourMatchedList > textFilteredList >
     * artistList > albumList
     */
    private void initData() {
        final EventList<Track> eventList = dao.getTracksEventList();
        final Text searchField = view.getSearchField();

        this.sortedList = new SortedList<Track>(eventList, new DefaultTrackComparator());
        this.colourMatcher = new ColourMatcherEditor();
        this.colourMatchedList = new FilterList<Track>(sortedList, colourMatcher);
        this.searchFilterator = new GeneralTrackFilterator();
        this.searchMatcherEditor = new MyTextWidgetMatcherEditor(searchField, searchFilterator);

        FilterList<Track> textFilteredList = new FilterList<Track>(colourMatchedList, searchMatcherEditor);

        glazedArtistList.init(textFilteredList);
        glazedAlbumList.init(glazedArtistList.getEventList(), glazedArtistList.getMatcherEditor());
        glazedMainTable.initMain(glazedAlbumList.getEventList(), sortedList);
        glazedMainTable.initNowPlaying(nowPlaying.getEventList());

        // add listener to colour map list so that when it's modified, the other
        // more specific filters are reset
        colourMatcher.addMatcherEditorListener(new MatcherEditor.Listener() {
            public void changedMatcher(ca.odell.glazedlists.matchers.MatcherEditor.Event matcherEvent) {
                glazedArtistList.getMatcherEditor().setFilterText(new String[] { "" });
                glazedAlbumList.getMatcherEditor().setFilterText(new String[] { "" });
            }
        });

        searchMatcherEditor.addMatcherEditorListener(new MatcherEditor.Listener() {
            public void changedMatcher(ca.odell.glazedlists.matchers.MatcherEditor.Event matcherEvent) {
                glazedArtistList.getMatcherEditor().setFilterText(new String[] { "" });
                glazedAlbumList.getMatcherEditor().setFilterText(new String[] { "" });
            }
        });

        view.getPickerCanvasWrapper().setEventList(glazedMainTable.getEventList());

        // initialise left nav table
        view.getSubSectList().initItems(nowPlaying);
    }

    /**
     * Initialise drag and drop listeners.
     */
    private void initDragAndDrop() {
        /*
         * File transfers
         * 
         */
        Transfer[] types = new Transfer[] { FileTransfer.getInstance() };
        int operations = DND.DROP_COPY | DND.DROP_LINK | DND.DROP_MOVE;

        // main table transfer (drag any file from windows
        final Table mainTable = (Table) widgetRegistry.get("mainTable");

        DropTarget target = new DropTarget(mainTable, operations);
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                event.operations = DND.DROP_COPY;
            }

            public void drop(DropTargetEvent event) {
                if (event.data == null) {
                    event.detail = DND.DROP_NONE;
                    return;
                }
                String[] files = (String[]) event.data;
                fileScanningAction.scanFiles(files);
            }
        });
    }

    /**
     * Initialise listeners which are mainly attached to the view.
     */
    private void initListeners() {
        final Table table = glazedMainTable.getTable();
        final EventList<Track> mainTableEventList = glazedMainTable.getEventList();
        final Table playlistTable = glazedMainTable.getNowPlayingTable();
        final Table leftMainTable = (Table) widgetRegistry.get("leftMainTable");
        final Text searchField = (Text) widgetRegistry.get("searchField");
        final List albumList = glazedAlbumList.getList();
        final List artistList = glazedArtistList.getList();
        final SomCanvasWrapper somCanvasWrapper = view.getSomCanvas();
        final StyledSongInfo songInfo = view.getStyledSongInfo();
        final TrackProgressBar trackProgressBar = (TrackProgressBar) widgetRegistry.get("trackProgressBar");
        final ColourPickerCanvasWrapper picker = view.getPickerCanvasWrapper();
        final Table subSectTable = view.getSubSectList().getTable();

        /**
         * Main table listeners
         */
        final MainTableListener mainTableListener = new MainTableListener(table, false);
        table.addMouseListener(mainTableListener);
        table.addKeyListener(mainTableListener);

        table.addKeyListener(new KeyListener() {
            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                if (e.character == 0x01) {
                    table.selectAll();
                } else if (e.character == 0x04) {
                    table.deselectAll();
                    if (nowPlaying.getTrack() != null)
                        picker.setReferenceTrack(nowPlaying.getTrack());
                }
            };

            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
            };
        });

        // table context menu
        tableContextMenu = new TableContextMenu(shell, table, dao, nowPlaying);
        tableContextMenu.initItems(glazedMainTable.getEventList());

        // add listener so that picker is notified when table is selected
        table.addSelectionListener(picker);

        /**
         * Playlist table listeners
         */
        final MainTableListener playlistTableListener = new MainTableListener(playlistTable, true);
        playlistTable.addMouseListener(playlistTableListener);
        playlistTable.addKeyListener(playlistTableListener);

        // context menu
        playlistTableContextmenu = new PlaylistTableContextMenu(shell, playlistTable, dao, nowPlaying);
        playlistTableContextmenu.initItems(glazedMainTable.getPlaylistEventList());

        /**
         * Player listeners
         */
        // album art listener
        player.addListener(view.getAlbumArt());

        // updates song info label when current track changes
        player.addListener(songInfo);

        // track progress bar
        player.addListener(trackProgressBar);
        player.addListener(new TitleChangingPlayerListener());

        // colour picker listeners
        player.addListener(picker);

        // add this listener to the end so track advances AFTER all other
        // listeners have been notified
        player.addListener(new TrackChangeController(nowPlaying, playAction));

        /**
         * Filtering listeners
         */
        // filtering tables double click listeners
        final FilteringListListener albumListener = new FilteringListListener(glazedAlbumList);
        albumList.addMouseListener(albumListener);
        albumList.addKeyListener(albumListener);

        final FilteringListListener artistListener = new FilteringListListener(glazedArtistList);
        artistList.addMouseListener(artistListener);
        artistList.addKeyListener(artistListener);

        // som listener
        somCanvasWrapper.getCanvas().addKeyListener(new SomCanvasKeyListener());

        // listener to force an update of filter whenever filter options are
        // changed (e.g. all, album, title)
        searchFilterator.addTextFilteratorListener(new TextFilteratorListener() {
            public void filteratorChanged() {
                searchMatcherEditor.setForceRefilter(true);
                searchMatcherEditor.getFilterSelectionListener().widgetSelected(null);
                searchMatcherEditor.setForceRefilter(false);
            };
        });

        /**
         * Playlist listeners
         */
        somCanvasWrapper.setPlaylist(nowPlaying);

        // listener for when playlist starves and wants more tracks
        nowPlaying.addPlaylistStarvingListener(playlistFeeder = new PlaylistStarvingFeeder(somCanvasWrapper.getSomTraverser(), sortedList, nowPlaying));

        /**
         * Colour picker listeners
         */
        trackAssociatingColourPickerListener = new TrackAssociatingColourPickerListener(nowPlaying, dao, mainTableEventList);
        picker.addListener(trackAssociatingColourPickerListener);
        trackAssociatingColourPickerListener.addColourAssociationListener(somRetrainListener = new SomRetrainListener(dao));

        // picker context menu
        new ColourPickerContextMenu(picker, nowPlaying);

        // assign listener that updates the profile submitter task when a track
        // has been associated
        trackAssociatingColourPickerListener.addColourAssociationListener(profileSubmitterTask);

        // sub (left) menu clicking
        subSectTable.addListener(SWT.Selection, new SubLibrarySelectionListener());
    }

    /**
     * Initialise the timer that submits association data to the server every 10
     * seconds
     */
    private void initProfileSubmittingTimer() {
        profileSubmitter = new Timer("profile_submitter", true);
        profileSubmitterTask = new ProfileSubmitterTask(propertiesLoader.getProperties());

        // schedule every minute
        profileSubmitter.scheduleAtFixedRate(profileSubmitterTask, 10000, 10000);
    }

    /**
     * Set the controllers on the required views
     */
    private void shareControllers() {
        final AlbumArtCanvas albumArt = (AlbumArtCanvas) widgetRegistry.get("albumArt");
        final VolumeBar volumeBar = (VolumeBar) widgetRegistry.get("volumeBar");
        final ColourPickerCanvasWrapper picker = view.getPickerCanvasWrapper();
        final SomCanvasWrapper somCanvasWrapper = view.getSomCanvas();
        final TrackProgressBar progress = (TrackProgressBar) widgetRegistry.get("trackProgressBar");

        volumeBar.setPlayer(player);
        picker.setPlaylist(nowPlaying);
        somCanvasWrapper.setColourMatcherEditor(colourMatcher);
        albumArt.setPropertiesLoader(propertiesLoader);
        glazedMainTable.setNowPlaying(nowPlaying);
        glazedMainTable.setPlayer(player);
        progress.setPlayer(player);
    }
}
