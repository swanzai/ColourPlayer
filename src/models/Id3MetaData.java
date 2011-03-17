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

import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_2D;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_CREATESTREAM;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_OPENONLY;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_SOFTWARE;
import static org.jouvieje.FmodEx.Defines.FMOD_TIMEUNIT.FMOD_TIMEUNIT_MS;

import java.io.File;
import java.nio.ByteBuffer;

import org.jouvieje.FmodEx.Sound;
import org.jouvieje.FmodEx.System;
import org.jouvieje.FmodEx.Enumerations.FMOD_RESULT;
import org.jouvieje.FmodEx.Misc.BufferUtils;
import org.jouvieje.FmodEx.Misc.PointerUtils;
import org.jouvieje.FmodEx.Structures.FMOD_TAG;

public class Id3MetaData extends MetaData {
    public static final String[] genres = { "Blues", "Classic Rock", "Country",
            "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal",
            "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae",
            "Rock", "Techno", "Industrial", "Alternative", "Ska",
            "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient",
            "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical",
            "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel",
            "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space",
            "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic",
            "Gothic", "Darkwave", "Techno-Industrial", "Electronic",
            "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy",
            "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle",
            "Native American", "Cabaret", "New Wave", "Psychedelic", "Rave",
            "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk",
            "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll",
            "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing",
            "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass",
            "Avantgarde", "Gothic Rock", "Progressive Rock",
            "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band",
            "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
            "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony",
            "Booty Brass", "Primus", "Porn Groove", "Satire", "Slow Jam",
            "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad",
            "Rhytmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo",
            "Acapella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass",
            "Club-House", "Hardcore", "Terror", "Indie", "BritPop",
            "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap",
            "Heavy Metal", "Black Metal", "Crossover",
            "Contemporary Christian", "Christian Rock", "Merengue", "Salsa",
            "Thrash Metal", "Anime", "Jpop", "Synthpop", "unknown" };

    public void read(String url, System system) {
        FMOD_TAG tag = FMOD_TAG.create();
        FMOD_RESULT result;
        Sound sound = new Sound();

        system.createSound(url, FMOD_SOFTWARE | FMOD_2D | FMOD_CREATESTREAM
                | FMOD_OPENONLY, null, sound);

        // get length
        ByteBuffer buffer = BufferUtils.newByteBuffer(BufferUtils.SIZEOF_INT);
        if (check(sound.getLength(buffer.asIntBuffer(), FMOD_TIMEUNIT_MS))) {
            setDuration(buffer.getInt(0));
        }

        { // normal tags
            if (check(sound.getTag("ARTIST", 0, tag)))
                setArtist(PointerUtils.toString(tag.getData()));

            if (check(sound.getTag("ALBUM", 0, tag)))
                setAlbum(PointerUtils.toString(tag.getData()));

            if (check(sound.getTag("TITLE", 0, tag))) {
                setTitle(PointerUtils.toString(tag.getData()));
            } else {
                setTitle(new File(url).getName());
            }

            try {
                if (check(sound.getTag("TRACK", 0, tag)))
                    setTrackNo(Integer.parseInt(PointerUtils.toString(tag
                            .getData())));
                
                if (check(sound.getTag("YEAR", 0, tag)))
                    setYear(Integer.parseInt(PointerUtils.toString(tag
                            .getData())));

                if (check(sound.getTag("GENRE", 0, tag))) {
                    int index = Integer.parseInt(PointerUtils.toString(tag
                            .getData()));

                    if (index < genres.length)
                        setGenre(genres[index]);
                }
            } catch (NumberFormatException e) {
                // ignore..
            }

        }

        { // replace with id3v2 if exists
            if (check(sound.getTag("TPE1", 0, tag)))
                setArtist(PointerUtils.toString(tag.getData()));
            if (check(sound.getTag("TOA", 0, tag))) //old v2
                setArtist(PointerUtils.toString(tag.getData()));            
            
            if (check(sound.getTag("TALB", 0, tag)))
                setAlbum(PointerUtils.toString(tag.getData()));
            if (check(sound.getTag("TAL", 0, tag))) //old v2
                setAlbum(PointerUtils.toString(tag.getData()));
            

            if (check(sound.getTag("TIT2", 0, tag)))
                setTitle(PointerUtils.toString(tag.getData()));
            if (check(sound.getTag("TT2", 0, tag))) //old v2
                setTitle(PointerUtils.toString(tag.getData()));

            if (check(sound.getTag("TRCK", 0, tag))) {
                String d = PointerUtils.toString(tag.getData());
                String[] a = d.split("/");

                try {
                    setTrackNo(Integer.parseInt(a[0]));
                    setTrackNoOf(Integer.parseInt(a[1]));
                } catch (NumberFormatException e) {
                    setTrackNo(0);
                } catch (ArrayIndexOutOfBoundsException oob) {
                    setTrackNo(Integer.parseInt(d));
                }
            }
            
            if (check(sound.getTag("TRK", 0, tag))) { //old v2
                String d = PointerUtils.toString(tag.getData());
                String[] a = d.split("/");

                try {
                    setTrackNo(Integer.parseInt(a[0]));
                    setTrackNoOf(Integer.parseInt(a[1]));
                } catch (NumberFormatException e) {
                    setTrackNo(0);
                } catch (ArrayIndexOutOfBoundsException oob) {
                    setTrackNo(Integer.parseInt(d));
                }
            }            

            if (check(sound.getTag("TYER", 0, tag))) {
                try {
                    setYear(Integer.parseInt(PointerUtils.toString(tag
                            .getData())));
                } catch (NumberFormatException e) {

                }
            }
            
            if (check(sound.getTag("TYE", 0, tag))) { //old v2
                try {
                    setYear(Integer.parseInt(PointerUtils.toString(tag
                            .getData())));
                } catch (NumberFormatException e) {

                }
            }

            if (check(sound.getTag("TCON", 0, tag))) {
                try {
                    setYear(Integer.parseInt(PointerUtils.toString(tag
                            .getData())));
                } catch (NumberFormatException e) {

                }
            }

            // if (check(sound.getTag("APIC", 0, tag))) {
            // setApic(PointerUtils.toBuffer(tag.getData(), tag.getDataLen()));
            // }

            tag.release();
            sound.release();
        }
    }
}
