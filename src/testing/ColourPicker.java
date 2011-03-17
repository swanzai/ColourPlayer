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

package testing;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import views.ColourPickerCanvasWrapper;

public class ColourPicker extends ApplicationWindow {
    private ColourPickerCanvasWrapper picker;

    public ColourPicker() {
        super(null);

    }

    @Override
    protected Control createContents(Composite parent) {
        getShell().setText("Colour Picker");
        picker = new ColourPickerCanvasWrapper(parent);

        return parent;
    }

    public void cleanUp() {
        picker.cleanUp();
    }

    public static void main(String[] args) {
        ColourPicker picker = new ColourPicker();
        picker.setBlockOnOpen(true);
        picker.open();
        picker.cleanUp();
    }

}
