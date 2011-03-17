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
/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                     publicbobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package glazedlists.matchereditors;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.List;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

/**
 * A {@link MatcherEditor} that matches elements that contain the filter text
 * located within a {@link List} field. This {@link TextWidgetMatcherEditor} is
 * directly coupled with a {@link List} and fires {@link MatcherEditor} events
 * in response to {@link ModifyEvent}s received from the {@link List}. This
 * matcher is fully concrete for use in SWT applications.
 * 
 * <p>
 * If this {@link MatcherEditor} must be garbage collected before the underlying
 * Text, the listener can be unregistered by calling {@link #dispose()}.
 * 
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ListWidgetMatcherEditor extends UpdatableTextMatcherEditor {

    /** the filter edit list */
    private List list;

    /** the listener that triggers refiltering when events occur */
    private FilterChangeListener filterChangeListener = new FilterChangeListener();

    /**
     * Creates a TextWidgetMatcherEditor bound to the provided {@link List} with
     * the given <code>textFilterator</code> where filtering can be specified
     * as "live" or to be based on another event such as the user pressing Enter
     * or a button being clicked.
     * 
     * @param text
     *            the {@link List} widget that drives the text-filtering
     * @param textFilterator
     *            an object capable of producing Strings from the objects being
     *            filtered. If <code>textFilterator</code> is
     *            <code>null</code> then all filtered objects are expected to
     *            implement {@link ca.odell.glazedlists.TextFilterable}.
     * 
     * @see GlazedLists#textFilterator(String[])
     */
    public ListWidgetMatcherEditor(List list, TextFilterator textFilterator) {
        super(textFilterator);
        this.list = list;
        registerListeners();
        refilter();
    }

    /**
     * Listen live
     */
    private void registerListeners() {
        this.list.addSelectionListener(filterChangeListener);

    }

    /**
     * Stop listening.
     */
    private void deregisterListeners() {
        this.list.removeSelectionListener(filterChangeListener);
    }

    /**
     * Gets a SelectionListener that refilters the list when it is fired. This
     * listener can be used to filter when the user presses a 'Search' button.
     */
    public SelectionListener getFilterSelectionListener() {
        return filterChangeListener;
    }

    /**
     * A cleanup method which stops this MatcherEditor from listening to changes
     * on the {@link List} component, thus freeing the MatcherEditor to be
     * garbage collected. Garbage collection could be blocked if you have
     * registered the SelectionListener provided by
     * {@link #getFilterSelectionListener()} and not removed that listener (of
     * disposed of the widget it was registered to).
     */
    public void dispose() {
        deregisterListeners();
    }

    /**
     * Refilter based on the new contents of the Text..
     */
    private void refilter() {
        if (list.getSelectionCount() == 0) {
            setFilterText(new String[0]);
        } else {
            setFilterText(list.getSelection());
        }
    }

    /**
     * Implements the SelectionListener interface for text filter updates. When
     * the user clicks a button (supplied by external code), this
     * SelectionListener can be used to update the filter in response.
     */
    private class FilterChangeListener implements SelectionListener {
        public void widgetSelected(SelectionEvent e) {
            refilter();
        }

        public void widgetDefaultSelected(SelectionEvent e) {
            refilter();
        }
    }
    
    @Override
    protected void parentMatcherUpdated() {
        list.deselectAll();
        refilter();
    }
}