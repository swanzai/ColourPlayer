package views;

import models.PlaylistDao;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class ColourPickerContextMenu {
    private final PlaylistDao nowPlaying;
    private final Composite parent;
    private Menu menu;
    private final ColourPickerCanvasWrapper wrapper;

    public ColourPickerContextMenu(ColourPickerCanvasWrapper wrapper,
            PlaylistDao nowPlaying) {
        this.wrapper = wrapper;
        this.parent = wrapper.getCanvas();
        this.nowPlaying = nowPlaying;

        initMenu();
    }

    private void initMenu() {
        menu = new Menu(parent.getShell(), SWT.POP_UP);
        parent.setMenu(menu);

        MenuItem jumpToTrack;

        jumpToTrack = new MenuItem(menu, SWT.PUSH);
        jumpToTrack.setText("&Jump to Current Track");
        jumpToTrack.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (nowPlaying.getTrack() != null)
                    wrapper.setReferenceTrack(nowPlaying.getTrack());
            }
        });

        MenuItem removeAssociation;

        removeAssociation = new MenuItem(menu, SWT.PUSH);
        removeAssociation.setText("&Remove Association");
        removeAssociation.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                wrapper.clearCurrent();
            }
        });
    }
}
