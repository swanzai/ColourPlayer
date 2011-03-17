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

import org.eclipse.swt.graphics.Image;

public class SubMenuItem {
    private Image image;
    private String text;
    private PlaylistDao playlist;

    public SubMenuItem(String text) {
        this.text = text;
    }

    public SubMenuItem(String text, Image image) {
        this(text);
        this.image = image;
    }

    public SubMenuItem(String text, Image image, PlaylistDao playlist) {
        this(text, image);
        this.playlist = playlist;
    }

    public Image getImage() {
        return image;
    }

    public PlaylistDao getPlaylist() {
        return playlist;
    }

    public String getText() {
        return text;
    }
}
