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

import listeners.ProgressDragListener;
import models.PlayerCallBackData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import views.DraggableProgressBar;
import views.PlayerListener;

public class TrackProgressBar extends DraggableProgressBar implements PlayerListener {
    private Player player;

    protected void setPlayer(final Player player) {
        this.player = player;
        
        this.addDragListener(new ProgressDragListener() {
            public void dragPerformed(int value) {
                if (player != null)
                    player.setPosition(value);
            };
        });
    }

    public TrackProgressBar(Composite parent, int style) {
        super(parent, SWT.DOUBLE_BUFFERED);

        setToolTipText("Progress");
    }

    public void callback(PlayerCallBackData data) {
        setValue(data.posMs);
    }

    public void message(final String message, final Player player) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if (message.equals("playing")) {
                    setEnabled(true);
                    setMaxValue(player.getLength());
                    setValue(0);
                } else if (message.equals("stopped")) {
                    setEnabled(false);
                }
            }
        });
    }
}
