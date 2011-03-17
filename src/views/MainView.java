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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import som.Som;

import com.novocode.naf.swt.custom.LiveSashForm;

import config.Constants;
import controllers.TrackProgressBar;

public class MainView extends RegistryApplicationWindow {
    public static Shell shell;
    private AlbumArtCanvas albumArt;
    private SomCanvasWrapper somCanvas;
    private ToolBarManager toolBarManager;
    private Composite leftSection;
    private TableViewer leftMainTableViewer;
    private StyledSongInfo styledSongInfo;;
    private List<Image> imageResources = new ArrayList<Image>();
    private ColourPickerCanvasWrapper colourPickerCanvasWrapper;
    private GlazedMainTable glazedMainTable;
    private GlazedArtistList glazedArtistList;
    private GlazedAlbumList glazedAlbumList;
    private Text searchField;
    private SubSectionList subSectList;
    private Button searchOptionArrow;
    private LiveSashForm mainSash;
    private LiveSashForm topSashComposite;

    public MainView() {
        super(null);
        
        createIcon();
        addMenuBar();
    }

    public void cleanUp() {
        albumArt.cleanUp();
        somCanvas.cleanUp();
        subSectList.cleanUp();
        glazedMainTable.cleanUp();

        for (Image i : imageResources) {
            i.dispose();
        }
    }

    public AlbumArtCanvas getAlbumArt() {
        return albumArt;
    }

    public GlazedAlbumList getGlazedAlbumList() {
        return glazedAlbumList;
    }

    public GlazedArtistList getGlazedArtistList() {
        return glazedArtistList;
    }

    public GlazedMainTable getGlazedMainTable() {
        return glazedMainTable;
    }

    public TableViewer getLeftMainTableViewer() {
        return leftMainTableViewer;
    }

    public LiveSashForm getMainSash() {
        return mainSash;
    }

    public ColourPickerCanvasWrapper getPickerCanvasWrapper() {
        return colourPickerCanvasWrapper;
    }

    public Text getSearchField() {
        return searchField;
    }

    public SomCanvasWrapper getSomCanvas() {
        return somCanvas;
    }

    public StyledSongInfo getStyledSongInfo() {
        return styledSongInfo;
    }

    public SubSectionList getSubSectList() {
        return subSectList;
    }

    public ToolBarManager getToolBarManager() {
        return toolBarManager;
    }

    public LiveSashForm getTopSashComposite() {
        return topSashComposite;
    }

    public void setLeftMainTableViewer(TableViewer leftMainTableViewer) {
        this.leftMainTableViewer = leftMainTableViewer;
    }

    public void setSom(Som som) {
        somCanvas.setSom(som);
    }

    private AlbumArtCanvas createAlbumArtCanvas(Composite parent) {
        FormData albumArtLayout = new FormData();

        albumArtLayout.bottom = new FormAttachment(100, -5);
        albumArtLayout.left = new FormAttachment(0, 5);
        albumArtLayout.right = new FormAttachment(100, 0);
        albumArtLayout.height = 120;
        final AlbumArtCanvas albumArt = new AlbumArtCanvas(parent, albumArtLayout, SWT.DOUBLE_BUFFERED | SWT.BORDER);
        albumArt.setLayoutData(albumArtLayout);

        // resize listener for albumart
        leftSection.addControlListener(new ControlListener() {
            public void controlMoved(ControlEvent e) {
            };

            public void controlResized(org.eclipse.swt.events.ControlEvent e) {
                albumArt.resizeCanvas();
            }
        });

        return albumArt;
    }

