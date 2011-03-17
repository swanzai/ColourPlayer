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

import java.io.File;

import models.Track;

import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

import ca.odell.glazedlists.EventList;

public class OpenContainingDirectoryAction implements Listener {
    private final Table table;
    private final EventList<Track> eventList;

    public OpenContainingDirectoryAction(Table table, EventList<Track> eventList) {
        this.table = table;
        this.eventList = eventList;
    }

    public void handleEvent(Event event) {

        Program.launch(new File(eventList.get(table.getSelectionIndex())
                .getPath()).getParent());

    }
}
