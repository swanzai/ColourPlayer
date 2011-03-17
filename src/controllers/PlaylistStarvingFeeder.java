package controllers;

import exceptions.DataAccessException;
import glazedlists.matchereditors.ColourMatcherEditor;

import java.util.HashSet;
import java.util.Random;

import listeners.PlaylistStarvingListener;
import models.PlaylistDao;
import models.Track;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import som.Som;
import som.SomNode;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;

public class PlaylistStarvingFeeder implements PlaylistStarvingListener {
    private final SomTraverser traverser;
    private Som som;
    private final EventList<Track> eventList;
    private final PlaylistDao playlist;
    private Random random;

    public PlaylistStarvingFeeder(SomTraverser traverser, EventList<Track> eventList, PlaylistDao playlist) {
        this.traverser = traverser;
        this.eventList = eventList;
        this.playlist = playlist;
    }

    public void playlistStarving(int fetchAmount) {
        if (som == null)
            return;

        System.out.println("Playlist starving. Give me more tracks!");
        ColourMatcherEditor colourMatcher = new ColourMatcherEditor();
        colourMatcher.setTolerance(50);
        EventList<Track> colourMatchedList = new FilterList<Track>(eventList, colourMatcher);

        // crop playlist if too long
        EventList<Track> plsEventList = playlist.getEventList();
        try {
            if (plsEventList.size() > 30) {
                for (int i = 0; i < plsEventList.size() - 30; i++) {
                    playlist.remove(0);
                }
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        boolean addedTrack = false;
        int i = 0;
        while (!addedTrack) {
            int[] pos = traverser.getNext();
            int[] pos2 = traverser.getNextPosition();
            traverser.fireTraverserChanged(); // redraw
            // whatever
            // needs
            // to be redrawn;
            HashSet<SomNode> matchItems = new HashSet<SomNode>();
            matchItems.add(som.getNetwork()[pos2[0]][pos2[1]]);
            colourMatcher.setSelectedNodes(matchItems);

            // get items from this list
            int noTracks = 0;
            try {
                HashSet<Track> triedHash = new HashSet<Track>();
                while (triedHash.size() < colourMatchedList.size()) {
                    int index = (int) (getRandom().nextDouble() * colourMatchedList.size());
                    Track t = colourMatchedList.get(index);

                    if (!triedHash.contains(t) && !playlist.getEventList().contains(t)) {
                        playlist.addTrack(t);
                        addedTrack = true;

                        if (++noTracks > 4) {
                            break;
                        }
                    }
                    triedHash.add(t);
                }
            } catch (DataAccessException e) {
                MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", "Error while adding track");
            }

            if (++i == 5) {
                System.out.println("Could not find any tracks!");
                break;
            }
        }
    }

    protected void setSom(Som som) {
        this.som = som;
    }

    private Random getRandom() {
        if (random == null)
            random = new Random();

        return random;
    }

}
