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

import glazedlists.BlankStringColour;

import java.awt.Color;
import java.util.Comparator;

public class ColourComparator implements Comparator<BlankStringColour> {
    public int compare(BlankStringColour colourObj1, BlankStringColour colourObj2) {
        int[] c1 = colourObj1.colour;
        int[] c2 = colourObj2.colour;
        
        if (c1 == null && c2 != null)
            return -1;
        else if (c2 == null && c1 != null)
            return +1;
        else if (c2 == null && c1 == null)
            return 0;

        float hue1 = Color.RGBtoHSB(c1[0], c1[1], c1[2], null)[0];
        float hue2 = Color.RGBtoHSB(c2[0], c2[1], c2[2], null)[0];

        if (hue1 > hue2)
            return 1;
        else if (hue1 < hue2)
            return -1;
        else
            return 0;
    }
   
}