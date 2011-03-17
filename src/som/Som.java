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

import java.util.Observable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import models.Track;
import ca.odell.glazedlists.EventList;

public class Som extends Observable {

    public static final float INITIAL_LEARNING_RATE = 0.1f;

    public static final float RETRAIN_LEARNING_RATE = 0.07f;

//    private static final int WIDTH = 20;
//
//    private static final int HEIGHT = 20;
    
    private static final int WIDTH = 80;

    private static final int HEIGHT = 20;

    private static final int DATA_DIMENSIONS = 3;

    // private static final int TEST_DATA_LENGTH = 500;

    protected static final Random random = new Random();

    private static final int TRAINING_ITERATIONS = 400;

    private static final int UPDATE_TIMER_INTERVAL = 2;

    /**
     * Radius of initial topological neighbourhood
     */
    public static final float INITIAL_MAP_RADIUS = Math.max(WIDTH, HEIGHT) / 2;

    public static final float RETRAIN_MAP_RADIUS = Math.max(WIDTH, HEIGHT) / 4;

    private static final float timeConstant = (TRAINING_ITERATIONS / (float) Math.log(INITIAL_MAP_RADIUS));

    private SomNode[][] network;

    private Node[] trainingData;

    private int iterations = 0;

    private TimerTask updateTimerTask;

    private Object training = new Object();

    private EventList<Track> tracks;

    public Som(EventList<Track> tracks) {
        this.tracks = tracks;

        network = new SomNode[WIDTH][HEIGHT];
    }

    /**
     * Singleton method
     * 
     * @return The UpdateTimerTask object for timing updates to the GUI
     */
    private TimerTask createUpdateTimerTask() {
        if (updateTimerTask == null) {
            return new UpdateTimerTask();
        } else {
            return updateTimerTask;
        }
    }

