/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.example;

import org.apache.commons.math3.util.Pair;
import org.openjdk.jmh.annotations.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MyBenchmark {

    static public void optimizeBuyAndSell(String inputName) throws IOException {
        //Время О(N)
        //Ресурсоемкость O(1)
        Pair<Integer, Integer> sell = new Pair<>(0, 0);
        Pair<Integer, Integer> buy = new Pair<>(0, 0);
        Pair <Integer, Integer> possibleBuy = new Pair<>(0, 0);
        int lineNumber = 0;
        String currentLine;
        int currentCost;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputName))) {
            currentLine = reader.readLine();
            while (currentLine != null) {
                if (!currentLine.matches("^\\d+$")) throw new IllegalArgumentException();
                lineNumber++;

                if (lineNumber == 1) {
                    currentCost = Integer.parseInt(currentLine);
                    sell = new Pair<>(lineNumber, currentCost);
                    buy = new Pair<>(lineNumber, currentCost);
                    possibleBuy = new Pair<>(lineNumber, currentCost);
                    continue;
                }

                currentCost = Integer.parseInt(currentLine);
                if (currentCost - buy.getSecond() > sell.getSecond() - buy.getSecond())
                    sell = new Pair<>(lineNumber, currentCost);
                if (currentCost - possibleBuy.getSecond() > sell.getSecond() - buy.getSecond()) {
                    sell = new Pair<>(lineNumber, currentCost);
                    buy = new Pair<>(possibleBuy.getFirst(), possibleBuy.getSecond());
                }
                if (possibleBuy.getSecond() > currentCost)
                    possibleBuy = new Pair<>(lineNumber, currentCost);
                currentLine = reader.readLine();
            }
        }
    }

    static public void optimizeBuyAndSell_2(String inputName)  throws IOException {
        //Время O(N)
        //Ресурсоемкость O(N)
        ArrayList<Integer> numbers = new ArrayList<>();
        ArrayList<Integer> dif = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputName))) {
            String currentLine = reader.readLine();
            while (currentLine != null) {
                if (!currentLine.matches("^\\d+$")) throw new IllegalArgumentException();
                Integer number = Integer.parseInt(currentLine);
                numbers.add(number);
                currentLine = reader.readLine();
            }
        }
        for (int i = 0; i < numbers.size() - 1; i++) {
            dif.add(numbers.get(i + 1) - numbers.get(i));
        }
        //Алгоритм Кадана
        Integer ans = dif.get(0);
        int ansLeft = 0;
        int ansRight = 0;
        Integer sum = 0;
        int minPosition = -1;
        for (int j = 0; j < dif.size(); j++) {
            sum += dif.get(j);
            if (sum > ans) {
                ans = sum;
                ansLeft = minPosition + 1;
                ansRight = j;
            }
            if (sum < 0) {
                sum = 0;
                minPosition = j;
            }
        }
        Pair<Integer, Integer> result = new Pair<>(ansLeft + 1, ansRight + 2);
    }

    static public void optimizeBuyAndSell_3(String inputName)  throws IOException {
        //Время O(N^2)
        //Ресурсоемкость O(N)

        int buy = 0; //j
        int sell = 0; //i
        int r = -10000;
        ArrayList<Integer> numbers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputName))) {
            String currentLine = reader.readLine();
            while (currentLine != null) {
                if (!currentLine.matches("^\\d+$")) throw new IllegalArgumentException();
                Integer number = Integer.parseInt(currentLine);
                numbers.add(number);
                currentLine = reader.readLine();
            }
        }
        for (int i = 0; i < numbers.size() - 1; i++) {
            for (int j = i + 1; j < numbers.size(); j++) {
                if (numbers.get(j) - numbers.get(i) > r) {
                    r = numbers.get(j) - numbers.get(i);
                    buy = i + 1;
                    sell = j + 1;
                }
            }
        }
        Pair<Integer, Integer> result = new Pair<>(buy, sell);
    }

    @State(Scope.Thread)
    public static class MyState {
        String filename = "src/input.txt";
        @Setup(Level.Trial)
        public void doSetup() throws IOException {
            int minPrice = 42;
            int maxPrice = 9999;
            Random r = new Random();
            ArrayList<Integer> prices = new ArrayList<>();
            for (int i = 0; i < 100000; i++) {
                Integer price = minPrice + 1 + r.nextInt(maxPrice - 1 - minPrice);
                prices.add(price);
            }
            try (BufferedWriter br = new BufferedWriter(new FileWriter(filename))) {
                for (Integer price : prices) {
                    br.write(price + "\n");
                }
            }
        }
        @TearDown(Level.Trial)
        public void doTearDown() {
            new File(filename).delete();
        }
    }

    @Fork(1) @Measurement(iterations = 10) @Benchmark @OutputTimeUnit(TimeUnit.SECONDS)
    public void testMethod(MyState state) {
        try {
            optimizeBuyAndSell(state.filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Fork(1) @Measurement(iterations = 10) @Benchmark @OutputTimeUnit(TimeUnit.SECONDS)
    public void testMethod2(MyState state) {
        try {
            optimizeBuyAndSell_2(state.filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Fork(1) @Measurement(iterations = 10) @Benchmark @OutputTimeUnit(TimeUnit.SECONDS)
    public void testMethod3(MyState state) {
        try {
            optimizeBuyAndSell_3(state.filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
