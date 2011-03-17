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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import views.MainView;
import data.GlazedDatabaseDao;
import data.PropertiesLoader;
import exceptions.DataAccessException;
import exceptions.DatabaseInUseException;
import exceptions.DriverNotFoundException;

public class LoadingThread extends Thread {

    boolean loaded = false;

    int progress = 0;

    private Player play;

    private MainView view;

    private MainController controller;

    private String text;

    private GlazedDatabaseDao dao;

    private final PropertiesLoader loader;

    private boolean end;

    public LoadingThread(PropertiesLoader loader) {
        this.loader = loader;
        this.text = "Loading Music Core...";
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void run() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                play = new CustomizablePlayer(loader);
                progress++;
            }
        });

        setText("Loading Gui...");

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                view = new MainView();
                view.create();
                progress++;
            }
        });

        setText("Loading Database...");
        try {
            this.dao = new GlazedDatabaseDao();
            // dao.loadColourData();
            progress++;
        } catch (DriverNotFoundException e) {
            System.err.println("Database driver not found.");
            System.exit(0);
        } catch (DatabaseInUseException sqle) {
            // database already open?
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "Database In Use",
                            "The database is still in use.\nMake sure the application isn't already running");

                }
            });
            System.exit(0);
        }

        setText("Loading Controllers...");

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    controller = new MainController(loader, play, dao, view);
                } catch (DataAccessException dae) {
                    dae.printStackTrace();
                }
                progress++;
            }
        });

        this.loaded = true;

        try {
            while (!end) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {

        }
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public MainController getController() {
        return controller;
    }

    public void setController(MainController controller) {
        this.controller = controller;
    }

    public Player getPlay() {
        return play;
    }

    public void setPlay(Player play) {
        this.play = play;
    }

    public MainView getView() {
        return view;
    }

    public void setView(MainView view) {
        this.view = view;
    }

    public int getProgress() {
        return progress;
    }
}