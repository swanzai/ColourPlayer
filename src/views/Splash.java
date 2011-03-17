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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import controllers.LoadingThread;

public class Splash {
    private final LoadingThread loadingThread;

    private final int itemCount = 4;

    public Splash(final LoadingThread loadingThread) {
        this.loadingThread = loadingThread;

        final Display display = new Display();
        Image image = null;

        try {
            image = ImageDescriptor.createFromURL(
                    new URL("file:images/splash.jpg")).createImage();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        final Shell splash = new Shell(SWT.ON_TOP);
        final ProgressBar bar = new ProgressBar(splash, SWT.NONE);

        bar.setMaximum(itemCount);
        Label label = new Label(splash, SWT.NONE);

        Label imageLabel = new Label(splash, SWT.NONE);
        imageLabel.setImage(image);

        // layouts
        FormLayout layout = new FormLayout();
        splash.setLayout(layout);
        FormData labelData = new FormData();

        labelData.bottom = new FormAttachment(100, -5);
        labelData.left = new FormAttachment(0, 5);
        labelData.right = new FormAttachment(100, -5);
        label.setLayoutData(labelData);

        FormData progressData = new FormData();
        progressData.left = new FormAttachment(0, 5);
        progressData.right = new FormAttachment(100, -5);
        progressData.bottom = new FormAttachment(label, -5, SWT.TOP);
        bar.setLayoutData(progressData);

        FormData imageLabelData = new FormData();
        imageLabelData.top = new FormAttachment(0, 5);
        imageLabelData.bottom = new FormAttachment(bar, -5, SWT.TOP);
        imageLabelData.left = new FormAttachment(0, 5);
        imageLabelData.right = new FormAttachment(100, -5);
        imageLabel.setLayoutData(imageLabelData);

        splash.pack();
        splash.open();

        loadingThread.start();

        while (!loadingThread.isLoaded()) {
            if (!display.readAndDispatch()) {
                label.setText(loadingThread.getText());
                bar.setSelection(loadingThread.getProgress());
                display.sleep();
            }
        }
        loadingThread.setEnd(true);
        splash.close();
    }
}
