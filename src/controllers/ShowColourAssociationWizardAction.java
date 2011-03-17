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

package controllers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import views.ColourAssociationWizard;
import data.PropertiesLoader;

public class ShowColourAssociationWizardAction extends Action {
    private final Shell shell;
    private final Player player;
    private ColourAssociationWizard colourWizard;
    private final PropertiesLoader properties;

    public ShowColourAssociationWizardAction(Shell shell, Player player, PropertiesLoader properties) {
        this.shell = shell;
        this.player = player;
        this.properties = properties;
        
        setText("Colour Association &Wizard");
    }

    @Override
    public void run() {
        colourWizard = new ColourAssociationWizard(player, properties);
        WizardDialog wizard = new WizardDialog(shell, colourWizard);
        wizard.setPageSize(400, 250);
        wizard.create();
        wizard.open();
    }
    
    public void cleanUp() {
        if (colourWizard!=null) {
            colourWizard.cleanUp();
        }
    }
}
