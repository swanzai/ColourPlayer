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
import java.io.IOException;

import org.eclipse.jface.wizard.WizardDialog;

import views.ColourAssociationWizard;
import views.MainView;
import views.Splash;
import controllers.LoadingThread;
import controllers.MainController;
import controllers.Player;
import data.Database;
import data.PropertiesLoader;

public class Main {
    private static MainView view;
    private static Player player;
    private static MainController controller;
    private static PropertiesLoader propertiesLoader;

    public static void main(String[] args) {
        // properties
        propertiesLoader = new PropertiesLoader();
        try {
            propertiesLoader.load();
        } catch (IOException e) {
            System.err.println("There was a problem with loading/creating the configuration file");
        }

        LoadingThread loadingThread = new LoadingThread(propertiesLoader);
        new Splash(loadingThread);

        // returns after loading
        view = loadingThread.getView();
        controller = loadingThread.getController();
        player = loadingThread.getPlay();

        // train som
        controller.trainSom();

        if (loadColourWizard()) {
            ColourAssociationWizard colourWizard = new ColourAssociationWizard(player, propertiesLoader);
            WizardDialog wizard = new WizardDialog(view.getShell(), colourWizard);
            wizard.setPageSize(400, 250);
            wizard.create();
            wizard.setBlockOnOpen(false);
            wizard.open();
        }

        // open window
        view.setBlockOnOpen(true);

        view.open();

        // save preferences
        try {
            propertiesLoader.save();
        } catch (IOException ioe) {
            System.err.println("There was an error saving the properties file while the program was shutting down");
        }

        player.cleanUp();
        controller.cleanUp();

        Database.cleanUp();
    }

    /**
     * Whether to load the colour wizard at all depends on a property
     * 
     * @return
     */
    private static boolean loadColourWizard() {
        String load = propertiesLoader.getProperties().getProperty("show_association_wizard");
        return (load == null || !load.equals("false"));
    }
}
