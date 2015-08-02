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
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

/**
 * 
 */
public class SolverTest extends TestCase {

//    public void testEquals() throws Exception {
//        Puzzle puzzle1 = new Puzzle(3, new int[][] {
//                new int[] {3, 0, 0, 0, 0, 0, 1, 0, 2},
//                new int[] {0, 8, 9, 4, 7, 0, 0, 0, 0},
//                new int[] {0, 0, 0, 1, 9, 0, 0, 0, 0},
//                new int[] {4, 5, 6, 0, 0, 0, 0, 0, 0},
//                new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//                new int[] {0, 9, 0, 0, 8, 0, 3, 0, 0},
//                new int[] {0, 0, 0, 0, 0, 5, 0, 7, 0},
//                new int[] {9, 4, 0, 0, 0, 0, 0, 5, 0},
//                new int[] {2, 3, 0, 8, 0, 0, 0, 0, 6},
//          });
//        Puzzle puzzle2 = new Puzzle(3, new int[][] {
//                new int[] {3, 0, 0, 0, 0, 0, 1, 0, 2},
//                new int[] {0, 8, 9, 4, 7, 0, 0, 0, 0},
//                new int[] {0, 0, 0, 1, 9, 0, 0, 0, 0},
//                new int[] {4, 5, 6, 0, 0, 0, 0, 0, 0},
//                new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//                new int[] {0, 9, 0, 0, 8, 0, 3, 0, 0},
//                new int[] {0, 0, 0, 0, 0, 5, 0, 7, 0},
//                new int[] {9, 4, 0, 0, 0, 0, 0, 5, 0},
//                new int[] {2, 3, 0, 8, 0, 0, 0, 0, 6},
//          });
//        Puzzle puzzle3 = new Puzzle(3, new int[][] {
//                new int[] {3, 1, 0, 0, 0, 0, 1, 0, 2},
//                new int[] {0, 8, 9, 4, 7, 0, 0, 0, 0},
//                new int[] {0, 0, 0, 1, 9, 0, 0, 0, 0},
//                new int[] {4, 5, 6, 0, 0, 0, 0, 0, 0},
//                new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//                new int[] {0, 9, 0, 0, 8, 0, 3, 0, 0},
//                new int[] {0, 0, 0, 0, 0, 5, 0, 7, 0},
//                new int[] {9, 4, 0, 0, 0, 0, 0, 5, 0},
//                new int[] {2, 3, 0, 8, 0, 0, 0, 0, 6},
//          });
//        assertEquals(puzzle1, puzzle2);
//        assertTrue(!puzzle1.equals(puzzle3));
//    }
   
//    public void testSolveAlmostEmpty() throws Exception {
//        final Solver solver = new Solver();
//        Puzzle puzzle = new Puzzle(3, new int[][] {
//              new int[] {1, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//        });
//        
//        assertTrue(puzzle.validate());
//        final AtomicInteger count = new AtomicInteger();
//        final AtomicInteger samePuzzleCount = new AtomicInteger();
//        final HashSet<Puzzle> puzzlesSoFar = new HashSet<Puzzle>();
//        Puzzle solution = solver.solve(puzzle, new IPuzzleCallback() {
//        
//            public void onPuzzle(Puzzle interimSolution) {
//                
//                if(interimSolution.complete()) {
//                    System.out.println(samePuzzleCount.get());
//                    System.out.println(count.incrementAndGet());
//                    System.out.println(interimSolution);
//                }
//                
//                if(puzzlesSoFar.contains(interimSolution)) {
//                    samePuzzleCount.incrementAndGet();
//                } else {
//                    puzzlesSoFar.add(interimSolution);
//                }
////                if(count.incrementAndGet() == 200) {
////                    throw new RuntimeException("stopped");
////                }
////                if(count.incrementAndGet() % 10000 == 0) {
////                    System.out.println(samePuzzleCount.get());
////                    System.out.println(count.get());
////                    System.out.println(interimSolution.toString());
////                }
//            }
//        });
//        assertTrue(solution.complete());
//        assertTrue(solution.validate());
//        System.out.println(samePuzzleCount.get());
//        System.out.println(count.get());
//        System.out.println(solution);
//    }
    public void testSolve() throws Exception {
        final Solver solver = new Solver();
//        Puzzle puzzle = new Puzzle(3, new int[][] {
//              new int[] {3, 0, 0, 0, 0, 0, 1, 0, 2},
//              new int[] {0, 8, 9, 4, 7, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 1, 9, 0, 0, 0, 0},
//              new int[] {4, 5, 6, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 9, 0, 0, 8, 0, 3, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 5, 0, 7, 0},
//              new int[] {9, 4, 0, 0, 0, 0, 0, 5, 0},
//              new int[] {2, 3, 0, 8, 0, 0, 0, 0, 6},
//        });
//      final Solver solver = new Solver();
      Puzzle puzzle = new Puzzle(3, new int[][] {
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
      });
        
        assertTrue(puzzle.validate());
        final AtomicInteger count = new AtomicInteger();
        final AtomicInteger samePuzzleCount = new AtomicInteger();
        final HashSet<Puzzle> puzzlesSoFar = new HashSet<Puzzle>();
        final HashSet<Puzzle> completePuzzles = new HashSet<Puzzle>();
        solver.solve(puzzle, new IPuzzleCallback() {
        
            public void onPuzzle(Puzzle interimSolution) {
                if(interimSolution.complete()) {
//                    System.out.println(samePuzzleCount.get());
//                    System.out.println(count.incrementAndGet());
//                    System.out.println(interimSolution.toString());
                    completePuzzles.add(interimSolution);
                    if (completePuzzles.size() % 10 == 0) {
                        System.out.println(completePuzzles.size());
                        System.out.println(count.get());
                        System.out.println(interimSolution.toString());
                    }
                }
                count.incrementAndGet();
//                if (count.incrementAndGet() % 10000 == 0) {
//                    System.out.println(samePuzzleCount.get());
//                    System.out.println(count.get());
//                    System.out.println(interimSolution.toString());
//                }
//                if(puzzlesSoFar.contains(interimSolution)) {
//                    samePuzzleCount.incrementAndGet();
//                } else {
//                    puzzlesSoFar.add(interimSolution);
//                }
            }
        });
//        assertTrue(solution.complete());
//        assertTrue(solution.validate());
        System.out.println(samePuzzleCount.get());
        System.out.println(count.get());
        System.out.println(completePuzzles.size());
        for (Puzzle complete : completePuzzles) {
            System.out.println(complete);
        }
//        System.out.println(solution);
    }
    
