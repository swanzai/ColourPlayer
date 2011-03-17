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

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import config.Constants;

public class AboutDialog extends ApplicationWindow {

    private static final String ABOUT_STRING = Constants.PROGRAM_NAME
            + " is a final year project at The University of Birmingham by Michael Voong.";
    private static final String VERSION_STRING = Constants.VERSION + "\n"
            + "NativeFModEx\n" + "HSQLDB\nGlazedLists";
    private Label label1;
    private Link link;
    private Label label3;
    private Button okButton;
    private Label image;
    private Image splashImage;

    public AboutDialog(Shell shell) {
        super(shell);

        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    @Override
    protected Control createContents(Composite parent) {
        parent.getShell().setText("About");

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FormLayout());

        image = new Label(composite, SWT.NONE);
        label1 = new Label(composite, SWT.WRAP);
        link = new Link(composite, SWT.BORDER | SWT.CENTER);
        label3 = new Label(composite, SWT.WRAP);
        okButton = new Button(composite, SWT.NONE);

        splashImage = new Image(getShell().getDisplay(), "images/splash.jpg");   
        image.setImage(splashImage);
        okButton.setText("OK");
        
        FormData fd;
        
        fd = new FormData();
        fd.top = new FormAttachment(0, 0);
        fd.left = new FormAttachment(0, 0);
        fd.right = new FormAttachment(100, 0);
        image.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(image, 10, SWT.BOTTOM);
        fd.left = new FormAttachment(0, 10);
        fd.right = new FormAttachment(100, -10);
        label1.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(label1, 15, SWT.BOTTOM);
        fd.left = new FormAttachment(0, 10);
        fd.right = new FormAttachment(100, -10);
        link.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(link, 15, SWT.BOTTOM);
        fd.right = new FormAttachment(100, -10);
        fd.width = 80;
        okButton.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(link, 15, SWT.BOTTOM);
        fd.left = new FormAttachment(0, 10);
        fd.right = new FormAttachment(okButton, -5, SWT.LEFT);
        fd.bottom = new FormAttachment(100, -10);

        label3.setLayoutData(fd);

        initListeners();
        initLabels();

        okButton.setFocus();

//        parent.getShell().setSize(splashImage.getImageData().width, 400);
        parent.getShell().pack();
        
        return composite;
    }

    private void initListeners() {
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                close();
            }
        });

        link.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Program.launch(Constants.WEB_ADDRESS);
            }
        });
    }

    private void initLabels() {
        label1.setText(ABOUT_STRING);
        link.setText("<a>" + Constants.WEB_ADDRESS + "</a>");
        label3.setText(VERSION_STRING);
    }
}
