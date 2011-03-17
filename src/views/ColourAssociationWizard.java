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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import listeners.ColourPickerListener;
import listeners.PlayerLoadingListener;
import models.PlayerCallBackData;
import models.Track;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import controllers.CustomizablePlayer;
import controllers.PlaybackError;
import controllers.Player;
import controllers.ProfileWriter;
import data.PropertiesLoader;

public class ColourAssociationWizard extends Wizard {
    private Player player;
    /**
     * Mutex for when player is being used
     */
    private Object playerMutex = new Object();
    private final Player mainPlayer;
    private TrackWizardPage lastPage;
    private static final String baseUrl = "http://mublog.co.uk/finalyear/music";
    private static final Track[] tracks;
    static {
        tracks = new Track[] {
                createTrack(baseUrl + "/Dethbed.mp3", "Alkaline Trio",
                        "Dethbed"),
                createTrack(baseUrl + "/Feels%20Like%20It%20Should.mp3",
                        "Jamiroquai", "Feels Like It Should"),
                createTrack(baseUrl + "/Killing%20Time.mp3", "Hed (P.E)",
                        "Killing Time"),
                createTrack(baseUrl + "/This%20Love.mp3", "Maroon 5",
                        "This Love"),
                createTrack(baseUrl + "/Unintended.mp3", "Muse", "Unintended"), };
    }

    private static Track createTrack(String url, String artist, String title) {
        Track track = new Track();
        track.setPath(url);
        track.setArtist(artist);
        track.setTitle(title);
        return track;

    }

    private HashMap<Track, Color> model = new HashMap<Track, Color>();
    private final PropertiesLoader properties;

    public ColourAssociationWizard(Player mainPlayer,
            PropertiesLoader properties) {
        super();

        System.out.println(mainPlayer);

        this.mainPlayer = mainPlayer;
        this.properties = properties;
        initPlayer();
        initWizard();
    }

    private void initWizard() {
        setWindowTitle("Colour Association Wizard");

        addPage(new IntroPage());
        for (int i = 0; i < tracks.length; i++) {
            addPage(lastPage = new TrackWizardPage(tracks[i], Integer
                    .toString(i + 1), "Track " + (i + 1) + " of "
                    + tracks.length));
        }
        addPage(new FinishPage());

    }

    @Override
    public boolean canFinish() {
        return lastPage != null && lastPage.isPageComplete();
    }

    private void initPlayer() {
        this.player = new CustomizablePlayer(properties);

        // attach listeners
        player.addListener(new WizardPlayerListener());
    }

    public void cleanUp() {
        if (player != null) {
            player.cleanUp();
        }
    }

    @Override
    public boolean performCancel() {
        boolean cancelled = super.performCancel();

        cleanUp();
        return cancelled;
    }

    @Override
    public boolean performFinish() {

        ProfileWriter writer = new ProfileWriter(Integer.parseInt(properties
                .getProperties().getProperty("unique_id")));

        writer.process(tracks);
        writer.submit();
        
        try {
            writeProperties();
        } catch (IOException ioe) {
            System.err
                    .println("There was an IOException when writing the properties after completing colour association wizard");
        }

        cleanUp();

        return true;
    }

    /**
     * Write properties file so that the program does not start the wizard
     * automatically next time
     */
    private void writeProperties() throws IOException {
        properties.getProperties().setProperty("show_association_wizard",
                "false");
        properties.save();
    }

    // pages
    private class IntroPage extends WizardPage {
        private Composite control;

        public IntroPage() {
            super("Wizard 1");

            setTitle("Introduction");
            setDescription("Welcome to the Colour Player colour association wizard");
        }

        public void createControl(Composite parent) {
            Composite c = new Composite(parent, SWT.NONE);
            c.setLayout(new GridLayout());
            Label introLabel = new Label(c, SWT.WRAP);
            introLabel
                    .setText("The wizard will step you through a series of songs. By telling Colour Player the colours that you "
                            + "think of when you listen to the songs, you will be greatly aiding the research we are performing which will eventually "
                            + "enable us to form a profile which will be used to download colour associations automatically from people that "
                            + "associate colours in the same way as you.\n\n"
                            + "Once you complete this wizard it will not show again when you start the program, although you can fill it in by clicking on \"Colour Association Wizard\" in the tools menu.\n\n"
                            + "Please not that the tracks are streamed from the Internet, so a working Internet connection is required to complete the wizard.\n\n"
                            + "Click next to continue.");

            GridData data = new GridData();
            data.grabExcessHorizontalSpace = true;
            introLabel.setLayoutData(data);

            setControl(c);
        }
    }

    private class FinishPage extends WizardPage {
        private Composite control;

