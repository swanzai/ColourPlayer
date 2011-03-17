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

package glazedlists.matchereditors;

import glazedlists.matchers.ColourMatcher;

import java.util.HashSet;

import som.SomNode;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;

public class ColourMatcherEditor extends AbstractMatcherEditor {
    private HashSet<SomNode> selectedNodes = null;
    private int tolerance = ColourMatcher.DEFAULT_TOLERANCE;

    public synchronized void setSelectedNodes(HashSet<SomNode> selectedNodes) {
        if (selectedNodes == null) {
            fireMatchAll();
        } else {
            this.selectedNodes = selectedNodes;
            ColourMatcher newMatcher = new ColourMatcher(tolerance);
            newMatcher.setSelectedNodes(selectedNodes);
            fireChanged(newMatcher);
        }
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
        if (selectedNodes == null) {
            fireMatchAll();
        } else {
            ColourMatcher newMatcher = new ColourMatcher(tolerance);
            newMatcher.setSelectedNodes(selectedNodes);
            fireChanged(newMatcher);
        }
    }
}
