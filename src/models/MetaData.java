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

import static java.lang.System.err;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_2D;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_CREATESTREAM;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_OPENONLY;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_SOFTWARE;
import static org.jouvieje.FmodEx.Enumerations.FMOD_RESULT.FMOD_OK;
import static org.jouvieje.FmodEx.Enumerations.FMOD_TAGTYPE.FMOD_TAGTYPE_ID3V1;
import static org.jouvieje.FmodEx.Enumerations.FMOD_TAGTYPE.FMOD_TAGTYPE_ID3V2;
import static org.jouvieje.FmodEx.Enumerations.FMOD_TAGTYPE.FMOD_TAGTYPE_VORBISCOMMENT;

import java.nio.ByteBuffer;

import org.jouvieje.FmodEx.Sound;
import org.jouvieje.FmodEx.System;
import org.jouvieje.FmodEx.Enumerations.FMOD_RESULT;
import org.jouvieje.FmodEx.Structures.FMOD_TAG;

import controllers.Player;

public abstract class MetaData {

    private String artist = "Unknown Artist";

    private String title = "Untitled";

    private String album = "Unknown Album";

    private int trackNo = 0;

    private int trackNoOf = 0;

    private int year;

    private String genre = "Unknown";

    private String path;

    private int folderId;

    private ByteBuffer apic;

    private int duration;

    public static MetaData factory(Player player, String url) {
        FMOD_TAG tag = FMOD_TAG.create();

        Sound sound = new Sound();
        System system = player.getSystem();

        system.createSound(url, FMOD_SOFTWARE | FMOD_2D | FMOD_CREATESTREAM
                | FMOD_OPENONLY, null, sound);

        int i = 0;
        while (sound == null && i < 50) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }

        try {
            if (sound.getTag(null, 0, tag) == FMOD_OK) {
                MetaData data = null;

                // tag found!
                if (tag.getType() == FMOD_TAGTYPE_ID3V1
                        || tag.getType() == FMOD_TAGTYPE_ID3V2) {
                    data = new Id3MetaData();
                } else if (tag.getType() == FMOD_TAGTYPE_VORBISCOMMENT) {
                    data = new VorbisMetaData();
                }

                sound.release();
                tag.release();

                return data;
            }
        } catch (NullPointerException e) {
            //no tag
            err.println("No tag found in " + url);
            return new DefaultMetaData();
        }
        return new DefaultMetaData();
    }

    public ByteBuffer getApic() {
        return apic;
    }

    public void setApic(ByteBuffer apic) {
        this.apic = apic;
        apic.rewind();
    }

    public abstract void read(String url, System system);

    protected boolean check(FMOD_RESULT res) {
        return (res == FMOD_RESULT.FMOD_OK);
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album.trim();
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        title = title.trim();
        this.title = title;
    }

    public int getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
    }

    public int getTrackNoOf() {
        return trackNoOf;
    }

    public void setTrackNoOf(int trackNoOf) {
        this.trackNoOf = trackNoOf;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Title: " + getTitle());
        buffer.append("\n");
        buffer.append("Artist: " + getArtist());
        buffer.append("\n");
        buffer.append("Album: " + getAlbum());
        buffer.append("\n");
        buffer.append("Genre: " + getGenre());
        buffer.append("\n");
        buffer.append("TrackNo: " + getTrackNo());
        buffer.append("\n");
        buffer.append("TrackNo Of: " + getTrackNoOf());
        buffer.append("\n");
        buffer.append("Year: " + getYear());
        return "-------------------------\n" + buffer.toString()
                + "\n----------------------------";

    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

}
