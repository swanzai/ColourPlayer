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

import glazedlists.matchereditors.ColourMatcherEditor;
import glazedlists.matchers.ColourMatcher;
import helpers.ColourRegistry;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import listeners.ToleranceChangeListener;
import listeners.TraverserChangeListener;
import models.MetaData;
import models.PlaylistDao;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import som.Som;
import som.SomNode;
import controllers.Player;
import controllers.SomTraverser;
import data.Dao;

public class SomCanvasWrapper extends ColourRegistry {
    private Canvas canvas;
    private Som som;
    private SomPaintListener paintListener;
    private PaintListener interactionPaintListener;
    private Player player;
    private MetaData metaData;
    private Font overlayFont;
    private Color colorBlack;
    private boolean drawText = true;
    private SomNode[][] network;
    private Dao dao;
    private boolean[][] nodeVisibility;
    private final Composite parent;
    private SomInteractionListener interactionListener;
    private ColourMatcherEditor matcherEditor;
    private MenuItem clearMenuItem;
    private int tolerance = ColourMatcher.DEFAULT_TOLERANCE;
    private SomTraverser somTraverser;
    private PlaylistDao playlist;
    private MenuItem marqueeModeMenu;
    private MenuItem adaptiveModeMenu;

    public SomCanvasWrapper(Composite parent) {
        this.parent = parent;

        if (somTraverser==null) {
            somTraverser = new SomTraverser();
        }
        
        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED | SWT.BORDER);
        overlayFont = new Font(Display.getCurrent(), "Arial", 16, SWT.BOLD);
        colorBlack = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
        
