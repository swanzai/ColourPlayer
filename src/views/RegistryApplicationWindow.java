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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class RegistryApplicationWindow extends ApplicationWindow {
    protected HashMap<String, Widget> widgets;
    protected HashMap<String, MenuManager> menus;

    protected void registerWidget(String name, Widget widget) {
        widgets.put(name, widget);
    }

    protected void registerMenu(String name, MenuManager menu) {
        menus.put(name, menu);
    }

    public Widget getWidget(String name) {
        return widgets.get(name);
    }

    public MenuManager getMenu(String name) {
        return menus.get(name);
    }

    public HashMap<String, Widget> getWidgetRegistry() {
        return widgets;
    }

    public HashMap<String, MenuManager> getMenuRegistry() {
        return menus;
    }

    public RegistryApplicationWindow(Shell shell) {
        super(shell);
        widgets = new HashMap<String, Widget>();
        menus = new HashMap<String, MenuManager>();
    }

}
