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

package glazedlists.matchers;

import java.util.HashSet;

import models.Track;
import som.SomNode;
import ca.odell.glazedlists.matchers.Matcher;

public class ColourMatcher implements Matcher<Track> {
    public static final int DEFAULT_TOLERANCE = 60;
    private HashSet<SomNode> selectedNodes;
    private int tolerance = DEFAULT_TOLERANCE;

    public ColourMatcher(int tolerance) {
        this.tolerance = tolerance;
    }

    public ColourMatcher() {
    }

    public boolean matches(Track item) {
        if (selectedNodes == null) {
            return true;
        } else if (item.getColour() != null) {
            int[] c = item.getColour();
            int v;

            for (SomNode node : selectedNodes) {
                v = (int) (node.getWeights()[0] * 255);
                if (c[0] > v + tolerance || c[0] < v - tolerance)
                    continue;
                v = (int) (node.getWeights()[1] * 255);
                if (c[1] > v + tolerance || c[1] < v - tolerance)
                    continue;
                v = (int) (node.getWeights()[2] * 255);
                if (c[2] > v + tolerance || c[2] < v - tolerance)
                    continue;
                return true;
            }
        }

        return false;
    }

    public synchronized void setSelectedNodes(HashSet<SomNode> selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

}
