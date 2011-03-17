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

package glazedlists.comparators;

import java.util.Comparator;

/**
 * A comparator that ignores the leading "the" in strings
 * 
 * @author Michael Voong
 * 
 */
public class EnglishComparator implements Comparator<String> {
    public int compare(String o1, String o2) {
        if (o1.indexOf("The ") == 0 || o1.indexOf("the ") == 0) {
            o1 = o1.substring(4);
        }
        if (o2.indexOf("The ") == 0 || o2.indexOf("the ") == 0) {
            o2 = o2.substring(4);
        }

        return o1.compareTo(o2);
    }
}
