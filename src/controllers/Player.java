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

import static java.lang.System.exit;
import static java.lang.System.out;
import static org.jouvieje.FmodEx.Defines.FMOD_INITFLAGS.FMOD_INIT_NORMAL;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_2D;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.FMOD_HARDWARE;
import static org.jouvieje.FmodEx.Defines.FMOD_TIMEUNIT.FMOD_TIMEUNIT_MS;
import static org.jouvieje.FmodEx.Defines.VERSIONS.FMOD_VERSION;
import static org.jouvieje.FmodEx.Defines.VERSIONS.NATIVEFMODEX_JAR_VERSION;
import static org.jouvieje.FmodEx.Defines.VERSIONS.NATIVEFMODEX_LIBRARY_VERSION;
import static org.jouvieje.FmodEx.Enumerations.FMOD_CHANNELINDEX.FMOD_CHANNEL_FREE;
import static org.jouvieje.FmodEx.Enumerations.FMOD_RESULT.FMOD_ERR_INVALID_HANDLE;
import static org.jouvieje.FmodEx.Enumerations.FMOD_RESULT.FMOD_OK;
import helpers.StoppableThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import listeners.PlayerLoadingListener;
import models.MetaData;
import models.PlayerCallBackData;
import models.Track;

import org.eclipse.swt.widgets.Display;
import org.jouvieje.FmodEx.Channel;
import org.jouvieje.FmodEx.FmodEx;
import org.jouvieje.FmodEx.Init;
import org.jouvieje.FmodEx.Sound;
import org.jouvieje.FmodEx.System;
import org.jouvieje.FmodEx.Defines.INIT_MODES;
import org.jouvieje.FmodEx.Enumerations.FMOD_RESULT;
import org.jouvieje.FmodEx.Exceptions.InitException;
import org.jouvieje.FmodEx.Misc.BufferUtils;

import testing.Benchmark;
import views.PlayerListener;

public class Player {
    // fmod
    private ByteBuffer buffer;
    private System system;
    private Sound sound;
    private Channel channel;
    private FMOD_RESULT result;

    private ArrayList<PlayerListener> listeners;
    private PlayerCallBack playerCallBack;
    private MetaData metaData;
    private static final int CALLBACK_REFRESH_INTERVAL = 500;

    /**
     * Thread for loading sounds
     */
    StreamLoadingThread loadingThread;

    /**
     * Player is in advancing mode until manual stop/pause
     */
    private boolean isInAdvancingMode = true;
    private int playbackMode = FMOD_HARDWARE;
    private PlayerLoadingListener streamLoadingListener;
    private boolean listenersEnabled = true;
    public Object playerMutex = new Object();
    private float gain = 1f;

    public Player() {
        init();
    }

    /**
     * @return
     */
    public System getSystem() {
        return system;
    }

    private void init() {
        try {
            Init.loadLibraries(INIT_MODES.INIT_FMOD_EX);
        } catch (InitException e) {
            out.printf("NativeFmodEx error! %s\n", e.getMessage());
            exit(1);
        }
        /*
         * Checking NativeFmodEx version
         */
        if (NATIVEFMODEX_LIBRARY_VERSION != NATIVEFMODEX_JAR_VERSION) {
            out
                    .printf(
                            "Error!  NativeFmodEx library version (%08x) is different to jar version (%08x)\n",
                            NATIVEFMODEX_LIBRARY_VERSION,
                            NATIVEFMODEX_JAR_VERSION);
            exit(0);
        }

        // create new FMOD system object
        system = new System();

        sound = new Sound();
        channel = new Channel();

        /*
         * Buffer used to store all data received from FMOD.
         */
        buffer = BufferUtils.newByteBuffer(BufferUtils.SIZEOF_INT);

        /*
         * Create a System object and initialize.
         */
        result = FmodEx.System_Create(system);
        ERRCHECK(result);

        result = system.getVersion(buffer.asIntBuffer());
        ERRCHECK(result);
        int version = buffer.getInt(0);

        if (version < FMOD_VERSION) {
            out
                    .printf(
                            "Error!  You are using an old version of FMOD %08x.  This program requires %08x\n",
                            version, FMOD_VERSION);
            exit(0);
        }

        result = system.init(1, FMOD_INIT_NORMAL, null);
        ERRCHECK(result);

        // //set buffer
        // system.setStreamBufferSize(800, FMOD_TIMEUNIT_MS);
        // system.update();

    }

