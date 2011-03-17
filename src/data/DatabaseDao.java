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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Track;
import testing.Benchmark;
import exceptions.DataAccessException;
import exceptions.DatabaseInUseException;
import exceptions.DriverNotFoundException;

public class DatabaseDao implements Dao {
    /**
     * Cached tracks list
     */
    private List<Track> tracks;

    public DatabaseDao() throws DriverNotFoundException, DatabaseInUseException {
        prepareStatements();
    }

    private void prepareStatements() throws DriverNotFoundException,
            DatabaseInUseException {
        try {
            Statements.addTrack = Database
                    .getConnection()
                    .prepareStatement(
                            "INSERT INTO tracks (artist_id, album_id, genre_id, path, title, track_no, track_no_of, year, duration, time_added) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, now())");

            Statements.updateTrack = Database
                    .getConnection()
                    .prepareStatement(
                            "UPDATE tracks SET artist_id = ?, album_id = ?, genre_id = ?, path = ?, title = ?, track_no = ?, track_no_of = ?, year = ?, duration = ? WHERE id = ?");

            Statements.trackExists = Database.getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM tracks t WHERE path = ?");

            Statements.getArtistId = Database.getConnection().prepareStatement(
                    "SELECT id FROM artists WHERE artist = ?");

            Statements.addArtist = Database.getConnection().prepareStatement(
                    "INSERT INTO artists (artist) VALUES (?)");

            Statements.getAlbumId = Database.getConnection().prepareStatement(
                    "SELECT id FROM albums WHERE album = ?");

            Statements.getTrackId = Database.getConnection().prepareStatement(
                    "SELECT id FROM tracks WHERE path = ?");

            Statements.addAlbum = Database.getConnection().prepareStatement(
                    "INSERT INTO albums (album) VALUES (?)");

            Statements.addGenre = Database.getConnection().prepareStatement(
                    "INSERT INTO genres (genre) VALUES (?)");

            Statements.getGenreId = Database.getConnection().prepareStatement(
                    "SELECT id FROM genres WHERE genre = ?");

            Statements.getPlaylistId = Database
                    .getConnection()
                    .prepareStatement("SELECT id FROM playlists WHERE name = ?");

            Statements.addPlaylist = Database.getConnection().prepareStatement(
                    "INSERT INTO playlists (name) VALUES (?)");

            Statements.addTrackToPlaylist = Database
                    .getConnection()
                    .prepareStatement(
                            "INSERT INTO playlist_tracks (playlist_id, track_id) VALUES (?, ?)");

            Statements.removeTrackFromPlaylist = Database
                    .getConnection()
                    .prepareStatement(
                            "DELETE FROM playlist_tracks WHERE playlist_id = ? AND id = ?");

            Statements.removeTrack = Database.getConnection().prepareStatement(
                    "DELETE FROM tracks WHERE id = ?");

            Statements.clearPlaylist = Database
                    .getConnection()
                    .prepareStatement(
                            "DELETE FROM playlist_tracks WHERE playlist_id = (SELECT id FROM playlists WHERE name = ?)");

            Statements.identity = Database.getConnection().prepareStatement(
                    "CALL IDENTITY();");

            Statements.getTracks = Database
                    .getConnection()
                    .prepareStatement(
                            "SELECT * FROM tracks_view ORDER BY artist, album, track_no LIMIT ? OFFSET ?");

            Statements.getTracksFiltered = Database
                    .getConnection()
                    .prepareStatement(
                            "SELECT * FROM tracks_view WHERE (LOCATE(LCASE(?), LCASE(title))!=0 OR LOCATE(LCASE(?), LCASE(album))!=0 OR LOCATE(LCASE(?), LCASE(artist))!=0) ORDER BY artist, album, track_no LIMIT ? OFFSET ?");

            Statements.getNoOfTracks = Database.getConnection()
                    .prepareStatement("SELECT COUNT(*) FROM tracks");

            Statements.getNoOfTracksFilteredPlaylist = Database
                    .getConnection()
                    .prepareStatement(
                            "SELECT COUNT(*) FROM playlists LEFT JOIN playlist_tracks ON playlists.id = playlist_tracks.playlist_id LEFT JOIN tracks_view ON tracks_view.id = playlist_tracks.track_id WHERE (LOCATE(LCASE(?), LCASE(title))!=0 OR LOCATE(LCASE(?), LCASE(album))!=0 OR LOCATE(LCASE(?), LCASE(artist))!=0) AND playlists.name = ?");

            Statements.getTracksFilteredPlaylist = Database
                    .getConnection()
                    .prepareStatement(
                            "SELECT * FROM tracks_view LEFT JOIN playlist_tracks ON playlist_tracks.track_id = tracks_view.id LEFT JOIN playlists ON playlists.id = playlist_tracks.playlist_id WHERE (LOCATE(LCASE(?), LCASE(title))!=0 OR LOCATE(LCASE(?), LCASE(album))!=0 OR LOCATE(LCASE(?), LCASE(artist))!=0) AND playlists.name = ? LIMIT ? OFFSET ?");

            Statements.getTrackIdPlaylist = Database
            .getConnection()
            .prepareStatement(
                    "SELECT track_id FROM playlist_tracks LEFT JOIN playlists ON playlists.id = playlist_tracks.playlist_id WHERE playlists.name = ?");

            
            Statements.setTrackColour = Database
                    .getConnection()
                    .prepareStatement(
                            "INSERT INTO track_colours (track_id, r, g, b) VALUES (?, ?, ?, ?)");

            Statements.getNoOfTrackColours = Database.getConnection()
                    .prepareStatement("SELECT COUNT(*) FROM track_colours");

            Statements.delTrackColour = Database.getConnection()
                    .prepareStatement(
                            "DELETE FROM track_colours WHERE track_id = ?;");
            // Statements.getTracksPlaylist = Database
            // .getConnection()
            // .prepareStatement(
            // "SELECT * FROM tracks_view LEFT JOIN playlist_tracks ON
            // playlist_tracks.track_id = tracks_view.id LEFT JOIN playlists ON
            // playlists.id = playlist_tracks.id AND playlists.name = ? LIMIT ?
            // OFFSET ?");

            Statements.getNoOfTracksFiltered = Database
                    .getConnection()
                    .prepareStatement(
                            "SELECT COUNT(*) FROM tracks, artists, albums WHERE tracks.artist_id = artists.id AND tracks.album_id = albums.id AND (LOCATE(LCASE(?), LCASE(title))!=0 OR LOCATE(LCASE(?), LCASE(album))!=0 OR LOCATE(LCASE(?), LCASE(artist))!=0)");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getSQLState());
        }
    }

    public void addTrackToPlaylist(int trackId, String playlist)
            throws DataAccessException {
        try {
            Statements.addTrackToPlaylist.setInt(1, getPlaylistId(playlist));
            Statements.addTrackToPlaylist.setInt(2, trackId);

            int count = Statements.addTrackToPlaylist.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public void clearPlaylist(String playlist) throws DataAccessException {
        try {
            Statements.clearPlaylist.setString(1, playlist);
            int count = Statements.clearPlaylist.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public void removeTrack(int trackId) throws DataAccessException {
        try {
            Statements.removeTrack.setInt(1, trackId);
            int count = Statements.removeTrack.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException();
        }
    }

    public void removeTrackFromPlaylist(String playlistName, int trackIdp)
            throws DataAccessException {
        try {
            Statements.removeTrackFromPlaylist.setInt(1,
                    getPlaylistId(playlistName));
            Statements.removeTrackFromPlaylist.setInt(2, trackIdp);
            int count = Statements.removeTrackFromPlaylist.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException();
        }
    }

    public int addTrack(Track track) throws DataAccessException {
        try {
            PreparedStatement statement;
            boolean inserted = false; // inserted or updated?

            // add or delete?
            if (!trackExists(track)) {
                inserted = true;
                statement = Statements.addTrack;
            } else {
                statement = Statements.updateTrack;
                statement.setInt(10, getTrackId(track.getPath()));
            }

            statement.setInt(1, getArtistId(track.getArtist()));
            statement.setInt(2, getAlbumId(track.getAlbum()));
            statement.setInt(3, getGenreId(track.getGenre()));
            statement.setString(4, track.getPath());
            statement.setString(5, track.getTitle());
            statement.setInt(6, track.getTrackNo());
            statement.setInt(7, track.getTrackNoOf());
            statement.setInt(8, track.getYear());
            statement.setInt(9, track.getDuration());

            int rows = statement.executeUpdate();

            if (inserted) {
                track.setId(getIdentity());
                return Dao.TRACK_INSERTED;
            } else {

                return Dao.TRACK_UPDATED;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException();
        }
    }

    public boolean trackExists(Track track) throws DataAccessException {
        try {
            Statements.trackExists.setString(1, track.getPath());

            ResultSet rs = Statements.trackExists.executeQuery();

            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                return exists;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException();
        }

        return false;
    }

    public Track getTrack(int id) {
        return null;
    }

    public List<Track> getTracks(int limit, int offset)
            throws DataAccessException {
        ResultSet rs = null;

        try {
            Statements.getTracks.setInt(1, limit);
            Statements.getTracks.setInt(2, offset);

            rs = Statements.getTracks.executeQuery();
        } catch (SQLException e) {

        }

        return getTracks(rs);
    }

    public List<Track> getTracks(ResultSet rs) throws DataAccessException {

        Benchmark bench = new Benchmark();
        ArrayList<Track> list = new ArrayList<Track>();

        try {
            bench.start();

            int i = 0;
            boolean noIdp = false;

            while (rs.next()) {
                final Track track = new Track();
                track.setId(rs.getInt("id"));
                track.setAlbum(rs.getString("album"));
                track.setArtist(rs.getString("artist"));
                track.setTrackNo(rs.getInt("track_no"));
                track.setTrackNoOf(rs.getInt("track_no_of"));
                track.setDuration(rs.getInt("duration"));
                track.setTitle(rs.getString("title"));
                track.setPath(rs.getString("path"));
                track.setGenre(rs.getString("genre"));

                if (!noIdp) {
                    try {
                        rs.findColumn("idp");
                        track.setIdp(rs.getInt("idp"));
                    } catch (SQLException sqle) {
                        // no idp column found
                        noIdp = true;
                    }
                }

                // colours if set
                if (!(rs.getInt("r") == 0 && rs.getInt("g") == 0 && rs
                        .getInt("b") == 0)) {
                    track.setColour(rs.getInt("r"), rs.getInt("g"), rs
                            .getInt("b"));
                }
                list.add(track);
                i++;
            }

            bench.stop();

            return list;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            throw new DataAccessException();
        }
    }

    /**
     * Get an artist id if match found in database, otherwise create artist
     * record and return the artist id
     * 
     * @param artist
     * @return
     * @throws SQLException
     */
    public int getArtistId(String artist) throws DataAccessException {
        try {
            // get artist id or add if non-existent
            Statements.getArtistId.setString(1, artist);
            ResultSet rs = Statements.getArtistId.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            } else {
                Statements.addArtist.setString(1, artist);
                Statements.addArtist.executeUpdate();

                return getIdentity();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException();
        }
    }

    public int getTrackId(String path) throws DataAccessException {
        try {
            // get artist id or add if non-existent
            Statements.getTrackId.setString(1, path);
            ResultSet rs = Statements.getTrackId.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException();
        }
    }

    public int getIdentity() throws DataAccessException {
        try {
            ResultSet identity = Statements.identity.executeQuery();

            if (identity.next()) {
                return identity.getInt(1);
            } else {
                throw new DataAccessException("Identity could not be found");
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            throw new DataAccessException();
        }
    }

    public int getAlbumId(String album) throws DataAccessException {
        try {
            // get artist id or add if non-existent
            Statements.getAlbumId.setString(1, album);
            ResultSet rs = Statements.getAlbumId.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            } else {
                Statements.addAlbum.setString(1, album);
                Statements.addAlbum.executeUpdate();

                return getIdentity();
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public int getPlaylistId(String playlist) throws DataAccessException {
        try {
            // get artist id or add if non-existent
            Statements.getPlaylistId.setString(1, playlist);
            ResultSet rs = Statements.getPlaylistId.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            } else {
                Statements.addPlaylist.setString(1, playlist);
                Statements.addPlaylist.executeUpdate();

                return getIdentity();
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public int getGenreId(String genre) throws DataAccessException {
        try {
            // get artist id or add if non-existent
            Statements.getGenreId.setString(1, genre);
            ResultSet rs = Statements.getGenreId.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            } else {
                Statements.addGenre.setString(1, genre);
                Statements.addGenre.executeUpdate();

                return getIdentity();
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public int getNoOfTracks() throws DataAccessException {
        try {
            ResultSet rs = Statements.getNoOfTracks.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public int getNoOfTrackColours() throws DataAccessException {
        try {
            ResultSet rs = Statements.getNoOfTrackColours.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public void setTrackColour(int trackId, int r, int g, int b)
            throws DataAccessException {
        try {
            Statements.delTrackColour.setInt(1, trackId);
            Statements.delTrackColour.executeUpdate();

            Statements.setTrackColour.setInt(1, trackId);
            Statements.setTrackColour.setInt(2, r);
            Statements.setTrackColour.setInt(3, g);
            Statements.setTrackColour.setInt(4, b);

            int count = Statements.setTrackColour.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    static class Statements {
        public static PreparedStatement addTrack, updateTrack, getArtistId,
                addArtist, getAlbumId, getTrackId, addAlbum, getGenreId,
                addGenre, addTrackToPlaylist, getPlaylistId, addPlaylist,
                identity, trackExists, getTracks, getTracksFiltered,
                getNoOfTracks, getNoOfTracksFiltered, setTrackColour,
                getNoOfTrackColours, delTrackColour,
                getNoOfTracksFilteredPlaylist, getTracksFilteredPlaylist,
                getTracksPlaylist, clearPlaylist, removeTrackFromPlaylist,
                removeTrack, getTrackIdPlaylist;
    }
}
