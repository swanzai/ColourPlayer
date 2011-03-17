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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

public class ButtonAction extends Action implements SelectionListener {
    protected final Button button;

    public ButtonAction(Button button) {
        this.button = button;
        
        button.addSelectionListener(this);
    }
    public void widgetDefaultSelected(SelectionEvent arg0) {
    }

    public void widgetSelected(SelectionEvent arg0) {
        run();
    }
    
    @Override
    public void setImageDescriptor(ImageDescriptor newImage) {
        super.setImageDescriptor(newImage);
        button.setImage(newImage.createImage());
    }
}
