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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import listeners.TracksModifiedListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import views.GlazedMainTable;
import controllers.Player;
import data.Dao;
import data.MusicScanner;

public class FileScanningAction extends Action {
    private final Player player;

    private final Dao dao;

    private final Shell shell;

    private ArrayList<TracksModifiedListener> listeners;

    private final GlazedMainTable mainTable;

    public FileScanningAction(Player player, Dao dao, Shell shell, GlazedMainTable mainTable) {
        this.shell = shell;
        this.player = player;
        this.dao = dao;
        this.mainTable = mainTable;

        setText("&Add Music to Library...");
    }

    @Override
    public void run() {
        
        try {
            mainTable.setRedrawEnabled(false);
            // disable action
            DirectoryDialog dialog = new DirectoryDialog(shell);
            dialog.setText("Select a Directory");
            dialog
                    .setMessage("Select a directory of music files. Music files found in that directory will be added to your music library.");
            final String path = dialog.open();

            new ProgressMonitorDialog(shell).run(true, true,
                    new ScanProgressMonitor(path));
        } catch (InvocationTargetException e) {
            MessageDialog.openError(shell, "Error", e.getMessage());
        } catch (InterruptedException e) {
            MessageDialog.openInformation(shell, "Cancelled", e.getMessage());
        } finally {
            mainTable.setRedrawEnabled(true);
        }
    }

    public void scanFiles(String[] files) {
        try {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
            dialog.run(true, true, new ScanProgressMonitor(files));
        } catch (InvocationTargetException e) {
            MessageDialog.openError(shell, "Error", e.getMessage());
        } catch (InterruptedException e) {
            MessageDialog.openInformation(shell, "Cancelled", e.getMessage());
        }
    }

    public void scanFilesQuiet(String[] files) {
        MusicScanner scanner = new MusicScanner(player, dao);
        scanner.scanFiles(files);
    }

    class ScanProgressMonitor implements IRunnableWithProgress {
        private String path;
        private String[] paths;

        public ScanProgressMonitor(String path) {
            this.path = path;
        }

        public ScanProgressMonitor(String[] paths) {
            this.paths = paths;
        }

        public void run(final IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
            if (path != null || paths != null) {
                monitor.beginTask("Adding tracks...", IProgressMonitor.UNKNOWN);
                final MusicScanner scanner = new MusicScanner(player, dao);

                new Thread(new Runnable() {
                    public void run() {
                        scanner
                                .addTrackModifiedListener(new TracksModifiedListener() {
                                    public void tracksModified(String message,
                                            Object data) {
                                        if (message.equals("inserted")) {
                                            monitor.subTask("Added "
                                                    + data.toString());
                                        } else if (message.equals("updated")) {
                                            monitor.subTask("Updated "
                                                    + data.toString());
                                        }
                                    };
                                });

                        if (path != null)
                            scanner.scan(path);
                        else
                            scanner.scanFiles(paths);
                    };
                }).start();

                while (!monitor.isCanceled() && !scanner.isFinished()) {
                    Thread.sleep(200);
                }

                if (monitor.isCanceled()) {
                    scanner.cancel();
                }

                monitor.done();
            }
        }
    }
}