        initMenu();
        initListeners();       
    }

    private void initListeners() {
        canvas.addPaintListener(paintListener = new SomPaintListener());

        useMarqueeInteractionListener();
    }

    private void removeInteractionListeners() {
        if (interactionListener != null) {
            canvas.removePaintListener(interactionPaintListener);
            canvas.removeMouseListener(interactionListener);
            canvas.removeMouseMoveListener(interactionListener);
            canvas.removeKeyListener(interactionListener);
        }
    }

    private void addInteractionListener(SomInteractionListener listener) {
        canvas.addMouseMoveListener(listener);
        canvas.addMouseListener(listener);
        canvas.addKeyListener(listener);
    }

    public void useMarqueeInteractionListener() {
        if (interactionListener == null || !(interactionListener instanceof MarqueInteractionListener)) {
            removeInteractionListeners();

            interactionListener = new MarqueInteractionListener();
            interactionPaintListener = new MarqueeInteractionPaintListener();
            canvas.addPaintListener(interactionPaintListener);

            addInteractionListener(interactionListener);
            
            if (playlist != null)
                playlist.setEnableStarvingProducer(false);
            marqueeModeMenu.setSelection(true);
            adaptiveModeMenu.setSelection(false);
        }
    }

    public void useAdaptiveInteractionListener() {
        if (interactionListener == null || !(interactionListener instanceof AdaptiveInteractionListener)) {
            removeInteractionListeners();

            interactionListener = new AdaptiveInteractionListener(somTraverser);
            interactionPaintListener = new AdaptiveInteractionPaintListener();
            canvas.addPaintListener(interactionPaintListener);         

            addInteractionListener(interactionListener);
            
            if (playlist != null)
                playlist.setEnableStarvingProducer(true);
            
            marqueeModeMenu.setSelection(false);
            adaptiveModeMenu.setSelection(true);
        }
    }

    protected void setColourTolerance(int tolerance) {
        this.tolerance = tolerance;
        matcherEditor.setTolerance(tolerance);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }

    public void setSom(Som som) {
        this.som = som;
        this.nodeVisibility = new boolean[Som.getWidth()][Som.getHeight()];

        som.addObserver(paintListener);
        network = som.getNetwork();
    }

    public void setPlaylist(PlaylistDao playlist) {
        this.playlist = playlist;
    }

    public void redraw() {
        if (canvas != null && Display.getDefault() != null) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    canvas.redraw();
                };
            });
        }
    }

    public void cleanUp() {
        super.cleanUp();
        overlayFont.dispose();
    }

    public void setColourMatcherEditor(ColourMatcherEditor matcherEditor) {
        this.matcherEditor = matcherEditor;
    }

    private void initMenu() {
        Menu menu = new Menu(canvas.getShell());
        canvas.setMenu(menu);

        clearMenuItem = new MenuItem(menu, SWT.PUSH);
        clearMenuItem.setText("&Clear Filter");
        clearMenuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                matcherEditor.setSelectedNodes(null);
                interactionListener.clearSelectedNodes();
                canvas.redraw();
            };
        });

        MenuItem refresh = new MenuItem(menu, SWT.PUSH);
        refresh.setText("&Refresh");
        refresh.addListener(SWT.Selection, new Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                som.init();
                som.train(Som.INITIAL_LEARNING_RATE, Som.INITIAL_MAP_RADIUS);
                interactionListener.clearSelectedNodes();
            };
        });

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem properties = new MenuItem(menu, SWT.PUSH);
        properties.setText("&Properties");
        properties.addListener(SWT.Selection, new Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                ColourMapPropertiesDialog propertiesWindow = new ColourMapPropertiesDialog(canvas.getShell(), tolerance);
                propertiesWindow.addToleranceChangeListener(new ToleranceChangeListener() {
                    public void toleranceChanged(int tolerance) {
                        setColourTolerance(tolerance);
                    };
                });
                propertiesWindow.open();
            };
        });

        new MenuItem(menu, SWT.SEPARATOR);

        marqueeModeMenu = new MenuItem(menu, SWT.CHECK);
        marqueeModeMenu.setText("&Marquee Mode");
        marqueeModeMenu.addListener(SWT.Selection, new Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                useMarqueeInteractionListener();
            };
        });
        
        adaptiveModeMenu = new MenuItem(menu, SWT.CHECK);
        adaptiveModeMenu.setText("&Arrow Mode");
        adaptiveModeMenu.addListener(SWT.Selection, new Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                useAdaptiveInteractionListener();
            };
        });
    }

    class SomPaintListener implements PaintListener, Observer {
        public void paintControl(PaintEvent e) {
            if (som == null)
                return;

            clearCache(100);

            int r;
            int g;
            int b;

            int cellPosX;
            int cellPosY;
            int cellHeight;
            int cellWidth;
            float cellWidthF;
            float cellHeightF;

            for (int i = 0; i < Som.getWidth(); i++) {
                cellWidthF = ((float) canvas.getSize().x / Som.getWidth());
                cellPosX = Math.round(i * cellWidthF);

                for (int j = 0; j < Som.getHeight(); j++) {
                    cellHeightF = ((float) canvas.getSize().y / Som.getHeight());
                    cellPosY = Math.round(j * cellHeightF);

                    r = (int) (network[i][j].weights[0] * 255);
                    g = (int) (network[i][j].weights[1] * 255);
                    b = (int) (network[i][j].weights[2] * 255);

                    e.gc.setBackground(hitCache(new int[] { r, g, b }));
                    e.gc.setForeground(hitCache(new int[] { Math.min(r + 50, 255), Math.min(g + 50, 255), Math.min(b + 50, 255) }));

                    // fill background and border
                    e.gc.fillRectangle(cellPosX, cellPosY, (int) Math.ceil(cellWidthF), (int) Math.ceil(cellHeightF));

                    if (canvas.getSize().x > 100 && canvas.getSize().y > 60) {
                        e.gc.drawRectangle(cellPosX, cellPosY, (int) Math.ceil(cellWidthF), (int) Math.ceil(cellHeightF));
                    }
                }
            }
        }

        public void update(Observable o, Object arg) {
            redraw();
        }
    }

    class AdaptiveInteractionListener extends SomInteractionListener {
        private boolean down = false;

        private int[] start, end;

        private HashSet<SomNode> selectedNodes;

        public AdaptiveInteractionListener(SomTraverser traverser) {
            somTraverser = traverser;

            somTraverser.addChangeListener(new TraverserChangeListener() {
                public void traverserChanged() {
                    start = somTraverser.getPosition();
                    end = somTraverser.getNextPosition();

                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            canvas.redraw();
                        };
                    });
                }
            });
        }

        @Override
        public void mouseMove(MouseEvent e) {
            if (down) {
                end = coordsToSomIndexes(e.x, e.y);
                addSelectedNode(start[0], start[1]);
                somTraverser.setPosition(start[0], start[1]);
                // System.out.println("Start: " + start[0] + "," + start[1]);
                somTraverser.setEndPosition(end[0], end[1]);
                // System.out.println("End: " + end[0] + "," + end[1]);

                somTraverser.fireTraverserChanged(); // redraw whatever needs
                // to be redrawn;

                int[] next = somTraverser.getNext();

                addSelectedNode(next[0], next[1]);

                clearMenuItem.setEnabled(true);
                matcherEditor.setSelectedNodes(selectedNodes);
            }
        }

        private void addSelectedNode(int x, int y) {
            if (selectedNodes == null) {
                selectedNodes = new HashSet<SomNode>();
            }
            selectedNodes.add(network[x][y]);
        }

        @Override
        public void mouseDown(MouseEvent e) {
            if (e.button == 1) {
                down = true;
                start = coordsToSomIndexes(e.x, e.y);
                end = start;
                canvas.redraw();

                if (selectedNodes != null)
                    selectedNodes.clear();
            }
            
//            else if (e.button == 3) {
//                if (selectedNodes != null)
//                    selectedNodes.clear();
//
//                int[] start = somTraverser.getPosition();
//                int[] next = somTraverser.getNext();
//                this.start = start;
//                this.end = next;
//
//                addSelectedNode(start[0], start[1]);
//                addSelectedNode(next[0], next[1]);
//
//                matcherEditor.setSelectedNodes(selectedNodes);
//            }
        }

        @Override
        public void mouseUp(MouseEvent e) {
            if (e.button == 1) {
                down = false;
                playlist.setEnableStarvingProducer(true);
            }
        }

        public int[] getStart() {
            return start;
        }

        public int[] getEnd() {
            return end;
        }

        @Override
        public void clearSelectedNodes() {
            super.clearSelectedNodes();
            selectedNodes = null;
            start = null;
            end = null;
            playlist.setEnableStarvingProducer(false);
        }

    }

    class AdaptiveInteractionPaintListener implements PaintListener {
        private static final int POINT_RADIUS = 2;

        public void paintControl(PaintEvent e) {
            AdaptiveInteractionListener listener = (AdaptiveInteractionListener) interactionListener;

            if (listener.getStart() != null) {
                int[] start = listener.somIndexesToCoords(listener.getStart()[0], listener.getStart()[1]);
                int[] end = listener.somIndexesToCoords(listener.getEnd()[0], listener.getEnd()[1]);

                e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

                e.gc.setAntialias(SWT.ON);
                e.gc.drawLine(start[0], start[1], end[0], end[1]);
                e.gc.fillOval(end[0] - POINT_RADIUS, end[1] - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);
                e.gc.setAntialias(SWT.OFF);
            }
        }
    }

    class MarqueInteractionListener extends SomInteractionListener {
        private int firstCol;
        private int firstRow;
        private int lastCol;
        private int lastRow;

        private boolean down;

        @Override
        public void mouseMove(MouseEvent e) {
            // normalise values
            e.x = Math.min(e.x, canvas.getSize().x);
            e.x = Math.max(0, e.x);
            e.y = Math.min(e.y, canvas.getSize().y);
            e.y = Math.max(0, e.y);

            if (down) {
                SomNode endNode = coordsToSomNode(e.x, e.y);

                int startX = startNode.getXPos();
                int startY = startNode.getYPos();
                int endX = endNode.getXPos();
                int endY = endNode.getYPos();

                // normalise
                firstCol = Math.min(startX, endX);
                firstRow = Math.min(startY, endY);
                lastCol = Math.max(startX, endX);
                lastRow = Math.max(startY, endY);

                // create selected nodes array
                if (selectedNodes != null && !keyHeld) {
                    selectedNodes.clear();
                } else if (selectedNodes == null) {
                    selectedNodes = new HashSet<SomNode>();
                }

                // assume for now going from top left to bottom right
                for (int i = firstCol; i <= lastCol; i++) {
                    for (int j = firstRow; j <= lastRow; j++) {
                        selectedNodes.add(som.getNetwork()[i][j]);
                    }
                }

                // only redraw if coords has changed (snapped to grid)
                if (previousSomCoords != null && (previousSomCoords[0] != endX || previousSomCoords[1] != endY)) {
                    matcherEditor.setSelectedNodes(selectedNodes);
                }

                previousSomCoords = new int[] { endX, endY };

                clearMenuItem.setEnabled(true);
                canvas.redraw();
            }
        }

        @Override
        public void mouseDown(MouseEvent e) {
            if (e.button == 1) {
                // left click
                down = true;
                startNode = coordsToSomNode(e.x, e.y);
            }
        }

        public void clearSelectedNodes() {
            super.clearSelectedNodes();
            if (selectedNodes != null) {
                selectedNodes.clear();
            }
            clearMenuItem.setEnabled(false);

        }

        @Override
        public void mouseUp(MouseEvent e) {
            down = false;
        }

        public int getFirstCol() {
            return firstCol;
        }

        public int getFirstRow() {
            return firstRow;
        }

        public int getLastCol() {
            return lastCol;
        }

        public int getLastRow() {
            return lastRow;
        }
    }

    class MarqueeInteractionPaintListener implements PaintListener {
        public void paintControl(PaintEvent e) {
            int r;
            int g;
            int b;

            int cellPosX;
            int cellPosY;
            int cellHeight;
            int cellWidth;
            float cellWidthF;
            float cellHeightF;

            for (int i = 0; i < Som.getWidth(); i++) {
                cellWidthF = ((float) canvas.getSize().x / Som.getWidth());
                cellPosX = Math.round(i * cellWidthF);

                for (int j = 0; j < Som.getHeight(); j++) {
                    cellHeightF = ((float) canvas.getSize().y / Som.getHeight());
                    cellPosY = Math.round(j * cellHeightF);

                    r = (int) (network[i][j].weights[0] * 255);
                    g = (int) (network[i][j].weights[1] * 255);
                    b = (int) (network[i][j].weights[2] * 255);

                    Color colour;

                    boolean isSelectedNode;
                    // if selected, colour gray
                    if (interactionListener != null && interactionListener.getSelectedNodes() != null
                            && interactionListener.getSelectedNodes().contains(network[i][j])) {
                        // colour = Display.getCurrent().getSystemColor(
                        // SWT.COLOR_GRAY);
                        // e.gc.setBackground(colour);

                        colour = hitCache(new int[] { Math.min(r + 50, 255), Math.min(g + 50, 255), Math.min(b + 50, 255) });
                        e.gc.setForeground(colour);
                        e.gc.setBackground(colour);

                        // fill background
                        e.gc.fillRectangle(cellPosX, cellPosY, (int) Math.ceil(cellWidthF), (int) Math.ceil(cellHeightF));
                    }
                }
            }
        }
    }

    abstract class SomInteractionListener implements MouseListener, MouseMoveListener, KeyListener, Observer {

        protected int[] previousSomCoords;
        protected boolean keyHeld = false;
        protected SomNode startNode;
        protected boolean interactive = false;
        protected HashSet<SomNode> selectedNodes;

        public HashSet<SomNode> getSelectedNodes() {
            return selectedNodes;
        }

        public void keyPressed(KeyEvent e) {
            keyHeld = true;
        }

        public void keyReleased(KeyEvent e) {
            keyHeld = false;
        }

        public void mouseMove(MouseEvent e) {

        }

        protected SomNode coordsToSomNode(int x, int y) {
            int[] indexes = coordsToSomIndexes(x, y);
            return som.getNetwork()[indexes[0]][indexes[1]];
        }

        protected int[] coordsToSomIndexes(int x, int y) {
            float proportionX = (float) x / canvas.getSize().x;
            float proportionY = (float) y / canvas.getSize().y;
            int itemX = (int) Math.ceil(proportionX * Som.getWidth()) - 1;
            int itemY = (int) Math.ceil(proportionY * Som.getHeight()) - 1;

            // cutoff
            itemX = Math.max(itemX, 0);
            itemY = Math.max(itemY, 0);

            return new int[] { itemX, itemY };
        }

        protected int[] somIndexesToCoords(int x, int y) {
            x++;
            y++;
            float proportionX = (float) canvas.getSize().x / Som.getWidth();
            float proportionY = (float) canvas.getSize().y / Som.getHeight();
            float coordX = x * proportionX - (proportionX / 2);
            float coordY = y * proportionY - (proportionY / 2);

            // cutoff
            int x2 = (int) Math.max(0, coordX);
            x2 = (int) Math.min(x2, (float) canvas.getSize().x - proportionX / 2);
            int y2 = (int) Math.max(0, coordY);
            y2 = (int) Math.min(y2, (float) canvas.getSize().y - proportionY / 2);

            return new int[] { x2, y2 };
        }

        public void mouseDown(MouseEvent e) {
        }

        public void mouseUp(MouseEvent e) {
        }

        public void mouseDoubleClick(MouseEvent e) {

        }

        public void update(Observable o, Object arg) {
            if (arg != null && arg.equals("finished")) {
                // finished associating a som node to all tracks
                interactive = true;
            }
        }

        public void clearSelectedNodes() {
            matcherEditor.setSelectedNodes(null);
        }
    }

    public SomTraverser getSomTraverser() {
        return somTraverser;
    }
}
