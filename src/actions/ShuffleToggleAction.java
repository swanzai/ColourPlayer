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

package actions;

import models.PlaylistDao;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import data.PropertiesLoader;

public class ShuffleToggleAction extends Action {
    private final PlaylistDao playlist;
    private final PropertiesLoader propertiesLoader;

    public ShuffleToggleAction(PlaylistDao playlist,
            PropertiesLoader propertiesLoader) {
        super("S&huffle", SWT.CHECK);
        this.playlist = playlist;
        this.propertiesLoader = propertiesLoader;

        init();
    }

    private void init() {
        String shuffle = propertiesLoader.getProperties()
                .getProperty("shuffle");

        if (shuffle == null || shuffle.equals("false")) {
            shuffle(false);
        } else {
            shuffle(true);
        }
    }

    private void shuffle(boolean shuffleOn) {
        setChecked(shuffleOn);

        // toggle the now playing playlist's mode
        playlist.setMode(shuffleOn ? PlaylistDao.PLAYBACK_SHUFFLE
                : PlaylistDao.PLAYBACK_NORMAL);

        propertiesLoader.getProperties().setProperty("shuffle",
                shuffleOn ? "true" : "false");
    }

    @Override
    public void run() {
        boolean shuffleOn = playlist.getMode() == PlaylistDao.PLAYBACK_NORMAL;
        shuffle(shuffleOn);
    }
}
