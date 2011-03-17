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

package models;

import data.Dao;
import exceptions.DataAccessException;

public class Track {
    int id, artistId, albumId, genreId;
    
    /**
     * ID if the trck is in a playlist
     */
    int idp;

    int rating = -1;

    String artist = "";

    String album = "";

    String title = "";

    int[] colour;

    int trackNo = 0;

    int trackNoOf = 0;

    int year;

    int duration;

    String path;

    String genre = "";

    MetaData metaData;

    public Track() {
    }

    public String getTitle() {
        return title;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
    }

    public void loadByMetaData(Dao dao, String path, MetaData metaData) {
        this.path = path;

        try {
            artistId = dao.getArtistId(metaData.getArtist());
            albumId = dao.getAlbumId(metaData.getAlbum());

            artistId = dao.getArtistId(metaData.getArtist());
            genre = metaData.getGenre();
            genreId = dao.getGenreId(genre);

            title = metaData.getTitle();
            artist = metaData.getArtist();
            album = metaData.getAlbum();

            // System.out.println("Found title: " + title);
            trackNo = metaData.getTrackNo();
            trackNoOf = metaData.getTrackNoOf();
            duration = metaData.getDuration();
            year = metaData.getYear();
        } catch (DataAccessException e) {
            System.err.println("Error in data access while loading meta data");
            e.printStackTrace();
        }
    }

    public int getTrackNo() {
        return trackNo;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", getId(), getArtist(), getTitle());
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getAlbumid() {
        return albumId;
    }

    public void setAlbumid(int albumId) {
        this.albumId = albumId;
    }

    public int getArtistid() {
        return artistId;
    }

    public void setArtistid(int artistId) {
        this.artistId = artistId;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getTrackNoOf() {
        return trackNoOf;
    }

    public void setTrackNoOf(int trackNoOf) {
        this.trackNoOf = trackNoOf;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTrackNoString() {
        if (trackNoOf != 0) {
            return trackNo + "/" + trackNoOf;
        } else {
            return Integer.toString(trackNo);
        }
    }

    public void setColour(int r, int g, int b) {
        colour = new int[] { r, g, b };
    }
    
    public int[] getColour() {
        return colour;
    }

    public int getIdp() {
        return idp;
    }

    public void setIdp(int idp) {
        this.idp = idp;
    }

    public void clearColour() {
        colour = null;
    }
}
