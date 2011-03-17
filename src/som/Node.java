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

package som;

import java.util.Random;

public class Node {
    protected static final Random random = new Random();

    public float[] weights;

    public Node(float[] weights) {
        setWeights(weights);
    }
    
    /**
     * Blank constructor
     */
    public Node() {
        
    }

    public float[] getWeights() {
        return weights;
    }

    public void setWeights(float[] weights) {
        this.weights = weights;
    }

    /**
     * Find the euclidian distance to another SOM node's weight
     * 
     * @param node
     * @return The SQUARE of the distance (optimization reasons) or -999 if the
     *         lengths of the weights don't match
     */
    public float distanceTo(Node node) {
        float[] weights2 = node.getWeights();

        if (weights2.length != weights.length)
            return -999;

        float summation = 0f, temp;

        for (int i = 0; i < weights.length; i++) {
            temp = weights[i] - weights2[i];
            temp *= temp;
            summation += temp;
        }

        return summation;
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Weights: ");

        for (double w : weights) {
            buffer.append(w + " ");
        }
        
        return buffer.toString();
    }
}
