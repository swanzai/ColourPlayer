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
import models.Track;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

import views.TrackPropertiesDialog;
import ca.odell.glazedlists.EventList;

public class ShowTrackPropertiesAction extends Action implements Listener {
    private final Table table;
    private final EventList<Track> eventList;

    public ShowTrackPropertiesAction(EventList<Track> eventList, Table table) {        
        this.eventList = eventList;
        this.table = table;

        setText("Properties");
    }

    @Override
    public void run() {
        showDialog();
    }

    public void handleEvent(Event event) {
        showDialog();
    }

    private void showDialog() {
        TrackPropertiesDialog dialog = new TrackPropertiesDialog(Display
                .getDefault().getActiveShell());

        dialog.open();
        dialog.setTrack(eventList.get(table.getSelectionIndex()));
    }
}
