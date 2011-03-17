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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import config.Constants;
import data.PropertiesLoader;

public class PreferencesWindow extends ApplicationWindow {
    // GUI items
    private TabFolder tabs;
    private TabItem generalTab;
    private TabItem betaTab;
    private TabItem advancedTab;
    private Button okButton;
    private Button cancelButton;
    private Text scanMusicInText;
    private Button scanMusicButton;
    private List albumArtFileList;
    private Button addAlbumArtFileButton;
    private Button delAlbumArtFileButton;
    private Button defaultAlbumArtButton;
    private Button allowSendingCheck;
    private Combo decodingMethodCombo;
    private final PropertiesLoader properties;
    private Button scanForMusicCheck;

    public PreferencesWindow(Shell shell, PropertiesLoader properties) {
        super(shell);
        this.properties = properties;
    }

    @Override
    protected int getShellStyle() {
        return SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;

    }

    @Override
    protected Control createContents(Composite parent) {
        parent.getShell().setSize(450, 300);
        parent.getShell().setText(Constants.PROGRAM_NAME);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FormLayout());

        // tabs
        tabs = new TabFolder(composite, SWT.NONE);
        generalTab = new TabItem(tabs, SWT.NONE);
        generalTab.setText("General");
        generalTab.setControl(initGeneralTab(tabs));
        betaTab = new TabItem(tabs, SWT.NONE);
        betaTab.setText("Beta");
        betaTab.setControl(initBetaTab(tabs));
        advancedTab = new TabItem(tabs, SWT.NONE);
        advancedTab.setText("Advanced");
        advancedTab.setControl(initAdvancedTab(tabs));

        // buttons
        okButton = new Button(composite, SWT.NONE);
        okButton.setText("OK");

        cancelButton = new Button(composite, SWT.NONE);
        cancelButton.setText("Cancel");

