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

import java.util.ArrayList;
import java.util.List;

import listeners.ColourPickerListener;
import models.PlayerCallBackData;
import models.PlaylistDao;
import models.Track;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import ca.odell.glazedlists.EventList;
import controllers.Player;

public class ColourPickerCanvasWrapper implements PlayerListener,
        SelectionListener {
    private Canvas canvas;
    private PaintListener paintListener;
    private Color currentColour;
    private Color currentTweakedColour;
    private int[] pointerPosition;
    private int[] saturationPointerPosition;
    private ArrayList<ColourPickerListener> listeners;
    private boolean enabled;
    private PlaylistDao playlist;

    /**
     * Stores the current repaint type so that the painter can optimize what it
     * needs to paint
     */
    private int repaintType = CANVAS_REPAINT_TYPE;

    private static final int PICKING_REPAINT_TYPE = 23;
    private static final int CANVAS_REPAINT_TYPE = 24;

    private final int BODY_OFFSET = 20;
    private final int SATURATION_BAR_WIDTH = 10;
    public int use;
    private EventList<Track> eventList;
    private List<Track> referenceTracks;
    private ColorRegistry colourRegistry;

    public ColourPickerCanvasWrapper(Composite parent) {
        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);

        canvas
                .addPaintListener(paintListener = new ColourPickerPaintListener());

        ColourPickerMouseListener mouseListener = new ColourPickerMouseListener();
        canvas.addMouseMoveListener(mouseListener);
        canvas.addMouseListener(mouseListener);

        // colourRegistry = new ArrayList<Color>();
        this.repaintType = CANVAS_REPAINT_TYPE;
        colourRegistry = new ColorRegistry();
    }

    public void setPlaylist(PlaylistDao playlist) {
        this.playlist = playlist;
    }

    public void setEventList(EventList<Track> eventList) {
        this.eventList = eventList;

    }

    public void addListener(ColourPickerListener l) {
        if (listeners == null)
            listeners = new ArrayList<ColourPickerListener>();

        listeners.add(l);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            canvas.redraw();
        }
    }

    public void notifyListeners() {
        if (listeners == null)
            return;

        for (ColourPickerListener l : listeners) {
            if (currentColour != null || currentTweakedColour != null) {
                if (use == 1) {
                    l.pickedColour(referenceTracks, currentColour.getRed(),
                            currentColour.getGreen(), currentColour.getBlue());

                } else {
                    l.pickedColour(referenceTracks, currentTweakedColour
                            .getRed(), currentTweakedColour.getGreen(),
                            currentTweakedColour.getBlue());
                }
            } else {
                // clear colour
                System.out.println("Clear colour");
                l.clearedColour(referenceTracks);
            }

        }
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void widgetSelected(SelectionEvent e) {
        if (e.widget instanceof Table) {
            // use the currently selected track as a reference for colour picker
            Table table = (Table) e.widget;
            int[] indices = table.getSelectionIndices();
            List<Track> tracks = new ArrayList<Track>();

            for (int index : indices) {
                tracks.add(eventList.get(index));
            }
            if (tracks.size() > 0)
                setReferenceTracks(tracks);
        }
    }

    public void setReferenceTracks(List<Track> referenceTracks) {
        if (referenceTracks == null || referenceTracks.size() == 0)
            throw new IllegalArgumentException(
                    "Null or empty reference tracks given");

        if (referenceTracks.size() == 1) {
            this.setReferenceTrack(referenceTracks.get(0));
            return;
        }

        this.referenceTracks = referenceTracks;
        updateColour();
        setEnabled(true);
        canvas.setToolTipText("Choose colour for " + referenceTracks.size()
                + " tracks");
    }

    public void setReferenceTrack(Track track) {
        if (track == null)
            throw new IllegalArgumentException("Null track given");

        this.referenceTracks = new ArrayList<Track>(1);
        referenceTracks.add(track);

        updateColour();
        setEnabled(true);
        canvas.setToolTipText("Choose colour for " + track.getArtist() + " - "
                + track.getTitle());
    }

    private void updateColour() {
        int[] colour = null;

        if (referenceTracks.size() == 1) {
            colour = referenceTracks.get(0).getColour();
        } else {
            boolean same = true;

            for (Track track : referenceTracks) {
                // set colour of picker to this track's colour
                int[] colour2 = track.getColour();
                if (colour == null || colour2 == null) {
                    colour = null;
                    break;
                } else if (colour[0] != colour2[0] || colour[1] != colour2[1]
                        || colour[2] != colour2[2]) {
                    colour = null;
                    break;
                }
                colour = colour2;
            }
        }

        if (colour != null) {
            setCurrent(new Color(Display.getCurrent(), colour[0], colour[1],
                    colour[2]));
        } else {
            setCurrent(null);
        }
        canvas.redraw();
    }

    public void cleanUp() {
        // for (Color colour : colourRegistry) {
        // colour.dispose();
        // }
        // colourRegistry.clear();
    }

    public int[] hsvToRgb(float[] hsv) {
        java.awt.Color color = new java.awt.Color(java.awt.Color.HSBtoRGB(
                hsv[0], hsv[1], hsv[2]));
        return new int[] { color.getRed(), color.getGreen(), color.getBlue() };
    }

    public float[] rgbToHsv(int[] rgb) {
        return java.awt.Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
    }

    public void callback(PlayerCallBackData data) {
    }

    public void message(String message, Player player) {
        if (message.equals("playing")) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    resetPicker();
                    if (playlist != null && playlist.getTrack() != null) {
                        Track track = playlist.getTrack();
                        setReferenceTrack(track);
                    }
                    setEnabled(true);
                };
            });

        } else if (message.equals("stopped")) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    setEnabled(false);
                };
            });
        }
    }

    private void resetPicker() {
        setCurrent(null);
        setCurrentTweaked(null);
        use = 1;
        resetPointerPositions();
        canvas.redraw();
    }

    /**
     * Sets the current colour
     * 
     * @param colour
     */
    public void setCurrent(Color colour) {
        this.currentColour = colour;
    }

    /**
     * Clears the current colour and notify its listeners and redraws the canvas
     */
    public void clearCurrent() {
        resetPicker();
        notifyListeners();
    }

    private void setCurrentTweaked(Color color) {
        this.currentTweakedColour = color;
    }

    public Color getCurrent() {
        return currentColour;
    }

    public void resetPointerPositions() {
        this.pointerPosition = null;
        this.saturationPointerPosition = null;
    }

    public void setPointerPosition(int x, int y) {
        if (this.pointerPosition == null) {
            this.pointerPosition = new int[2];
        }

        this.pointerPosition[0] = x;
        this.pointerPosition[1] = y;
    }

    private void setSaturationPointerPosition(int x, int y) {
        saturationPointerPosition = new int[] { x, y };
    }

    class ColourPickerPaintListener implements PaintListener {
        /**
         * Must be divisible by 2
         */
        int pointerWidth = 10;

        int pointerHeight = 10;

        final Color cursorOuter = Display.getDefault().getSystemColor(
                SWT.COLOR_BLACK);

        final Color cursorInner = Display.getDefault().getSystemColor(
                SWT.COLOR_WHITE);

        public void paintControl(PaintEvent e) {
            // paint body
            int width = canvas.getSize().x - SATURATION_BAR_WIDTH - 1;
            int height = canvas.getSize().y;

            int[] rgb;
            Color colour;

            float widthProportion = 1.0f / (width - 1);
            float heightProportion = 1.0f / (height - 1);
            float rowValue;

            for (int j = 0; j < height; j++) {
                rowValue = heightProportion * j;

                for (int i = 0; i < width; i++) {
                    rgb = hsvToRgb(new float[] { widthProportion * i, 1,
                            rowValue });

                    RGB col = new RGB(rgb[0], rgb[1], rgb[2]);
                    colourRegistry.put(col.toString(), col);

                    e.gc.setForeground(colourRegistry.get(col.toString()));
                    e.gc.drawPoint(i, BODY_OFFSET + j);
                }

                // saturation bar
                if (currentColour != null) {
                    for (int i = width + 1; i < width + 1
                            + SATURATION_BAR_WIDTH; i++) {
                        float[] awtCurColour = java.awt.Color.RGBtoHSB(
                                currentColour.getRed(), currentColour
                                        .getGreen(), currentColour.getBlue(),
                                null);
                        rgb = hsvToRgb(new float[] { awtCurColour[0], rowValue,
                                awtCurColour[2] });

                        RGB col = new RGB(rgb[0], rgb[1], rgb[2]);
                        colourRegistry.put(col.toString(), col);
                        e.gc.setForeground(colourRegistry.get(col.toString()));
                        e.gc.drawPoint(i, BODY_OFFSET + j);
                    }
                }
            }

            // text "no track" if picker is disabled
            if (!enabled) {
                e.gc.setForeground(cursorInner); // white
                e.gc.drawString("No Track", 20, 40, true);
            }

            // paint currently picked colour
            if (currentColour != null || currentTweakedColour != null) {
                if (use == 2) {
                    e.gc.setBackground(currentTweakedColour);
                } else if (currentColour != null) {
                    e.gc.setBackground(currentColour);
                }

                e.gc.fillRectangle(0, 0, width + 1 + SATURATION_BAR_WIDTH,
                        BODY_OFFSET - 2);

                // paint cursor
                if (pointerPosition != null) {
                    e.gc.setAntialias(SWT.ON);
                    // draw main cursor at main body
                    e.gc.setForeground(cursorOuter);
                    e.gc.drawOval(pointerPosition[0] - pointerWidth / 2,
                            pointerPosition[1] - pointerWidth / 2,
                            pointerWidth, pointerHeight);
                    e.gc.setForeground(cursorInner);
                    e.gc.drawOval(pointerPosition[0] + 1 - pointerWidth / 2,
                            pointerPosition[1] + 1 - pointerWidth / 2,
                            pointerWidth - 2, pointerHeight - 2);
                    e.gc.setAntialias(SWT.OFF);
                }
                if (saturationPointerPosition != null) {
                    // if (pointerPosition[0] > width) {
                    // draw saturation bar indicator
                    e.gc.setForeground(cursorOuter);
                    e.gc.drawLine(width + 2, saturationPointerPosition[1],
                            width + 2 + BODY_OFFSET,
                            saturationPointerPosition[1]);

                }
            }

            // paint header/body line
            e.gc.setForeground(cursorOuter);
            e.gc.drawLine(0, BODY_OFFSET - 2, width + 1 + SATURATION_BAR_WIDTH,
                    BODY_OFFSET - 2);
        }
    }

    class ColourPickerMouseListener implements MouseListener, MouseMoveListener {
        public void mouseMove(MouseEvent e) {
            if (enabled && e.stateMask == SWT.BUTTON1) {
                int width = canvas.getSize().x - SATURATION_BAR_WIDTH - 1;
                int height = canvas.getSize().y;

                // cap inside boundaries
                if (e.y <= BODY_OFFSET) {
                    e.y = BODY_OFFSET + 1;
                }
                if (e.x < 1) {
                    e.x = 1;
                }

                if (e.y > height) {
                    e.y = height;
                }

                if (e.x <= width) {
                    float widthProportion = 1.0f / (width - 1);
                    float heightProportion = 1.0f / (height - 1);

                    int[] rgb = hsvToRgb(new float[] { widthProportion * e.x,
                            1, heightProportion * e.y });

                    RGB col = new RGB(rgb[0], rgb[1], rgb[2]);
                    colourRegistry.put(col.toString(), col);

                    setCurrent(colourRegistry.get(col.toString()));
                    setPointerPosition(e.x, e.y);
                    setSaturationPointerPosition(width + 1, e.y);
                    use = 1;
                } else if (currentColour != null) {
                    // saturation bar
                    float[] currentHsv = rgbToHsv(new int[] {
                            currentColour.getRed(), currentColour.getGreen(),
                            currentColour.getBlue() });
                    float saturation = (1f / (height - BODY_OFFSET))
                            * (e.y - BODY_OFFSET);
                    int[] rgb = hsvToRgb(new float[] { currentHsv[0],
                            saturation, currentHsv[2] });

                    RGB col = new RGB(rgb[0], rgb[1], rgb[2]);
                    colourRegistry.put(col.toString(), col);

                    setCurrentTweaked(colourRegistry.get(col.toString()));
                    // canvas.redraw(width + 1, BODY_OFFSET, width + 1 +
                    // SATURATION_BAR_WIDTH, height, false);

                    setSaturationPointerPosition(e.x, e.y);
                    use = 2;
                }

                repaintType = PICKING_REPAINT_TYPE;
                canvas.redraw();
            }
        }

        public void mouseDoubleClick(MouseEvent e) {
        }

        public void mouseDown(MouseEvent e) {
            mouseMove(e);
        }

        public void mouseUp(MouseEvent e) {
            if (e.button == 1 && enabled) {
                repaintType = CANVAS_REPAINT_TYPE;
                notifyListeners();
            }
        }

    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void removeAllListeners() {
        if (listeners != null) {
            listeners.clear();
        }
    }

}