    public void init() {
        synchronized (training) {
            // generate training data
            // generateTrainingData();

            // make input data nodes from training data
            if (tracks != null) {
                trainingData = new Node[tracks.size()];
                int i = 0;

                for (Track track : tracks) {
                    trainingData[i] = new Node(new float[] { (float) track.getColour()[0] / 255, (float) track.getColour()[1] / 255,
                            (float) track.getColour()[2] / 255 });
                    i++;
                }
            }

            // create SOM nodes
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    network[i][j] = new SomNode(DATA_DIMENSIONS, i, j);
                }
            }
        }
    }

    public void initRetrain() {
        synchronized (training) {
            // generate training data
            // generateTrainingData();

            // make input data nodes from training data
            trainingData = new Node[tracks.size()];
            int i = 0;

            for (Track track : tracks) {
                trainingData[i] = new Node(new float[] { (float) track.getColour()[0] / 255, (float) track.getColour()[1] / 255,
                        (float) track.getColour()[2] / 255 });
                i++;
            }
        }
    }

    // private void generateTrainingData() {
    // data = new Node[TEST_DATA_LENGTH];
    //
    // for (int i = 0; i < TEST_DATA_LENGTH; i++) {
    // data[i] = new Node(Som.generateRandomWeights());
    // }
    // }

    // /**
    // * Associates tracks to their nearest colour
    // *
    // * Warning: some of this code has been copied and pasted for efficiency
    // * reasons from train()
    // */
    // private void associateNearestColours() {
    // float minDistanceSq = 999;
    // float distanceSq;
    // TrackSomNode nearestSomNode = null;
    //
    // // get random node
    // TrackSomNode somNode;
    // for (Node trainingNode : trainingData) {
    // // find SomNode with nearest weight
    // for (int i = 0; i < WIDTH; i++) {
    // for (int j = 0; j < HEIGHT; j++) {
    // somNode = network[i][j];
    // distanceSq = somNode.distanceTo(trainingNode);
    // if (distanceSq < minDistanceSq) {
    // minDistanceSq = distanceSq;
    // nearestSomNode = somNode;
    // }
    // }
    // }
    //
    // // associate to nearest node
    // nearestSomNode.addTrackId(trainingNode.getTrackId());
    // minDistanceSq = 999;
    // }
    //
    // setChanged();
    // notifyObservers("finished");
    // }

    public void train(final float initialLearningRate, final float mapRadius) {
        this.train(initialLearningRate, mapRadius, false);
    }

    /**
     * Train one epoch
     */
    public void train(final float initialLearningRate, final float mapRadius, final boolean animate) {
        if (trainingData.length == 0) {
            return; // no som data to train on
        }
        new Thread(new Runnable() {
            public void run() {
                synchronized (training) {
                    iterations = 0;
                    Timer updateTimer = null;

                    while (iterations < TRAINING_ITERATIONS) {
                        float learningRate = initialLearningRate;

                        // get random node
                        Node trainingNode = trainingData[(int) Math.round(random.nextDouble() * (trainingData.length - 1))];

                        // find SomNode with nearest weight
                        float minDistanceSq = 999;
                        float distanceSq;
                        SomNode nearestNode = null;
                        SomNode somNode;

                        for (int i = 0; i < WIDTH; i++) {
                            for (int j = 0; j < HEIGHT; j++) {
                                somNode = network[i][j];
                                distanceSq = somNode.distanceTo(trainingNode);
                                if (distanceSq < minDistanceSq) {
                                    minDistanceSq = distanceSq;
                                    nearestNode = somNode;
                                }
                            }
                        }

                        // System.out.println("Nearest node: " + nearestNode);

                        if (nearestNode == null)
                            return;

                        // work out topological neighbourhood size

                        // System.out.println("Map radius: " + MAP_RADIUS);
                        float neighbourhoodRadius = (float) (mapRadius * Math.exp(-iterations / timeConstant));

                        // we use square distances so we convert the
                        // neighbourhood
                        // radius to
                        // square distance too
                        double neighbourhoodRadiusSq = neighbourhoodRadius * neighbourhoodRadius;

                        // for each som node, check if it's within neighbourhood
                        float influence;

                        for (int i = 0; i < WIDTH; i++) {
                            for (int j = 0; j < HEIGHT; j++) {
                                somNode = network[i][j];
                                distanceSq = somNode.physicalDistanceTo(nearestNode);

                                int affected = 0;

                                // System.out.println("Distance: " +
                                // distanceSq);
                                // System.out.println("Nei Rad:" +
                                // neighbourhoodRadiusSq);

                                if (distanceSq < neighbourhoodRadiusSq) {
                                    // Amount by which weight will be updated,
                                    // depending
                                    // on
                                    // distance
                                    influence = (float) Math.exp(-(distanceSq) / (2 * neighbourhoodRadiusSq));

                                    // System.out.println("Old: " + somNode);
                                    somNode.adjustWeights(trainingNode, learningRate, influence);
                                    // System.out.println("New: " + somNode);

                                    // scale the learning rate exponentially
                                    learningRate = learningRate * (float) Math.exp(-iterations / TRAINING_ITERATIONS);
                                    // System.out.println("Learning rate now: "
                                    // +
                                    // learningRate);
                                }
                            }
                        }

                        if (animate) {
                            setChanged();
                            notifyObservers(); // repaint
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                            }
                        }

                        iterations++;
                    }

                    setChanged();
                    notifyObservers(); // repaint
                }
            };
        }).start();

    }

    class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            setChanged();
            notifyObservers();
        }
    }

    private static final float[] generateRandomWeights() {
        float[] weights = new float[DATA_DIMENSIONS];

        for (int i = 0; i < DATA_DIMENSIONS; i++) {
            weights[i] = random.nextFloat();
        }

        return weights;
    }

    public static int getHeight() {
        return HEIGHT;
    }

    public static int getWidth() {
        return WIDTH;
    }

    public SomNode[][] getNetwork() {
        return network;
    }
}