        // layout items
        FormData fd;

        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -10);
        fd.width = 80;
        cancelButton.setLayoutData(fd);

        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(cancelButton, -5, SWT.LEFT);
        fd.width = 80;
        okButton.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        fd.right = new FormAttachment(100, -10);
        fd.bottom = new FormAttachment(okButton, -10, SWT.TOP);
        tabs.setLayoutData(fd);

        initListeners();
        initData();
        
        parent.getShell().pack();

        return parent;
    }

    private Control initBetaTab(TabFolder folder) {
        FormData fd;

        Composite composite = new Composite(folder, SWT.NONE);
        composite.setLayout(new FormLayout());

        allowSendingCheck = new Button(composite, SWT.CHECK);
        Label allowSendingLabel = new Label(composite, SWT.WRAP);
        allowSendingLabel
                .setText("Allow automatic sending of colour associations");
        Label allowSendingDescriptionLabel = new Label(composite, SWT.WRAP);
        allowSendingDescriptionLabel
                .setText("Your colour profiles are sent automatically to us "
                        + Constants.VERSION
                        + " for research purposes. If you don't want your associations to be sent off, please leave this box unchecked");

        // layout
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 30);

        fd.right = new FormAttachment(100, -10);
        allowSendingLabel.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(allowSendingLabel, 0, SWT.TOP);
        fd.right = new FormAttachment(allowSendingLabel, -10, SWT.LEFT);
        allowSendingCheck.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(allowSendingLabel, 5, SWT.BOTTOM);
        fd.left = new FormAttachment(allowSendingLabel, 20, SWT.LEFT);
        fd.right = new FormAttachment(100, -10);
        fd.width = 300;
        allowSendingDescriptionLabel.setLayoutData(fd);

        return composite;
    }

    private void initDefaultAlbumArtFileList() {
        for (String fileName : AlbumArtCanvas.ALBUM_ART_FILES) {
            albumArtFileList.add(fileName);
        }

    }

    protected void initData() {
        Properties p = properties.getProperties();
        // init album art list
        initDefaultAlbumArtFileList();

        // add custom ones
        String customNames = p.getProperty("album_art_file_names");
        if (customNames != null) {
            String[] names = customNames.trim().split("\\|");
            for (String name : names) {
                if (name.trim().length() > 0)
                    albumArtFileList.add(name);
            }
        }

        // decoding mode combo box
        decodingMethodCombo.add("Hardware");
        decodingMethodCombo.add("Software");
        String decodingMethod = (String) properties.getProperties().get(
                "decoding_method");
        decodingMethodCombo.select((decodingMethod == null
                || decodingMethod.equals("Hardware") ? 0 : 1));

        allowSendingCheck.setSelection(p
                .getProperty("allow_sending_associations") == null
                || p.getProperty("allow_sending_associations").equals("true"));

        String autoScan = p.getProperty("auto_scan_for_music");
        if (autoScan == null) {
            scanForMusicCheck.setSelection(false);
        } else {
            scanForMusicCheck.setSelection(autoScan.equals("true"));
        }

        scanMusicInText
                .setText(p.getProperty("auto_scan_directory") == null ? "" : p
                        .getProperty("auto_scan_directory"));
    }

    protected void saveData() throws IOException {
        Properties p = properties.getProperties();
        p.setProperty("decoding_method", decodingMethodCombo.getText());
        p.setProperty("allow_sending_associations", (allowSendingCheck
                .getSelection() ? "true" : "false"));
        p.setProperty("auto_scan_for_music",
                (scanForMusicCheck.getSelection() ? "true" : "false"));
        if (scanForMusicCheck.getSelection()) {
            p.setProperty("auto_scan_directory", scanMusicInText.getText());
        }

        // custom album art file names
        boolean found;
        String items = "";
        for (String item : albumArtFileList.getItems()) {
            found = false;
            for (String defaultItem : AlbumArtCanvas.ALBUM_ART_FILES) {
                if (item.equals(defaultItem)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                if (items.length() > 0) {
                    items += "|";
                }
                items += item.trim();
            }
        }

        p.setProperty("album_art_file_names", items);

        properties.save();
    }

    private void initListeners() {
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                try {
                    saveData();
                } catch (IOException ioe) {
                    MessageBox msg = new MessageBox(getShell(), SWT.ICON_ERROR
                            | SWT.OK);
                    msg.setText("Error");
                    msg
                            .setMessage("There was an error while saving your preferences");
                    msg.open();
                }
                close();
            }
        });

        cancelButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                close();
            }
        });

        scanMusicButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                dialog.setMessage("Select the directory you wish "
                        + Constants.PROGRAM_NAME
                        + " to keep scanning for music files in:");
                String dir = dialog.open();
                if (dir != null && new File(dir).isDirectory()) {
                    scanMusicInText.setText(dir);
                    scanForMusicCheck.setSelection(true);
                }
            }
        });

        addAlbumArtFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                InputDialog dialog = new InputDialog(getShell(),
                        "New File name", "Enter a new file name", "",
                        new IInputValidator() {
                            public String isValid(String newText) {
                                if (newText.length() == 0)
                                    return "Please enter a value";
                                if (newText.indexOf("|") != -1) {
                                    return "Please don't enter | in your file name";
                                }
                                return null;
                            }
                        });

                if (dialog.open() == Window.OK
                        && albumArtFileList.indexOf(dialog.getValue()) == -1) {
                    albumArtFileList.add(dialog.getValue());
                }
            }
        });

        delAlbumArtFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                albumArtFileList.remove(albumArtFileList.getSelectionIndex());
            }
        });

        defaultAlbumArtButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                albumArtFileList.removeAll();
                initDefaultAlbumArtFileList();
            }
        });

        albumArtFileList.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                delAlbumArtFileButton
                        .setEnabled(!(albumArtFileList.getSelectionIndex() < AlbumArtCanvas.ALBUM_ART_FILES.length));

            };
        });
    }

    private Composite initGeneralTab(TabFolder folder) {
        Label label;
        FormData fd;
        Label separator;

        Composite composite = new Composite(folder, SWT.NONE);
        composite.setLayout(new FormLayout());

        // album art file names label
        label = new Label(composite, SWT.NONE);
        label.setText("Album art file names");
        fd = new FormData();
        fd.left = new FormAttachment(0, 30);
        fd.top = new FormAttachment(0, 10);
        fd.right = new FormAttachment(100, -10);
        label.setLayoutData(fd);

        albumArtFileList = new List(composite, SWT.BORDER | SWT.V_SCROLL);
        fd = new FormData();
        fd.top = new FormAttachment(label, 5, SWT.BOTTOM);
        fd.left = new FormAttachment(label, 20, SWT.LEFT);
        fd.width = 150;
        fd.height = 90;
        albumArtFileList.setLayoutData(fd);

        // add/remove buttons ("+") and ("-")
        addAlbumArtFileButton = new Button(composite, SWT.NONE);
        addAlbumArtFileButton.setText("+");
        fd = new FormData();
        fd.top = new FormAttachment(albumArtFileList, 0, SWT.TOP);
        fd.width = 25;
        fd.left = new FormAttachment(albumArtFileList, 5, SWT.RIGHT);
        addAlbumArtFileButton.setLayoutData(fd);

        delAlbumArtFileButton = new Button(composite, SWT.NONE);
        delAlbumArtFileButton.setText("-");
        fd = new FormData();
        fd.top = new FormAttachment(addAlbumArtFileButton, 5, SWT.BOTTOM);
        fd.left = new FormAttachment(albumArtFileList, 5, SWT.RIGHT);
        fd.width = 25;
        delAlbumArtFileButton.setLayoutData(fd);

        defaultAlbumArtButton = new Button(composite, SWT.NONE);
        defaultAlbumArtButton.setText("Default");
        fd = new FormData();
        fd.left = new FormAttachment(albumArtFileList, 5, SWT.RIGHT);
        fd.bottom = new FormAttachment(albumArtFileList, 0, SWT.BOTTOM);
        defaultAlbumArtButton.setLayoutData(fd);
        
        

        separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        fd = new FormData();
        fd.top = new FormAttachment(albumArtFileList, 10, SWT.BOTTOM);
        fd.left = new FormAttachment(label, 0, SWT.LEFT);
        fd.right = new FormAttachment(100, -10);
        separator.setLayoutData(fd);

        scanForMusicCheck = new Button(composite, SWT.CHECK);
        fd = new FormData();
        fd.top = new FormAttachment(separator, 10, SWT.BOTTOM);
        fd.right = new FormAttachment(label, -10, SWT.LEFT);

        scanForMusicCheck.setLayoutData(fd);

        label = new Label(composite, SWT.NONE);
        label.setText("Automatically find new music in (not yet working)");
        fd = new FormData();
        fd.left = new FormAttachment(separator, 0, SWT.LEFT);
        fd.right = new FormAttachment(100, -10);
        fd.top = new FormAttachment(scanForMusicCheck, 0, SWT.TOP);
        label.setLayoutData(fd);

       
      

        
        scanMusicButton = new Button(composite, SWT.NONE);
        scanMusicButton.setText("Browse...");
        fd = new FormData();
        fd.right = new FormAttachment(100, -10);
        fd.top = new FormAttachment(label, 5, SWT.BOTTOM);
        scanMusicButton.setLayoutData(fd);

        scanMusicInText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        fd = new FormData();
        fd.left = new FormAttachment(separator, 0, SWT.LEFT);
        fd.right = new FormAttachment(scanMusicButton, -5, SWT.LEFT);
        fd.top = new FormAttachment(scanMusicButton, 0, SWT.CENTER);
        fd.bottom = new FormAttachment(100, -10);
        scanMusicInText.setLayoutData(fd);
      
        return composite;
    }

    private Composite initAdvancedTab(TabFolder folder) {
        Label label;
        FormData fd;

        Composite composite = new Composite(folder, SWT.NONE);
        composite.setLayout(new FormLayout());

        // album art file names label
        label = new Label(composite, SWT.NONE);
        label.setText("Decoding Mode");
        fd = new FormData();
        fd.left = new FormAttachment(0, 30);
        fd.top = new FormAttachment(0, 10);
        label.setLayoutData(fd);

        decodingMethodCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        fd = new FormData();
        fd.left = new FormAttachment(label, 10, SWT.RIGHT);
        fd.top = new FormAttachment(label, 0, SWT.CENTER);
        fd.width = 100;
        decodingMethodCombo.setLayoutData(fd);

        return composite;
    }

    public static void main(String[] args) {
        PropertiesLoader p = new PropertiesLoader();
        try {
            p.load();
        } catch (IOException e) {
        }
        ;
        PreferencesWindow window = new PreferencesWindow(new Shell(
                new Display()), p);
        window.setBlockOnOpen(true);
        window.open();
    }

}
