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

import java.util.HashMap;

import models.PlaylistDao;
import models.Track;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import actions.AddToPlaylistAction;
import actions.ClearPlaylistAction;
import actions.OpenContainingDirectoryAction;
import actions.RemoveFromPlaylistAction;
import actions.ShowTrackPropertiesAction;
import ca.odell.glazedlists.EventList;
import data.GlazedDatabaseDao;

public class TableContextMenu {
    private Menu menu;
    private HashMap<String, MenuItem> itemMap;
    private final PlaylistDao nowPlaying;
    private final Table table;
    private final GlazedDatabaseDao dao;
    private MenuItem properties;
    private MenuItem openContaining;

    public TableContextMenu(Shell shell, Table table, GlazedDatabaseDao dao,
            PlaylistDao nowPlaying) {
        this.table = table;
        this.dao = dao;
        this.nowPlaying = nowPlaying;

        itemMap = new HashMap<String, MenuItem>(10);
        menu = new Menu(shell, SWT.POP_UP);
        table.setMenu(menu);
    }

    public void initItems(EventList<Track> eventList) {
        MenuItem addToPlaylist;
        MenuItem removeFromPlaylist;
        MenuItem clearPlaylist;

        itemMap.put("addToPlaylist", addToPlaylist = new MenuItem(menu,
                SWT.PUSH));
        addToPlaylist.setText("Add to playlist");
        addToPlaylist.addListener(SWT.Selection, new AddToPlaylistAction(table,
                nowPlaying, eventList));


        new MenuItem(menu, SWT.SEPARATOR);

        itemMap.put("openContaining", openContaining = new MenuItem(menu,
                SWT.PUSH));
        openContaining.setText("Open Containing Directory");
        openContaining.addListener(SWT.Selection,
                new OpenContainingDirectoryAction(table, eventList));

        itemMap.put("properties", properties = new MenuItem(menu, SWT.PUSH));
        properties.setText("Properties");
        properties.addListener(SWT.Selection, new ShowTrackPropertiesAction(
                eventList, table));
    }

}
