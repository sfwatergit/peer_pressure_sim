package sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm.concurrent;

import java.util.Random;

/**
 * Created by sidneyfeygin on 8/23/15.
 */
public class ParallelSum {

    public static void main(String[] args) {
        Random rand = new Random();

        int[] arr = new int[100000000];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = rand.nextInt(101) + 1; // 1..100
        }

        long start = System.currentTimeMillis();

        Summation.sum(arr);

        System.out.println("Single: " + (System.currentTimeMillis() - start)); // Single: 44

        start = System.currentTimeMillis();

        Summation.parallelSum(arr);

        System.out.println("Parallel: " + (System.currentTimeMillis() - start)); // Parallel: 25
    }
}
