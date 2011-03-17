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

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

/**
 * A text matcher editor that when updated can update a parent matcher editor
 * too
 * 
 * @author Michael Voong
 * 
 */
public abstract class UpdatableTextMatcherEditor extends TextMatcherEditor {
    public UpdatableTextMatcherEditor(TextFilterator textFilterator) {
        super(textFilterator);
    }

    public void addFilterClearingListener(TextMatcherEditor parentMatcherEditor) {
        parentMatcherEditor.addMatcherEditorListener(new Listener() {
            public void changedMatcher(
                    ca.odell.glazedlists.matchers.MatcherEditor.Event event) {
                parentMatcherUpdated();
            };
        });
    }

    protected abstract void parentMatcherUpdated();
}
