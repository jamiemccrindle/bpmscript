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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 */
public class InterimSolution {

    final private Puzzle puzzle;
    final private Set<Integer>[][] possibleValues;

    @SuppressWarnings("unchecked")
    public InterimSolution(Puzzle puzzle) throws InvalidException {
        super();
        this.possibleValues = new Set[puzzle.size()][puzzle.size()];
        for (int x = 0; x < puzzle.size(); x++) {
            for (int y = 0; y < puzzle.size(); y++) {
                int value = puzzle.getValues()[x][y];
                if (value == 0) {
                    Set<Integer> allValues = puzzle.getAllValues();
                    Set<Integer> rowValues = puzzle.rowValues(x);
                    allValues.removeAll(rowValues);
                    Set<Integer> columnValues = puzzle.columnValues(y);
                    allValues.removeAll(columnValues);
                    Set<Integer> blockValues = puzzle.blockValues(x, y);
                    allValues.removeAll(blockValues);
                    if (allValues.size() == 0) {
                        throw new InvalidException();
                    }
                    possibleValues[x][y] = allValues;
                }
            }
        }
        this.puzzle = puzzle;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    /**
     * @param x
     * @return
     */
    public Set<Integer> getRowPossibleValues(int x) {
        HashSet<Integer> result = new HashSet<Integer>();
        int[][] values = puzzle.getValues();
        for (int y = 0; y < values.length; y++) {
            int value = values[x][y];
            if (value == 0) {
                result.addAll(possibleValues[x][y]);
            }
        }
        return result;
    }

    /**
     * @param y
     * @return
     */
    public Set<Integer> getColumnPossibleValues(int y) {
        HashSet<Integer> result = new HashSet<Integer>();
        int[][] values = puzzle.getValues();
        for (int x = 0; x < values.length; x++) {
            int value = values[x][y];
            if (value == 0) {
                result.addAll(possibleValues[x][y]);
            }
        }
        return result;
    }

    /**
     * @param x
     * @param y
     * @return
     */
    public Set<Integer> getBlockPossibleValues(int x, int y) {
        HashSet<Integer> result = new HashSet<Integer>();

        int blockSize = puzzle.getBlockSize();
        int[][] values = puzzle.getValues();

        // ok, so we need to work out which block we're in...
        // and to do that we need to find out how many blocks there are... :)
        int blockX = x / blockSize;
        int blockY = y / blockSize;

        // right now we need to go through all values values in the block...
        for (int bx = blockSize * blockX; bx < (blockX + 1) * blockSize; ++bx) {
            for (int by = blockSize * blockY; by < (blockY + 1) * blockSize; ++by) {
                int value = values[bx][by];
                if (value == 0 && !(bx == x && by == y)) {
                    result.addAll(possibleValues[bx][by]);
                }
            }
        }
        return result;
    }

    /**
     * @return
     * @throws UnsolvableException 
     */
    public Puzzle reduce() throws UnsolvableException {

        int[][] newvalues = new int[puzzle.size()][puzzle.size()];

        boolean found = false;
        
        for (int x = 0; x < puzzle.size(); x++) {
            for (int y = 0; y < puzzle.size(); y++) {
                int value = puzzle.getValues()[x][y];
                if (value == 0) {
                    if(possibleValues[x][y].size() == 1) {
                        found = true;
                        newvalues[x][y] = possibleValues[x][y].iterator().next();
                    }
                } else {
                    newvalues[x][y] = value;
                }
            }
        }

        for (int x = 0; x < puzzle.size(); x++) {
            for (int y = 0; y < puzzle.size(); y++) {
                int value = newvalues[x][y];
                if (value == 0) {
                    Set<Integer> currentPossibleValues = possibleValues[x][y];
                    {
                        Set<Integer> possibleValues = new HashSet<Integer>(currentPossibleValues);
                        Set<Integer> blockPossibleValues = this.getBlockPossibleValues(x, y);
                        possibleValues.removeAll(blockPossibleValues);
                        if (possibleValues.size() == 1) {
                            newvalues[x][y] = possibleValues.iterator().next();
                            found = true;
                        }
                    }
                    {
                        Set<Integer> possibleValues = new HashSet<Integer>(currentPossibleValues);
                        Set<Integer> rowPossibleValues = this.getRowPossibleValues(x);
                        possibleValues.removeAll(rowPossibleValues);
                        if (possibleValues.size() == 1) {
                            newvalues[x][y] = possibleValues.iterator().next();
                            found = true;
                        }
                    }
                    {
                        Set<Integer> possibleValues = new HashSet<Integer>(currentPossibleValues);
                        Set<Integer> columnPossibleValues = this.getColumnPossibleValues(y);
                        possibleValues.removeAll(columnPossibleValues);
                        if (possibleValues.size() == 1) {
                            newvalues[x][y] = possibleValues.iterator().next();
                            found = true;
                        }
                    }
                }
            }
        }

        if(!found) {
            throw new UnsolvableException();
        }
        
        Puzzle merge = puzzle.merge(newvalues);
//        System.out.println(merge);
        return merge;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return puzzle.toString();
    }

    /**
     * @return
     */
    public Iterator<Puzzle> getPossiblePuzzles() {
        return new PossiblePuzzleIterator();
    }
    
    protected class PossiblePuzzleIterator implements Iterator<Puzzle> {
        
        private Iterator<Integer> values;
        private int x = 0;
        private int y = 0;
        
        public PossiblePuzzleIterator() {
            boolean found = false;
            for(; x < puzzle.size() && !found; ++x) {
                for(y = 0; y < puzzle.size() && !found; ++y) {
                    int value = puzzle.getValues()[x][y];
                    if(value == 0) {
                        values = possibleValues[x][y].iterator();
                        found = true;
                    }
                }
            }
            --x; --y;
        }
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return values.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Puzzle next() {
            return puzzle.merge(x, y, values.next());
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported");
        }
        
    }

}
