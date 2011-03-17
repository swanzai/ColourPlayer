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

import java.io.File;

import org.jouvieje.FmodEx.Sound;
import org.jouvieje.FmodEx.Enumerations.FMOD_RESULT;
import org.jouvieje.FmodEx.Misc.PointerUtils;
import org.jouvieje.FmodEx.Structures.FMOD_TAG;
import org.jouvieje.FmodEx.System;

public class VorbisMetaData extends MetaData {
    public void read(String url, System system) {
        FMOD_TAG tag = FMOD_TAG.create();
        FMOD_RESULT result;
        Sound sound = new Sound();

        system.createSound(url, FMOD_SOFTWARE | FMOD_2D | FMOD_CREATESTREAM
                | FMOD_OPENONLY, null, sound);

        { // normal tags
            if (check(sound.getTag("artist", 0, tag)))
                setArtist(PointerUtils.toString(tag.getData()));

            if (check(sound.getTag("album", 0, tag)))
                setAlbum(PointerUtils.toString(tag.getData()));

            if (check(sound.getTag("title", 0, tag)))
                setTitle(PointerUtils.toString(tag.getData()));
            else
                setTitle(new File(url).getName());

            if (check(sound.getTag("date", 0, tag)))
                setYear(Integer.parseInt(PointerUtils.toString(tag.getData())));

            if (check(sound.getTag("genre", 0, tag)))
                setGenre(PointerUtils.toString(tag.getData()));

            if (check(sound.getTag("tracknumber", 0, tag)))
                setTrackNo(Integer.parseInt(PointerUtils
                        .toString(tag.getData())));
        }
    }
}
