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

import static java.lang.System.out;
import models.PlayerCallBackData;
import controllers.Player;

public class ConsolePlayerListener implements PlayerListener {
    public void callback(PlayerCallBackData data) {
        int ms = 0, lenMs = 0;

        /*
         * result = channel.getPosition(buffer.asIntBuffer(), FMOD_TIMEUNIT_MS);
         * if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE))
         * ERRCHECK(result); ms = buffer.getInt(0);
         * 
         * result = sound.getLength(buffer.asIntBuffer(), FMOD_TIMEUNIT_MS); if
         * ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE))
         * ERRCHECK(result); lenMs = buffer.getInt(0);
         */

        out.printf("Time %02d:%02d:%02d/%02d:%02d:%02d : %s\r",
                data.posMs / 1000 / 60, data.posMs / 1000 % 60,
                data.posMs / 10 % 100, data.totalMs / 1000 / 60,
                data.totalMs / 1000 % 60, data.totalMs / 10 % 100,
                data.paused ? "Paused " : data.playing ? "Playing" : "Stopped");
    }

    public void message(String message, Player player) {
    }
}
