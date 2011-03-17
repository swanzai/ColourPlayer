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

public class SomNode extends Node {
    private int xPos, yPos;

    private static float neighbourhoodRadius;

    public SomNode(int numWeights, int x, int y) {
        setWeights(initializeWeights(numWeights));

        this.xPos = x;
        this.yPos = y;
    }

    /**
     * Initialize the weights of the node to small random values
     * 
     * @param numWeights
     * @return
     */
    public static float[] initializeWeights(int numWeights) {
        float[] weights = new float[numWeights];

        for (int i = 0; i < numWeights; i++) {
            // between -0.5 and +0.5
            weights[i] = random.nextFloat();
        }

        return weights;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("(" + xPos + ", " + yPos + ")");
//        buffer.append("Num weights: " + weights.length + "\n");
//        buffer.append("Weights: ");
//
//        for (double w : weights) {
//            buffer.append(w + " ");
//        }
        return buffer.toString();
    }

    /**
     * Adjust weights around the winning node that's in the topological
     * neighbourhood
     * 
     * @param inputNode
     * @param learningRate
     * @param influence
     */
    public void adjustWeights(Node inputNode, float learningRate,
            float influence) {
        
        float weight, weight2;
        float[] weights2 = inputNode.getWeights();

        for (int w = 0; w < weights.length; w++) {
            weight = weights[w];
            weight2 = weights2[w];

            weights[w] += influence * learningRate * (weight2 - weight);
        }
    }

    /**
     * Calculates the physical distance in the network between nodes. Used for
     * calculating the topological neighbourhood
     * 
     * @param node
     * @return The SQUARE of the distance (optimization reasons)
     */
    public float physicalDistanceTo(SomNode node) {
        int xLeg, yLeg;

        xLeg = xPos - node.getXPos();
        xLeg *= xLeg;
        yLeg = yPos - node.getYPos();
        yLeg *= yLeg;

        return xLeg + yLeg;
    }

    public int getXPos() {
        return xPos;
    }

    public void setXPos(int pos) {
        xPos = pos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setYPos(int pos) {
        yPos = pos;
    }
}
