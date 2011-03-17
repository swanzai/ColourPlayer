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

package views;

import models.MetaData;
import models.PlayerCallBackData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import controllers.Player;

public class StyledSongInfo implements PlayerListener {
    StyledText text;

    String title, album, artist;

    StyleRange titleStyle, albumStyle, artistStyle;

    private StyledText control;

    public StyledSongInfo(Composite composite) {
        initStyleRanges();

        control = new StyledText(composite, SWT.READ_ONLY | SWT.MULTI
                | SWT.READ_ONLY);

        control.setEditable(false);
        control.setText("Not playing...");
        control.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void update() {
        control.setText(title + "\nby " + artist + " from the album " + album);

        int offset;

        titleStyle.start = 0;
        titleStyle.length = title.length();

        offset = title.length() + 4; // of
        artistStyle.start = offset;
        artistStyle.length = artist.length();

        offset += artist.length() + " from the album ".length();
        albumStyle.start = offset;
        albumStyle.length = album.length();

        control.setStyleRange(titleStyle);
        control.setStyleRange(artistStyle);
        control.setStyleRange(albumStyle);
    }

    private void initStyleRanges() {
        titleStyle = new StyleRange();
        // titleStyle.font = new Font(Display.getDefault(), "Arial", 18,
        // SWT.BOLD);
        titleStyle.fontStyle = SWT.BOLD;

        albumStyle = new StyleRange();
        albumStyle.fontStyle = SWT.BOLD;

        artistStyle = new StyleRange();
        artistStyle.fontStyle = SWT.BOLD;
    }

    public StyledText getControl() {
        return control;
    }

    public void callback(PlayerCallBackData data) {
    }

    public void message(String message, Player player) {
        if (message.equals("playing")) {
            MetaData data = player.getMetaData();
            setAlbum(data.getAlbum());
            setArtist(data.getArtist());
            setTitle(data.getTitle());

            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    update();
                };
            });

        }
    }

}