    private static boolean ERRCHECK(FMOD_RESULT result) {
        if (result != FMOD_RESULT.FMOD_OK) {
            out.printf("FMOD error! (%d) %s\n", result.asInt(), FmodEx
                    .FMOD_ErrorString(result));
            return false;
        }
        return true;
    }

    public void setPlaybackMode(int playbackMode) {
        this.playbackMode = playbackMode;
    }

    public synchronized void play(String url) throws MalformedURLException,
            FileNotFoundException, PlaybackError {
        play(url, null);
    }

    public synchronized void play(String url, PlayerLoadingListener[] listeners)
            throws MalformedURLException, FileNotFoundException, PlaybackError {

        // listener that plays tracks when loaded
        if (streamLoadingListener == null) {
            streamLoadingListener = new StreamLoadingListener();
        }

        String playUrl;
        try {
            if (new File(url).isFile()) {
                playUrl = url;
            } else {
                playUrl = new URL(url).toExternalForm(); // see if this
                // throws an
                // exception
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Stream could not be opened: "
                    + url);
        }

        out.println("Opening " + playUrl);

        // LOADING THREAD
        if (loadingThread != null) {
            loadingThread.setStop();
        }

        loadingThread = new StreamLoadingThread(playUrl);
        loadingThread.addListener(streamLoadingListener);

        if (listeners != null) {
            for (PlayerLoadingListener listener : listeners) {
                loadingThread.addListener(listener);
            }
        }

        loadingThread.start();
    }