    private Composite createButtonPanel(Composite composite) {
        Composite buttonComposite = new Composite(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        gridLayout.horizontalSpacing = 2;
        gridLayout.verticalSpacing = 2;

        buttonComposite.setLayout(gridLayout);
        Button previousButton = new Button(buttonComposite, SWT.NONE);
        Button playButton = new Button(buttonComposite, SWT.NONE);
        Button stopButton = new Button(buttonComposite, SWT.NONE);
        Button nextButton = new Button(buttonComposite, SWT.NONE);

        registerWidget("previousButton", previousButton);
        registerWidget("playButton", playButton);
        registerWidget("stopButton", stopButton);
        registerWidget("nextButton", nextButton);

        FormData ld = new FormData();
        ld.top = new FormAttachment(0, 0);
        ld.left = new FormAttachment(0, 0);
        buttonComposite.setLayoutData(ld);

        return buttonComposite;
    }

    private void createIcon() {
        final Image icon = new Image(Display.getDefault(), "images/shell_icon.png");
        imageResources.add(icon);
        setDefaultImage(icon);
    }

    private Composite createLeftSection(Composite leftSash) {
        Composite leftSection = new Composite(leftSash, SWT.NONE);
        FormLayout layout = new FormLayout();
        leftSection.setLayout(layout);

        return leftSection;
    }

    private Table createMainTable(Composite shell) {
        glazedMainTable = new GlazedMainTable(shell);

        return glazedMainTable.getTable();
    }

    private void initLeftSection(Composite leftSection) {
        subSectList = new SubSectionList(leftSection);

        colourPickerCanvasWrapper = new ColourPickerCanvasWrapper(leftSection);
        Canvas picker = colourPickerCanvasWrapper.getCanvas();

        FormData formData;

        // picker
        formData = new FormData();
        formData.bottom = new FormAttachment(albumArt, -5, SWT.TOP);
        formData.right = new FormAttachment(100, 0);
        formData.height = 100;
        formData.left = new FormAttachment(0, 5);
        picker.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(0, 5);
        formData.right = new FormAttachment(100, 0);
        formData.bottom = new FormAttachment(picker, -5, SWT.TOP);
        subSectList.getTable().setLayoutData(formData);
    }

    @Override
    protected Control createContents(Composite shell) {
        getShell().setText(Constants.PROGRAM_NAME + " " + Constants.VERSION);
        getShell().setSize(700, 600);
        MainView.shell = getShell();

        Composite composite = new Composite(shell, SWT.NONE);
        FormLayout layout = new FormLayout();
        composite.setLayout(layout);

        /* Create widgets */
        styledSongInfo = new StyledSongInfo(composite);
        TrackProgressBar trackProgressBar = new TrackProgressBar(composite, SWT.HORIZONTAL);
        trackProgressBar.setEnabled(false);
        VolumeBar volumeBar = new VolumeBar(composite, SWT.HORIZONTAL);
        StyledText styledSongInfoText = styledSongInfo.getControl();
        searchField = new Text(composite, SWT.BORDER);
        searchOptionArrow = new Button(composite, SWT.ARROW | SWT.DOWN);

        Label searchLabel = new Label(composite, SWT.NONE);
        searchLabel.setText("&Search: ");

        final Composite leftSash = new Composite(composite, SWT.NONE);
        leftSash.setLayout(new FormLayout());

        // left sash
        leftSection = createLeftSection(leftSash); // SASH LEFT

        // create sash separator
        Sash separator = new Sash(leftSash, SWT.VERTICAL);
        separator.setSize(5, leftSash.getSize().y);

        albumArt = createAlbumArtCanvas(leftSection);
        initLeftSection(leftSection);

        // main sash
        mainSash = new LiveSashForm(leftSash, SWT.NONE | SWT.VERTICAL);
        somCanvas = new SomCanvasWrapper(mainSash);

        topSashComposite = new LiveSashForm(mainSash, SWT.NONE);

        // artist list
        glazedArtistList = new GlazedArtistList(topSashComposite);
        glazedAlbumList = new GlazedAlbumList(topSashComposite);

        topSashComposite.setWeights(new int[] { 50, 50 });

        Table mainTable = createMainTable(mainSash);
        mainSash.setWeights(new int[] { 20, 20, 60 });

        // play button/stop etc
        Composite buttonComposite = createButtonPanel(composite);

        registerWidget("shell", shell);
        registerWidget("mainTable", mainTable);
        registerWidget("albumArt", albumArt);
        registerWidget("searchField", searchField);
        registerWidget("mainSash", mainSash);
        registerWidget("trackProgressBar", trackProgressBar);
        registerWidget("volumeBar", volumeBar);
        registerWidget("searchOptionArrow", searchOptionArrow);

        FormData ld;

        ld = new FormData();
        ld.top = new FormAttachment(buttonComposite, 0, SWT.TOP);
        ld.left = new FormAttachment(buttonComposite, 5, SWT.RIGHT);
        ld.right = new FormAttachment(searchLabel, -10, SWT.LEFT);
        ld.bottom = new FormAttachment(buttonComposite, 0, SWT.BOTTOM);
        styledSongInfoText.setLayoutData(ld);

        ld = new FormData();
        ld.top = new FormAttachment(styledSongInfoText, 0, SWT.BOTTOM);
        ld.left = new FormAttachment(styledSongInfoText, 0, SWT.LEFT);
        ld.right = new FormAttachment(styledSongInfoText, 0, SWT.RIGHT);
        ld.height = 10;
        trackProgressBar.setLayoutData(ld);

        ld = new FormData();
        ld.top = new FormAttachment(buttonComposite, 0, SWT.BOTTOM);
        ld.left = new FormAttachment(buttonComposite, 5, SWT.LEFT);
        ld.right = new FormAttachment(buttonComposite, -5, SWT.RIGHT);
        ld.height = 10;
        volumeBar.setLayoutData(ld);

        ld = new FormData();
        ld.bottom = new FormAttachment(volumeBar, 0, SWT.BOTTOM);
        ld.right = new FormAttachment(100, -5);
        searchOptionArrow.setLayoutData(ld);

        ld = new FormData();
        ld.bottom = new FormAttachment(volumeBar, 0, SWT.BOTTOM);
        ld.right = new FormAttachment(searchOptionArrow, -1, SWT.LEFT);
        ld.width = 100;
        searchField.setLayoutData(ld);

        ld = new FormData();
        ld.top = new FormAttachment(searchField, 0, SWT.CENTER);
        ld.right = new FormAttachment(searchField, -5, SWT.LEFT);
        searchLabel.setLayoutData(ld);

        ld = new FormData();
        ld.top = new FormAttachment(volumeBar, 5, SWT.BOTTOM);
        ld.left = new FormAttachment(0, 0);
        ld.right = new FormAttachment(100, 0);
        ld.bottom = new FormAttachment(100, 0);
        leftSash.setLayoutData(ld);

        // layout the main vertical split composite
        final FormData separatorFormData = new FormData();
        separatorFormData.top = new FormAttachment(0, 0);
        separatorFormData.left = new FormAttachment(0, 120);
        separatorFormData.bottom = new FormAttachment(100, 0);
        separatorFormData.width = 5;
        separator.setLayoutData(separatorFormData);

        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(separator, 0, SWT.LEFT);
        data.bottom = new FormAttachment(100, 0);
        leftSection.setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = new FormAttachment(separator, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(100, 0);
        data.right = new FormAttachment(100, 0);
        mainSash.setLayoutData(data);

        separator.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                separatorFormData.left = new FormAttachment(0, e.x);
                leftSash.layout();
            }

        });

        return composite;
    }

    protected MenuManager createMenuManager() {
        MenuManager menuBar = new MenuManager("");
        MenuManager fileMenu = new MenuManager("&File");
        MenuManager viewMenu = new MenuManager("&View");
        MenuManager toolsMenu = new MenuManager("&Tools");
        MenuManager playbackMenu = new MenuManager("&Playback");
        MenuManager helpMenu = new MenuManager("&Help");

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(playbackMenu);
        menuBar.add(helpMenu);

        registerMenu("root", menuBar);
        registerMenu("file", fileMenu);
        registerMenu("view", viewMenu);
        registerMenu("tools", toolsMenu);

        registerMenu("playback", playbackMenu);
        registerMenu("help", helpMenu);

        return menuBar;
    }
}
