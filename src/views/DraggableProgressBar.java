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

import helpers.StringUtilities;

import java.util.ArrayList;

import listeners.ProgressDragListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class DraggableProgressBar extends Canvas {
    private int style;

    private int value = 0;
    private int maxValue = 0;
    private int draggingValue;

    private ArrayList<ProgressDragListener> dragListeners;

    private boolean dragging;

    public DraggableProgressBar(Composite parent, int style) {
        super(parent, SWT.DOUBLE_BUFFERED);

        this.style = style;

        addPaintListener(new DraggableProgressBarPaintListener());
        ProgressBarMouseListener listener = new ProgressBarMouseListener();
        addMouseListener(listener);
        addMouseMoveListener(listener);
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        redraw();
    }

    /**
     * Sets the value of the progress bar
     * 
     * @param value
     */
    public void setValue(final int value) {
        this.value = value;

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                redraw();
                setToolTipText(StringUtilities.msToString(value) + "/"
                        + StringUtilities.msToString(maxValue));
            }
        });
    }

    /**
     * Called while progress bar is being dragged
     * 
     * @param position
     */
    protected void draggedTo(int position) {
        draggingValue = position;
        dragging = true;
        redraw();
    }

    /**
     * Called when the drag has been released
     */
    private void dragSet() {
        value = draggingValue;

        if (dragListeners != null)
            for (ProgressDragListener l : dragListeners) {
                l.dragPerformed(value);
            }

        dragging = false;
        draggingValue = 0;

    }

    public void addDragListener(ProgressDragListener listener) {
        if (dragListeners == null) {
            dragListeners = new ArrayList<ProgressDragListener>();
        }
        dragListeners.add(listener);
    }

    class DraggableProgressBarPaintListener implements PaintListener {
        Color background;
        Color foreground;

        public DraggableProgressBarPaintListener() {
            this.background = Display.getDefault().getSystemColor(
                    SWT.COLOR_WHITE);
            this.foreground = Display.getDefault().getSystemColor(
                    SWT.COLOR_DARK_GRAY);
        }

        public void paintControl(PaintEvent e) {
            if (getEnabled()) {
                int width;

                if (dragging) {
                    width = (int) Math
                            .round(((double) draggingValue / maxValue)
                                    * getSize().x);

                } else {
                    width = (int) Math.round(((double) value / maxValue)
                            * getSize().x);
                }

                e.gc.setBackground(foreground);
                e.gc.fillRectangle(2, 2, Math.min(getSize().x - 4, Math.max(2,
                        width - 4)), getSize().y - 4);
                e.gc.setForeground(foreground);
                e.gc.drawRectangle(0, 0, getSize().x - 1, getSize().y - 1);
            }
        }
    }

    class ProgressBarMouseListener implements MouseListener, MouseMoveListener {
        private boolean mouseDown = false;

        public void mouseDoubleClick(MouseEvent e) {
        }

        public void mouseDown(MouseEvent e) {
            if (e.button == 1 || e.button == 3) {
                mouseDown = true;
            }
        }

        public void mouseUp(MouseEvent e) {
            mouseDown = false;
            draggedTo(getNormalisedVal(e.x));
            dragSet();
        }

        public void mouseMove(MouseEvent e) {
            if (mouseDown) {
                draggedTo(getNormalisedVal(e.x));
            }
        }

        private int getNormalisedVal(int x) {
            int normalised = Math.min(x, getSize().x);
            normalised = Math.max(normalised, 0);

            return (int) Math.round(((double) normalised / getSize().x)
                    * maxValue);
        }
    }
}