    public void notifyListeners(String message) {
        if (listeners == null)
            return;
        for (PlayerListener l : listeners) {
            l.message(message, this);
        }
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public synchronized Channel getChannel() {
        return channel;
    }

    public synchronized void pause() {
        boolean paused = isPaused();

        if (playerCallBack != null)
            playerCallBack.setEnabled(paused);

        if (channel != null && !channel.isNull()) {
            channel.setPaused(!paused);
            notifyListeners("toggledpause");
        }

    }

    public synchronized boolean isPaused() {
        if (channel.isNull())
            return false;
        channel.getPaused(buffer);
        return buffer.get(0) != 0;

    }

    public synchronized boolean isPlaying() {
        if (channel.isNull()) {
            return false;
        }

        channel.isPlaying(buffer);
        return buffer.get(0) != 0;

    }

    public synchronized void stop() {
        if (!channel.isNull()) {
            channel.stop();
            playerCallBack.setEnabled(false);
            notifyListeners("stopped");
        }
    }

    public synchronized void setPosition(int position) {
        if (!channel.isNull())
            channel.setPosition(position, FMOD_TIMEUNIT_MS);
    }

    public synchronized void setVolume(float gain) {
        if (!channel.isNull()) {
            channel.setVolume(gain);           
        }
        this.gain = gain;
    }

    public synchronized int getPosition() {
        result = channel.getPosition(buffer.asIntBuffer(), FMOD_TIMEUNIT_MS);
        if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE))
            ERRCHECK(result);
        return buffer.getInt(0);
    }

    public synchronized int getLength() {
        result = sound.getLength(buffer.asIntBuffer(), FMOD_TIMEUNIT_MS);
        if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE))
            ERRCHECK(result);
        return buffer.getInt(0);
    }

    public synchronized ArrayList<PlayerListener> getListeners() {
        return listeners;
    }

    public boolean isInAdvancingMode() {
        return isInAdvancingMode;
    }

    public void setInAdvancingMode(boolean b) {
        isInAdvancingMode = b;
    }

    public synchronized void addListener(PlayerListener listener) {
        if (listeners == null)
            listeners = new ArrayList<PlayerListener>();
        listeners.add(listener);
    }

    public synchronized void cleanUp() {
        if (!sound.isNull()) {
            setInAdvancingMode(false);
            stop();
            result = sound.release();
            ERRCHECK(result);
            result = system.close();
            ERRCHECK(result);
            result = system.release();
            ERRCHECK(result);
        }

        if (playerCallBack != null) {
            playerCallBack.stop = true;
        }
    }

    class StreamLoadingThread extends StoppableThread {
        private final String url;
        private ArrayList<PlayerLoadingListener> loadingListeners;

        public StreamLoadingThread(String url) {
            this.url = url;

        }

        @Override
        public void run() {
            Benchmark benchmark = new Benchmark();
            benchmark.start();

            synchronized (playerMutex) {
                result = system.createStream(url, playbackMode | FMOD_2D, null,
                        sound);
            }
            if (!ERRCHECK(result)) {
                // throw new PlaybackError("Error opening file");
                out.println("Error opening file " + url);
                loadError();
                return;
            }

            benchmark.stop();
            out.println("Opening stream took " + benchmark.getTimeTaken()
                    + " ms");

            if (!isStopped()) {
                finished();
            }
        }

        private void loadError() {
            // notify listeners
            if (loadingListeners != null) {
                for (PlayerLoadingListener l : loadingListeners) {
                    l.loadError();
                }
            }

        }

        private void finished() {
            // notify listeners
            if (loadingListeners != null) {
                for (PlayerLoadingListener l : loadingListeners) {
                    l.loaded(url);
                }
            }

        }

        public void addListener(PlayerLoadingListener l) {
            if (loadingListeners == null) {
                loadingListeners = new ArrayList<PlayerLoadingListener>();
            }

            loadingListeners.add(l);
        }
    }

    class StreamLoadingListener implements PlayerLoadingListener {
        public void loaded(final String url) {
            // do this in the main thread
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    Benchmark benchmark = new Benchmark();
                    benchmark.start();

                    // play the track
                    // get meta-data
                    metaData = MetaData.factory(Player.this, url);
                    metaData.setPath(url);
                    metaData.read(url, system);

                    /*
                     * Play the sound.
                     */
                    synchronized (playerMutex) {
                        result = system.playSound(FMOD_CHANNEL_FREE, sound,
                                false, channel);
                        channel.setVolume(gain);
                        ERRCHECK(result);
                    }

                    if (playerCallBack != null) {
                        // sleep player callback thread until playback has
                        // started
                        while (!isPlaying()) {
                            playerCallBack.setEnabled(false);
                        }
                        // reenable the callback
                        playerCallBack.setEnabled(true);
                    } else {

                        playerCallBack = new PlayerCallBack(Player.this,
                                CALLBACK_REFRESH_INTERVAL);
                        playerCallBack.start();
                    }

                    benchmark.stop();
                    out.println("Opening file took an additional "
                            + benchmark.getTimeTaken() + " ms");

                    notifyListeners("playing");
                }

            });

        }

        public void loadError() {
        }
    }

    public void listenersEnabled(boolean b) {
        this.listenersEnabled = b;
    }

    class PlayerCallBack extends Thread {
        public boolean stop;

        private Player player;

        private ArrayList<PlayerListener> listeners;

        private int refreshInterval;

        private boolean enabled;

        // private ByteBuffer buffer;

        public PlayerCallBack(Player player, int refreshInterval) {
            this.player = player;
            this.refreshInterval = refreshInterval;
            this.listeners = player.getListeners();

            this.stop = false;
            // this.buffer = BufferUtils.newByteBuffer(BufferUtils.SIZEOF_INT);
            this.enabled = true;
        }

        public void setEnabled(boolean enable) {
            this.enabled = enable;
        }

        @Override
        public void run() {
            PlayerCallBackData data = new PlayerCallBackData();

            while (!stop) {
                if (enabled && listeners != null) {
                    for (PlayerListener l : listeners) {
                        synchronized (playerMutex) {
                            data.paused = player.isPaused();
                            data.playing = player.isPlaying();
                            data.posMs = player.getPosition();
                            data.totalMs = player.getLength();

                            if (listenersEnabled) {
                                l.callback(data);
                            }
                        }
                    }

                    // disable callback if stopped
                    if (!data.playing && !data.paused) {
                        enabled = false;
                        player.stop();
                    }
                }
                try {
                    Thread.sleep(refreshInterval);
                } catch (InterruptedException e) {
                    out.println("Interrupted");
                }
            }
        }
    }
}