        public FinishPage() {
            super("End");

            setTitle("Finished!");
            setDescription("Thank you for helping out");
        }

        public void createControl(Composite parent) {
            Composite c = new Composite(parent, SWT.NONE);
            c.setLayout(new GridLayout());
            Label introLabel = new Label(c, SWT.WRAP);
            introLabel
                    .setText("Thank you for your time.\n\n"
                            + "To get started with the program, try adding some tracks by going to \"File > Add Music to Library\"");

            GridData data = new GridData();
            data.grabExcessHorizontalSpace = true;
            introLabel.setLayoutData(data);

            setControl(c);
        }
    }

    private class WizardPlayerListener implements PlayerListener {
        public void callback(PlayerCallBackData data) {
        }

        public void message(String message, Player player) {

        }
    }

    private class TrackWizardPage extends WizardPage {

        private Button playAgainButton;
        private ColourPickerCanvasWrapper picker;
        private final Track track;

        public TrackWizardPage(Track track, String pageName, String title) {
            super(pageName, title, null);
            this.track = track;

            setDescription("Contribute to the research that makes this program possible");
            setPageComplete(false);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(1, true));

            Label descriptionLabel = new Label(composite, SWT.WRAP);
            descriptionLabel
                    .setText("Click play to start listening to the track. There may be a small delay as the track is downloaded.\n\nAs you listen to the track, please associate a colour with it using the colour picker below. After you have associated a colour, please continue by clicking next.");

            picker = new ColourPickerCanvasWrapper(composite);

            picker.addListener(new ColourPickerListener() {
                public void pickedColour(List<Track> tracks, int r, int g, int b) {
                    track.setColour(r, g, b);
                    setPageComplete(true);
                };
                public void clearedColour(List<Track> referenceTracks) {
                }
            });

            // player.addListener(picker);

            playAgainButton = new Button(composite, SWT.NONE);
            playAgainButton.setText("Play");

            // layout
            GridData data;

            data = new GridData();
            data.grabExcessHorizontalSpace = true;
            descriptionLabel.setLayoutData(data);

            data = new GridData();
            data.horizontalAlignment = SWT.CENTER;
            data.widthHint = 250;
            data.heightHint = 100;
            data.verticalIndent = 5;
            data.grabExcessHorizontalSpace = true;
            picker.getCanvas().setLayoutData(data);

            data = new GridData();
            data.horizontalAlignment = SWT.CENTER;
            playAgainButton.setLayoutData(data);

            initListeners();

            setControl(composite);
        }

        private void initListeners() {
            playAgainButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    loadTrack();
                    playAgainButton.setEnabled(false);
                }
            });

            player.addListener(new PlayerListener() {
                public void message(String message, Player player) {
                    if (message.equals("stopped")) {
                        Display.getDefault().syncExec(new Runnable() {
                            public void run() {
                                if (playAgainButton != null
                                        && !playAgainButton.isDisposed()) {
                                    playAgainButton.setEnabled(true);
                                    playAgainButton.setText("Play Again");
                                    playAgainButton.getParent().layout(
                                            new Control[] { playAgainButton });
                                }
                            };
                        });
                    }
                }

                public void callback(PlayerCallBackData data) {
                }
            });
        }

        private void loadTrack() {
            try {
                player.play(track.getPath(),
                        new PlayerLoadingListener[] { new LoadedChecker() });
            } catch (MalformedURLException urle) {
                urle.printStackTrace();
            } catch (PlaybackError pbe) {
                pbe.printStackTrace();
            } catch (FileNotFoundException fnf) {
                fnf.printStackTrace();
            }
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);

            if (visible) {
                player.setInAdvancingMode(false);
                player.stop();
                playAgainButton.setText("Play");
            }
        }

        private class LoadedChecker implements PlayerLoadingListener {
            public void loaded(String url) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        if (!mainPlayer.isPaused()) {
                            mainPlayer.pause();
                        }
                        picker.setEnabled(true);
                    }
                });

            }

            public void loadError() {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        MessageBox msg = new MessageBox(getShell(),
                                SWT.ICON_ERROR | SWT.OK);
                        msg
                                .setMessage("Due to a network error the track could not be downloaded.\nPlease check your internet connection and try again");
                        msg.open();
                        playAgainButton.setEnabled(true);
                    }
                });
            }
        }
    }

    public static void main(String[] args) {
        Shell shell = new Shell(new Display());
        ColourAssociationWizard wizard = new ColourAssociationWizard(
                new Player(), new PropertiesLoader());
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.setPageSize(400, 400);
        dialog.setBlockOnOpen(true);
        dialog.create();
        dialog.open();
    }
}
