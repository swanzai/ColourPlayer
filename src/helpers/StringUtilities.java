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

package helpers;

public class StringUtilities {
    /**
     * Force a specific length of string. If the string is too small, zeros are
     * added to the beginning
     * 
     * @param string
     * @param pad
     * @return
     */
    public static String padZeros(String string, int pad) {
        if (string.length() == pad) {
            return string;
        } else {
            int count = pad - string.length();
            StringBuffer buffer = new StringBuffer();
            buffer.append(string);

            for (int i = 0; i < count; i++) {
                buffer.insert(0, '0');
            }
            return buffer.toString();
        }
    }

    public static String escapeQuotes(String string) {
        string = string.replaceAll("\"", "\"\"");
        string = string.replaceAll("'", "''");
        return string;
    }

    public static String escapeAmpersands(String string) {
        return string.replaceAll("&", "&&");
    }

    public static String msToString(int ms) {
        StringBuffer buffer = new StringBuffer();

        int secs = ms / 1000 % 60;
        int mins = ms / 1000 / 60 % 60;
        int hours = ms / 1000 / 60 / 60 % 60;

        if (hours > 0) {
            buffer.append(hours);
            buffer.append(":");
        }
        buffer.append(mins);
        buffer.append(":");

        buffer.append(padZeros(Integer.toString(secs), 2));

        
        return buffer.toString();
    }

}
