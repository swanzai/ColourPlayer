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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

import models.PlaylistDao;
import models.SubMenuItem;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swt.EventTableViewer;

public class SubSectionList {
    private Table table;
    private EventList<SubMenuItem> eventList;
    private ArrayList<Image> imageResources = new ArrayList<Image>();

    public SubSectionList(Composite parent) {
        this.table = new Table(parent, SWT.V_SCROLL | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.BORDER);
    }

    public void initItems(PlaylistDao nowPlaying) {
        try {

            Image librayImage = ImageDescriptor.createFromURL(
                    new URL("file:images/hdd.png")).createImage();
            Image nowPlayingImage = ImageDescriptor.createFromURL(
                    new URL("file:images/play.png")).createImage();

            imageResources.add(librayImage);
            imageResources.add(nowPlayingImage);

            EventList<SubMenuItem> eventList = new BasicEventList<SubMenuItem>();

            eventList.add(new SubMenuItem("Library", librayImage));
            eventList.add(new SubMenuItem("Now Playing", nowPlayingImage, nowPlaying));

            eventList.addListEventListener(new ImageUpdatingEventListener());

            setEventList(eventList);

            // force an update of images
            update();
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void update() {
        update(0, eventList.size() - 1);
    }

    public void update(int startIndex, int endIndex) {
        for (int i = startIndex; i <= endIndex; i++) {
            eventList.set(i, eventList.get(i));
        }
    }

    public void cleanUp() {
        if (imageResources != null)
            for (Image i : imageResources) {
                i.dispose();
            }
    }

    public Table getTable() {
        return table;
    }

    private void setEventList(EventList<SubMenuItem> eventList) {
        this.eventList = eventList;

        EventTableViewer viewer = new EventTableViewer(this.eventList,
                this.table, new SubItemTableFormat());

        addColumnResizer();
    }

    private void addColumnResizer() {
        table.getParent().addControlListener(new ControlListener() {
            public void controlResized(org.eclipse.swt.events.ControlEvent e) {
                table.getColumn(0).setWidth(table.getSize().x - table.getBorderWidth() * 2);
            };

            public void controlMoved(ControlEvent e) {
            }
        });
    }

    private class SubItemTableFormat implements AdvancedTableFormat {
        // @Override
        // public Image getImage(Object element) {
        // return ((SubMenuItem) element).getImage();
        // }
        //
        // @Override
        // public String getText(Object element) {
        // return ((SubMenuItem) element).getText();
        // }

        public Class getColumnClass(int column) {
            return null;
        }

        public Comparator getColumnComparator(int column) {
            return null;
        }

        public int getColumnCount() {
            return 1;
        }

        public String getColumnName(int column) {
            return "Sections";
        }

        public Object getColumnValue(Object obj, int arg1) {
            return ((SubMenuItem) obj).getText();
        }

    }

    private class ImageUpdatingEventListener implements
            ListEventListener<SubMenuItem> {
        public void listChanged(ListEvent event) {
            while (event.next()) {
                if (event.getType() == ListEvent.UPDATE) {
                    table.getItem(event.getIndex()).setImage(
                            eventList.get(event.getIndex()).getImage());
                }
            }
        };
    }

    public EventList<SubMenuItem> getEventList() {
        return eventList;
    }

}
