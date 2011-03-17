package helpers;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColourRegistry {
    private LinkedHashMap<Integer, Color> colourMap = new LinkedHashMap<Integer, Color>();
    int updateCounter = 0;

    protected Color hitCache(int[] colour) {
        // check cache
        Integer key = new Integer(StringUtilities.padZeros(Integer
                .toString(colour[0]), 3)
                + StringUtilities.padZeros(Integer.toString(colour[1]), 3)
                + StringUtilities.padZeros(Integer.toString(colour[2]), 3));
        Color cachedColour;

        if (colourMap.containsKey(key)) {
            return colourMap.get(key);
        } else {
            cachedColour = new Color(Display.getDefault(), colour[0],
                    colour[1], colour[2]);
            colourMap.put(key, cachedColour);
        }

        return cachedColour;
    }

    public void cleanUp() {
        // dispose colours
        Collection<Color> values = colourMap.values();
        System.out.println("Disposing colours (Length: " + values.size() + ")");
        Iterator<Color> valueIterator = values.iterator();
        while (valueIterator.hasNext()) {
            valueIterator.next().dispose();
            valueIterator.remove();
        }
    }

    /**
     * Clear the cache if a certain number of updates has occurred
     * 
     * @param updates
     */
    public void clearCache(int updates) {
        if (++updateCounter >= updates) {
            cleanUp();
            updateCounter = 0;
        }
    }
}
