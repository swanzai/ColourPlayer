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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import listeners.TraverserChangeListener;

import som.Som;
import som.SomNode;

/**
 * This class traverses a SOM depending on an angle and starting location. Each
 * time we call getNext(), the position is updated. The nodes that are covered
 * by the vector are stored in a HashSet
 * 
 * @author Michael Voong
 * 
 */
public class SomTraverser {
    public int velX;
    public int velY;
    public int x;
    public int y;

    private List<TraverserChangeListener> changeListeners = new ArrayList<TraverserChangeListener>();

    public int[] getNext() {
        int[] pos = getNextPosition();
        x = pos[0];
        y = pos[1];
        
        return pos;
    }

    public void setPosition(int col, int row) {
        if (col >= Som.getWidth() || row >= Som.getHeight()) {
            throw new IllegalArgumentException("Invalid SOM position specified");
        }

        this.x = col;
        this.y = row;
    }

    public void setEndPosition(int col, int row) {
        velX = col - x;
        velY = row - y;        
    }

    public int[] getNextPosition() {
        int x2 = (int) Math.round(x + velX);
        int y2 = (int) Math.round(y + velY);

        HashSet<SomNode> nodes = new HashSet<SomNode>();

        if (x2 < 0 || x2 > Som.getWidth() - 1) {
            // reverse x velocity

            x2 = (int) Math.round(x2 - velX);
            velX = -velX;
        }
        if (y2 < 0 || y2 > Som.getHeight() - 1) {
            y2 = (int) Math.round(y2 - velY);
            velY = -velY;
        }

        return new int[] { x2, y2 };
    }

    public int[] getPosition() {
        return new int[] { x, y };
    }

    public void fireTraverserChanged() {
        for (TraverserChangeListener l : changeListeners) {
            l.traverserChanged();
        }
    }

    public void addChangeListener(TraverserChangeListener listener) {
        changeListeners.add(listener);
    }
}