/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bpmscript.sudoku;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class Puzzle {
    
    final private int[][] values;
    final private int blockSize;
    
    public Puzzle(int blockSize, int[][] values) {
        super();
        this.blockSize = blockSize;
        this.values = values;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + blockSize;
        result = prime * result + Arrays.deepHashCode(values);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Puzzle other = (Puzzle) obj;
        if (blockSize != other.blockSize)
            return false;
        if (!Arrays.deepEquals(values, other.values))
            return false;
        return true;
    }

    public int[][] getValues() {
        return values;
    }

    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @return
     */
    public int size() {
        return values.length;
    }
    
    /**
     * @param x
     * @return
     */
    public Set<Integer> rowValues(int x) {
        HashSet<Integer> result = new HashSet<Integer>();
        for(int y = 0; y < values.length; y++) {
            int value = values[x][y];
            if(value > 0) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * @param y
     * @return
     */
    public Set<Integer> columnValues(int y) {
        HashSet<Integer> result = new HashSet<Integer>();
        for(int x = 0; x < values.length; x++) {
            int value = values[x][y];
            if(value > 0) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * @param x
     * @param y
     * @return
     */
    public Set<Integer> blockValues(int x, int y) {
        HashSet<Integer> result = new HashSet<Integer>();

        // ok, so we need to work out which block we're in...
        // and to do that we need to find out how many blocks there are... :)
        int blockX = x / blockSize;
        int blockY = y / blockSize;

        // right now we need to go through all values values in the block...
        for(int bx = blockSize * blockX; bx < (blockX + 1) * blockSize; ++bx) {
            for(int by = blockSize * blockY; by < (blockY + 1) * blockSize; ++by) {
                int value = values[bx][by];
                if(value > 0) {
                    result.add(value);
                }
            }
        }
        return result;
    }


    public boolean validate() {
        // check rows
        for(int x = 0; x < values.length; ++x) {
            Set<Integer> allValues = getAllValues();
            for(int y = 0; y < values.length; ++y) {
                int value = values[x][y];
                if(value != 0) {
                    if(allValues.contains(value)) {
                        allValues.remove(value);
                    } else {
                        return false;
                    }
                }
            }
        }
        // check columns
        for(int y = 0; y < values.length; ++y) {
            Set<Integer> allValues = getAllValues();
            for(int x = 0; x < values.length; ++x) {
                int value = values[x][y];
                if(value != 0) {
                    if(allValues.contains(value)) {
                        allValues.remove(value);
                    } else {
                        return false;
                    }
                }
            }
        }
        // check blocks
        int blockSize = this.blockSize;
        for(int blockX = 0; blockX < blockSize; ++blockX) {
            for(int blockY = 0; blockY < blockSize; ++blockY) {
                // ok, now we have a block
                // going to assume for now that a block is also blocksize
                Set<Integer> allValues = getAllValues();
                for(int bx = blockX * blockSize; bx < (blockX + 1) * blockSize; bx++) {
                    for(int by = blockY * blockSize; by < (blockY + 1) * blockSize; by++) {
                        int value = values[bx][by];
                        if(value != 0) {
                            if(allValues.contains(value)) {
                                allValues.remove(value);
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    public Set<Integer> getAllValues() {
        HashSet<Integer> result = new HashSet<Integer>();
        for(int i = 1; i < values.length + 1; ++i) {
            result.add(i);
        }
        return result;
    }
    

    public boolean complete() {
        // check for empty cells
        for(int x = 0; x < values.length; ++x) {
            for(int y = 0; y < values.length; ++y) {
                int value = values[x][y];
                if(value == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param newvalues
     * @return
     */
    public Puzzle merge(int[][] newvalues) {
        // not cloning newvalues even though i probably should
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values.length; y++) {
                int value = values[x][y];
                if(value != 0) {
                    newvalues[x][y] = value;
                }
            }
        }
        return new Puzzle(blockSize, newvalues);
    }
    
    /**
     * @param newvalues
     * @return
     */
    public Puzzle merge(int valueX, int valueY, int newValue) {
        int[][] newvalues = new int[values.length][values.length];
        for (int x = 0; x < values.length; ++x) {
            for (int y = 0; y < values.length; ++y) {
                int value = values[x][y];
                if(valueX == x && valueY == y) {
                    newvalues[x][y] = newValue;
                } else {
                    newvalues[x][y] = value;
                }
            }
        }
        return new Puzzle(blockSize, newvalues);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(int y = 0; y < values.length; y++) {
            builder.append("----");
        }
        builder.append("-");
        builder.append("\n");
        for(int x = 0; x < values.length; x++) {
            builder.append("|");
            for(int y = 0; y < values.length; y++) {
                int value = values[x][y];
                builder.append(" " + (value != 0 ? value : " ") + " |");
            }
            builder.append("\n");
            for(int y = 0; y < values.length; y++) {
                builder.append("----");
            }
            builder.append("-");
            builder.append("\n");
        }
        builder.append("\n");
        return builder.toString();
    }

}