    public static void main(String[] args) throws Exception {
        final Solver solver = new Solver();
        Puzzle puzzle = new Puzzle(3, new int[][] {
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
        });
        
        assertTrue(puzzle.validate());
        final AtomicInteger count = new AtomicInteger();
        solver.solve(puzzle, new IPuzzleCallback() {
        
            public void onPuzzle(Puzzle interimSolution) {
                
                if(count.incrementAndGet() > 1000) {
                    throw new RuntimeException();
                };
            }
        });
        System.out.println(count.get());
    }

//    public void testPuzzleIterator() throws Exception {
//        final Solver solver = new Solver();
//        Puzzle puzzle = new Puzzle(3, new int[][] {
//              new int[] {3, 0, 0, 0, 0, 0, 1, 0, 2},
//              new int[] {0, 8, 9, 4, 7, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 1, 9, 0, 0, 0, 0},
//              new int[] {4, 5, 6, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
//              new int[] {0, 9, 0, 0, 8, 0, 3, 0, 0},
//              new int[] {0, 0, 0, 0, 0, 5, 0, 7, 0},
//              new int[] {9, 4, 0, 0, 0, 0, 0, 5, 0},
//              new int[] {2, 3, 0, 8, 0, 0, 0, 0, 6},
//        });
//        InterimSolution interimSolution = new InterimSolution(puzzle);
//        while(true) {
//            try {
//                Puzzle reduced = interimSolution.reduce();
//                interimSolution = new InterimSolution(reduced);
//            } catch(UnsolvableException e) {
//                break;
//            }
//        }
//        System.out.println(interimSolution);
//        Iterator<Puzzle> possiblePuzzles = interimSolution.getPossiblePuzzles();
//        HashSet<Puzzle> puzzlesSoFar = new HashSet<Puzzle>();
//        while(possiblePuzzles.hasNext()) {
//            Puzzle next = possiblePuzzles.next();
//            if(puzzlesSoFar.contains(next)) {
//                fail("same puzzle\n " + next);
//            } else {
//                puzzlesSoFar.add(next);
//            }
//        }
//    }
//    public void testPuzzleIterator() throws Exception {
//        final Solver solver = new Solver();
//        Puzzle puzzle = new Puzzle(2, new int[][] {
//                new int[] {1, 2, 3, 4},
//                new int[] {4, 3, 2, 1},
//                new int[] {3, 1, 4, 2},
//                new int[] {0, 0, 1, 3},
//        });
//        InterimSolution interimSolution = new InterimSolution(puzzle);
//        Iterator<Puzzle> possiblePuzzles = interimSolution.getPossiblePuzzles();
//        while(possiblePuzzles.hasNext()) {
//            Puzzle next = possiblePuzzles.next();
//            System.out.println(next);
//        }
//    }
//    public void testSmallSolve() throws Exception {
//        final Solver solver = new Solver();
//        Puzzle puzzle = new Puzzle(2, new int[][] { 
//                new int[] { 0, 0, 0, 0 }, 
//                new int[] { 0, 0, 0, 0 },
//                new int[] { 1, 0, 0, 0 }, 
//                new int[] { 0, 0, 0, 0 }, 
//        });
//        assertTrue(puzzle.validate());
//        final AtomicInteger count = new AtomicInteger();
//        Puzzle solution = solver.solve(puzzle, new IPuzzleCallback() {
//            public void onPuzzle(Puzzle interimSolution) {
//                if (count.incrementAndGet() % 10000 == 0) {
//                    System.out.println(count.get());
//                    System.out.println(interimSolution.toString());
//                }
//            }
//        });
//        assertTrue(solution.complete());
//        assertTrue(solution.validate());
//        System.out.println(count.get());
//        System.out.println(solution);
//    }
}
