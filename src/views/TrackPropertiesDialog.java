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

import helpers.FileUtil;
import helpers.StringUtilities;

import java.io.File;

import models.Track;
import net.ffxml.swtforms.extras.DefaultFormBuilder;
import net.ffxml.swtforms.layout.FormLayout;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TrackPropertiesDialog extends ApplicationWindow {
    private Label albumLabel;
    private Label typeLabel;
    private Label sizeLabel;
    private Label pathLabel;

    public TrackPropertiesDialog(Shell parent) {
        super(parent);
        setShellStyle(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 10dlu, 200dlu", "");

        DefaultFormBuilder builder = new DefaultFormBuilder(composite, layout);
        builder.setDefaultDialogBorder();

        // builder.appendSeparator("TEST");

        builder.append("Album", albumLabel = new Label(composite, SWT.NONE));
        builder.nextLine();
        builder.append("Type", typeLabel = new Label(composite, SWT.NONE));
        builder.nextLine();
        builder.append("Size", sizeLabel = new Label(composite, SWT.NONE));
        builder.nextLine();
        builder.append("Path", pathLabel = new Label(composite, SWT.NONE));
        return composite;
    }

    public void setTrack(Track track) throws IllegalArgumentException {
        if (track == null)
            throw new IllegalArgumentException("Track can not be null");
        
        File file = new File(track.getPath());
        albumLabel.setText(track.getAlbum());
        typeLabel.setText(FileUtil.getExtension(track.getPath()));
        sizeLabel.setText(Long.toString(file.length()) + " Bytes");
        pathLabel.setText(track.getPath());
        pathLabel.setToolTipText(track.getPath());

        getShell().setText(
                StringUtilities.escapeAmpersands(track.getArtist() + " - "
                        + track.getTitle()));
    }

    public static void main(String[] args) {
        TrackPropertiesDialog test = new TrackPropertiesDialog(new Shell(
                new Display()));
        test.setBlockOnOpen(true);
        test.open();
    }
}
