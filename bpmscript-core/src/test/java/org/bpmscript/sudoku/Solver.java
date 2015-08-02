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

import java.util.Iterator;

/**
 * 
 */
public class Solver {
    
    public Puzzle solveRecursive(Puzzle puzzle, IPuzzleCallback callback) throws InvalidException, UnsolvableException, CompleteException {
        callback.onPuzzle(puzzle);
        if(!puzzle.validate()) {
//            System.out.println(puzzle);
            throw new InvalidException();
        }
        if(puzzle.complete()) {
            throw new CompleteException();
        }
//        if(puzzle.complete()) {
//            return puzzle;
//        }
        InterimSolution interimSolution = new InterimSolution(puzzle);
        
        // at this point we should have a puzzle that passes at least initial validation, i.e. it's not invalid
        // because the column, row and block values are sensible and none of the blocks as an empty list
        // of possible values.
        
        // reduce the interim solution
        try {
            // if the interim can be reduced call solveRecursive
            Puzzle reduce = interimSolution.reduce();
            return solveRecursive(reduce, callback);
        } catch (UnsolvableException e) {
            
        }
        Iterator<Puzzle> puzzles = interimSolution.getPossiblePuzzles();
        while(puzzles.hasNext()) {
            Puzzle next = puzzles.next();
            try {
                // if the interim can be reduced call solveRecursive
                return solveRecursive(next, callback);
            } catch (InvalidException e) {
                
            } catch (UnsolvableException e) {
                
            } catch (CompleteException e) {
                
            }
        }
        throw new UnsolvableException();
        // if the interim can't be reduced go through each of the 
        // possible values, trying to set them
    }
    
    public void solve(Puzzle puzzle, IPuzzleCallback callback) throws InvalidException, UnsolvableException, CompleteException {
        try {
            solveRecursive(puzzle, callback);
        } catch(UnsolvableException e) {
            
        }
    }
}
