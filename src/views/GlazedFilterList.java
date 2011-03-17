package views;

import glazedlists.matchereditors.ListWidgetMatcherEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public abstract class GlazedFilterList {
    protected List list;
    protected ListWidgetMatcherEditor matcherEditor;
    protected List clearList;

    public GlazedFilterList(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        
        this.clearList = new List(composite, SWT.NONE);
        this.list = new List(composite, SWT.V_SCROLL | SWT.BORDER);
        
        this.clearList.add(getClearListLabel());
        this.clearList.pack();
        this.clearList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {};
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                clearListClicked();
            };
        });
        
        composite.setLayout(new FormLayout());
        FormData fd = new FormData();
        fd.left = new FormAttachment(0,0);
        fd.top = new FormAttachment(0,0);
        fd.right = new FormAttachment(100, 0);
        clearList.setLayoutData(fd);
        
        fd = new FormData();
        fd.left = new FormAttachment(0,0);
        fd.right = new FormAttachment(100,0);
        fd.bottom = new FormAttachment(100,0);
        fd.top = new FormAttachment(clearList, 0, SWT.BOTTOM);
        list.setLayoutData(fd);

        Menu menu = new Menu(list.getShell());
        list.setMenu(menu);
        MenuItem clear = new MenuItem(menu, SWT.PUSH);
        clear.setText("Clear Filter");
        clear.addListener(SWT.Selection, new Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                matcherEditor.setFilterText(new String[0]);
                list.deselectAll();
            };
        });
    }
    
    protected void clearListClicked() {
        matcherEditor.setFilterText(new String[0]);
        clearList.deselectAll();
        list.deselectAll();
    }
    
    protected abstract String getClearListLabel();
}
