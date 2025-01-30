package com.decimelli;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    private static final int WAIT_TIME = 1;
    private static final int LIST_SIZE = 10_000;
    private static final int ELEMENT_MAX_SIZE = 1_000_000;

    private static final ExecutorService e = Executors.newWorkStealingPool();

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        long startTime;
        int sum;
        int[] input = new int[LIST_SIZE];
        Random random = new Random();
        for (int i = 0; i < input.length; i++) {
            input[i] = random.nextInt(ELEMENT_MAX_SIZE);
        }

        startTime = System.currentTimeMillis();
        sum = getSumFast(input, 0, input.length - 1);
        System.out.println("getSumFast -> Sum: " + sum + ", Total time: " + (System.currentTimeMillis() - startTime));

        e.shutdown();
    }

    private static int getSum(int[] input, int start, int end) throws InterruptedException {
        if (end - start == 0) {
            Thread.sleep(WAIT_TIME);
            return input[start];
        }
        if (end - start == 1) {
            Thread.sleep(WAIT_TIME);
            return input[start] + input[end];
        }

        int half = (end + start) / 2;
        return getSum(input, start, half) + getSum(input, half + 1, end);
    }

    private static int getSumSlow(int[] input) throws InterruptedException {
        int sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += input[i];
            Thread.sleep(WAIT_TIME);
        }
        return sum;
    }

    private static Integer getSumFast(int[] input, int start, int end) throws InterruptedException, ExecutionException {
        if (end - start == 0) {
            Thread.sleep(WAIT_TIME);
            return input[start];
        }
        if (end - start == 1) {
            Thread.sleep(WAIT_TIME);
            return input[start] + input[end];
        }

        int half = (end + start) / 2;
        Future<Integer> left = e.submit(() -> getSumFast(input, start, half));
        Future<Integer> right = e.submit(() -> getSumFast(input, half + 1, end));
        return left.get() + right.get();
    }

}
