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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import models.MetaData;
import models.PlayerCallBackData;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import controllers.Player;
import data.PropertiesLoader;

/**
 * A Canvas object which is designed to display an album art image that resizes
 * with the canvas
 * 
 * @author Michael Voong
 * 
 */
public class AlbumArtCanvas extends Canvas implements PlayerListener {
    private Image image;
    private FormData layoutData;
    private String lastDirPath;

    /**
     * Max image width (resizes to this on start);
     */
    private static final int IMAGE_WIDTH = 300;

    public static final String[] ALBUM_ART_FILES = { "folder.jpg", "cover.jpg",
            "cd.jpg", "Folder.jpg", "CD.jpg", "folder.png", "artwork.jpg" };
    private PropertiesLoader propertiesLoader;

    public AlbumArtCanvas(Composite composite, FormData layoutData, int args) {
        super(composite, args);
        this.layoutData = layoutData;
        this.addListener(SWT.Paint, new Painter());

        String tooltip = "";
        tooltip += "Finds images called ";
        int i = 0;
        for (String file : ALBUM_ART_FILES) {
            tooltip += "\"" + file + "\"";
            if (i < ALBUM_ART_FILES.length - 1) {
                tooltip += ", ";
            }
            i++;
        }
        tooltip += " in folders";
        setToolTipText(tooltip);

    }

    public void message(String message, Player player) {
        if (message.equals("playing")) {
            MetaData data = player.getMetaData();
            
            if (data == null)
                return;

            File file = new File(data.getPath());
            final String folder = file.getParent();

            if (folder != null) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        findFolderImage(folder);
                        redraw();
                    }
                });

            }
        }
    }

    private void findFolderImage(String dirPath) {
        // don't update if already have the image
        if (lastDirPath != null && lastDirPath.equals(dirPath)) {
            return;
        } else {
            lastDirPath = dirPath;
        }

        final File dir = new File(dirPath);

        if (dir.isDirectory()) {
            // merge constant list with user preferences
            List<String> artFileNames = getAlbumArtFileNames();

            for (String artFileName : artFileNames) {
                File file = new File(dir, artFileName);
                if (file.isFile()) {
                    try {
                        cleanUp();
                        image = ImageDescriptor.createFromURL(
                                new URL("file", null, file.getAbsolutePath()))
                                .createImage();
                        image = scaleImage(image);
                        resizeCanvas();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    break;
                } else {
                    image = null;
                    resizeCanvas();
                }
            }
        }
    }

    private List<String> getAlbumArtFileNames() {
        if (propertiesLoader == null)
            return null;

        String fileNames = propertiesLoader.getProperties().getProperty(
                "album_art_file_names");
        ArrayList<String> list = new ArrayList<String>();
        if (fileNames != null) {
            String[] split = fileNames.split("\\|");
            for (String item : split) {
                list.add(item.trim());
            }
        }

        for (String item : AlbumArtCanvas.ALBUM_ART_FILES) {
            list.add(item);
        }

        return list;
    }

    /**
     * Scales the image to fit the canvas boundaries
     * 
     * @param oldImage
     * @return
     */
    private Image scaleImage(Image oldImage) {
        ImageData data = image.getImageData();

        // if too big, scale to IMAGE_WIDTH
        if (data.width > IMAGE_WIDTH) {
            data = data.scaledTo(IMAGE_WIDTH,
                    (int) (((double) IMAGE_WIDTH / data.width) * data.height));
        }
        oldImage.dispose();

        return new Image(Display.getCurrent(), data);
    }

    /**
     * Resizes the canvas to match the image size
     */
    public void resizeCanvas() {
        if (image != null) {
            layoutData.width = getSize().x;
            layoutData.height = (int) (((double) getSize().x / image
                    .getImageData().width) * image.getImageData().height);

        } else {
            // layoutData.width = 0;
            layoutData.height = 120;
        }
        getParent().layout(new Control[] { this });
    }

    public void callback(PlayerCallBackData data) {
    }

    /**
     * Renders the image on the canvas
     * 
     * @param imageBuffer
     */
    public void renderImage(ByteBuffer imageBuffer) {
        if (imageBuffer == null) {
            image = null;
            redraw();
        } else {

            byte[] bytes = new byte[imageBuffer.capacity() - 1];

            imageBuffer.rewind();
            imageBuffer.get();

            int i = 0;
            int count = 0;

            boolean start = false;

            while (imageBuffer.hasRemaining()) {
                byte b = imageBuffer.get();
                char c = (char) b;

                if (start) {
                    bytes[i] = b;
                    i++;
                }
                if (!start && c == 0x0) { // hex 0x0
                    if (count == 2) { // two 0x0 before the actual binary data
                        start = true;
                    } else {
                        count++;
                    }
                }
                // System.out.print(imageBuffer.getChar());

            }
            //
            // try {
            // FileOutputStream writer = new FileOutputStream("e:/test.jpg");
            // writer.write(bytes);
            // } catch (IOException ioe) {
            // }

            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            image = new Image(Display.getCurrent(), input);
            redraw();
        }
    }

    public void cleanUp() {
        if (image != null) {
            image.dispose();
        }
    }

    /**
     * This class paints handles the actual image painting
     * 
     * @author Michael Voong
     * 
     */
    class Painter implements Listener {
        public void handleEvent(Event e) {
            if (image != null) {
                e.gc.setAntialias(SWT.ON);
                e.gc.drawImage(image, 0, 0, image.getBounds().width, image
                        .getBounds().height, 0, 0, layoutData.width,
                        layoutData.height);

            } else {
                e.gc.setForeground(Display.getDefault().getSystemColor(
                        SWT.COLOR_BLACK));
                e.gc.drawString("No Cover", 5, 5);
            }
        }
    }

    public void setPropertiesLoader(PropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
    }
}
