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

package data;

import helpers.FileUtil;

import java.io.File;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import listeners.TracksModifiedListener;
import models.MetaData;
import models.Track;
import controllers.Player;
import decoding.FmodLib;
import exceptions.DataAccessException;
import exceptions.DatabaseInUseException;
import exceptions.DriverNotFoundException;

/**
 * A file scanner that adds music to the database once it has found tracks that
 * it supports
 * 
 * @author Mike
 */
public class MusicScanner extends FileScanner {
    private static PreparedStatement stmtAddSong, stmtGetArtistId, stmtAddArtist, stmtGetAlbumId, stmtAddAlbum, stmtSongExists;
    /**
     * @param args
     */
    public static void main(String[] args) {
        final Player player = new Player();

        try {
            DatabaseDao dao = new DatabaseDao();
            new MusicScanner(player, dao).scan("E:\\music");
        } catch (DriverNotFoundException e) {
            e.printStackTrace();
        } catch (DatabaseInUseException e) {
            e.printStackTrace();
        }

    }
    private Player player;
    private Dao dao;

    private ArrayList<TracksModifiedListener> modifiedListeners;

    private boolean finished = false;

    /**
     * Prints out scanning information
     */
    private boolean verbose = false;

    /**
     * @param player
     *            Passed in for metadata reading capability
     */
    public MusicScanner(Player player, Dao dao) {
        // this.tagLib = new MusicTag();
        this.player = player;
        this.dao = dao;
    }

    /**
     * Adds a song to the database and model
     * 
     * @param file
     * @throws UnsupportedAudioFileException
     */
    public void addSong(File file) throws UnsupportedAudioFileException {
        // return is file not supported
        if (!FmodLib.isSupported(FileUtil.getExtension(file.getName()))) {
            throw new UnsupportedAudioFileException("Unsupported audio file " + FileUtil.getExtension(file.getName()));
        }

        // get meta-data
        MetaData metaData = MetaData.factory(player, file.getAbsolutePath());
        metaData.read(file.getAbsolutePath(), player.getSystem());

        Track track = new Track();
        track.loadByMetaData(dao, file.getAbsolutePath(), metaData);

        if (verbose) {
            System.out.println(file.getAbsolutePath());
            System.out.println(metaData);
        }
        int insertType = Dao.TRACK_INSERTED;

        try {
            insertType = dao.addTrack(track);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        notifyModifiedListeners((insertType == Dao.TRACK_INSERTED) ? "inserted" : "updated", file.getAbsolutePath());
    }

    /**
     * Add song to the database and model described by a URL
     * 
     * @param url
     * @throws UnsupportedAudioFileException
     */
    public void addSong(URL url) throws UnsupportedAudioFileException {
        final Track track = new Track();
        int insertType = Dao.TRACK_INSERTED;
        track.setTitle("Stream");
        track.setPath(url.getFile());

        try {
            insertType = dao.addTrack(track);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        notifyModifiedListeners((insertType == Dao.TRACK_INSERTED) ? "inserted" : "updated", url.getPath());
    }

    public void addTrackModifiedListener(TracksModifiedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (modifiedListeners == null) {
            modifiedListeners = new ArrayList<TracksModifiedListener>();

        }
        modifiedListeners.add(listener);
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void scan(String path) {
        finished = false;
        // get connection object
        // initDb();

        super.scan(path);
    }

    private void notifyModifiedListeners(String message, Object data) {
        if (modifiedListeners != null) {
            for (TracksModifiedListener l : modifiedListeners) {
                l.tracksModified(message, data);
            }
        }
    }

    @Override
    protected void fileFound(File file) {

        try {
            addSong(file);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unsupported file");
        } catch (NullPointerException e) {
            // weird bug found in beta 2 - hide error! yeh, it's bad
            e.printStackTrace();
        }
    }

    @Override
    protected void finished() {
        notifyModifiedListeners("finished", null);
        finished = true;
    }
}
