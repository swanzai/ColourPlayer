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

import models.Track;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ProfileSubmittingDialog extends ApplicationWindow {
    private static final String DESCRIPTION = "Submit your colour associations to aid the development of ColourMusic by giving us a chance to spot patterns in your colour association. Colour associations are normally submitted automatically unless you have a firewall enabled.\n\nSimply click \"Send Data\" and wait a few moments.\n\nClick on \"Show Detail\" to view the information sent to ColourMusic\n\nIf you have a firewall enabled, please allow ColourMusic access to the Internet.";
    private Label description;
    private Button okButton;
    private Button showDetailButton;
    private Text detail;
    private ProfileWriter writer;
    private final Track[] tracks;
    private Thread submitThread;
    private ProgressBar progressBar;

    public ProfileSubmittingDialog(Shell shell, Track[] tracks, int uniqueId) {
        super(shell);
        this.tracks = tracks;

        writer = new ProfileWriter(uniqueId);
        writer.process(tracks);
    }

    @Override
    protected int getShellStyle() {
        return SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;

    }

    @Override
    protected Control createContents(final Composite parent) {
        parent.getShell().setText("Submit Colour Associations");

        Composite composite = new Composite(parent, SWT.NONE);

        composite.setLayout(new FormLayout());

        description = new Label(composite, SWT.WRAP);
        okButton = new Button(composite, SWT.NONE);
        showDetailButton = new Button(composite, SWT.TOGGLE);
        detail = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        progressBar = new ProgressBar(composite, SWT.INDETERMINATE);

        // format items

        detail.setVisible(false);
        progressBar.setVisible(false);

        description.setText("You have assigned a colour to "
                + writer.getCount() + " tracks. " + DESCRIPTION);
        okButton.setText("&Send Data");
        showDetailButton.setText("Show &Details");

        // layout
        FormData fd = new FormData();
        fd.left = new FormAttachment(0, 10);
        fd.right = new FormAttachment(100, -10);
        fd.top = new FormAttachment(0, 10);
        fd.width = 300;
        description.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(description, 10, SWT.BOTTOM);
        fd.left = new FormAttachment(0, 10);
        fd.right = new FormAttachment(100, -10);
        progressBar.setLayoutData(fd);

        fd = new FormData();
        fd.right = new FormAttachment(100, -5);
        fd.top = new FormAttachment(progressBar, 10, SWT.BOTTOM);
        showDetailButton.setLayoutData(fd);

        fd = new FormData();
        fd.right = new FormAttachment(showDetailButton, -5, SWT.LEFT);
        fd.top = new FormAttachment(progressBar, 10, SWT.BOTTOM);
        okButton.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(0, 5);
        fd.right = new FormAttachment(100, -5);
        fd.top = new FormAttachment(okButton, 5, SWT.BOTTOM);
        fd.bottom = new FormAttachment(100, -5);
        fd.height = 0;
        detail.setLayoutData(fd);

        // button listeners
        showDetailButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
            };

            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (detail.getText().length() == 0) {
                    detail.setText(writer.getCompiledXml());
                }

                toggleShowDetail();
            };
        });

        okButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
            };

            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                submitDetails();
            };
        });

        parent.getShell().pack();

        return parent;
    }

    private void submitDetails() {
        // spawn a new thread to submit details
        if (submitThread == null) {
            okButton.setEnabled(false);
            progressBar.setVisible(true);
            submitThread = new Thread(new Runnable() {
                public void run() {
                    writer.submit();

                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            okButton.setEnabled(true);
                            okButton.setText("&Send Data");
                            MessageBox messageBox = new MessageBox(getShell(),
                                    SWT.OK | SWT.ICON_INFORMATION);
                            messageBox.setText("Thank You");
                            messageBox
                                    .setMessage("Your colour profile has been submitted successfully. Thank you for your contribution.");
                            messageBox.open();

                            progressBar.setVisible(false);
                            submitThread = null;
                        };
                    });
                }
            });
            submitThread.start();

        }
    }

    private void toggleShowDetail() {
        boolean hide = showDetailButton.getSelection();
        detail.setVisible(hide);

        if (!hide) {
            showDetailButton.setText("Show &Detail");
        } else {
            showDetailButton.setText("&Hide Detail");
        }

        FormData data = (FormData) detail.getLayoutData();
        data.height = (!hide) ? 0 : 200;
        getShell().layout();
        getShell().pack();
    }

    public static void main(String[] args) {
        Display display;
        Shell shell;
        shell = new Shell(display = new Display());
        ApplicationWindow window = new ProfileSubmittingDialog(shell,
                new Track[] {}, 0);

        window.setBlockOnOpen(true);
        window.open();

        shell.dispose();
        display.dispose();

    }

}
