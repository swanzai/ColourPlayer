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

import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_HARDWARE;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_SOFTWARE;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import data.PropertiesLoader;

/**
 * A player that can be customized using properties
 * 
 * @author Michael Voong
 *
 */
public class CustomizablePlayer extends Player {
    private final PropertiesLoader propertiesLoader;

    public CustomizablePlayer(PropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;

    }

    @Override
    public synchronized void play(String url) throws FileNotFoundException, MalformedURLException, PlaybackError {
        String mode = propertiesLoader.getProperties().getProperty(
                "decoding_method");

        if (mode == null || mode.equals("Hardware")) {
            setPlaybackMode(FMOD_HARDWARE);
            System.out.println("Playing using hardware mode");
        } else {
            setPlaybackMode(FMOD_SOFTWARE);
            System.out.println("Playing using software mode");
        }
        
        super.play(url);
    }
}
