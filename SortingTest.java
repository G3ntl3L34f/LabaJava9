import java.util.Arrays;
import java.util.Random;

public class SortingTest {

    static final long SEED = 42;
    static final int[] SIZES = {10, 100, 1000, 10000};

    public static void main(String[] args) {
        System.out.printf("%-8s %12s %12s %12s %12s%n",
                "n", "Bubble", "Selection", "Insertion", "Quick");

        for (int n : SIZES) {
            int[] base = randomArray(n);

            long b = measure(base, SortingAlgorithms::bubbleSort);
            long s = measure(base, SortingAlgorithms::selectionSort);
            long i = measure(base, SortingAlgorithms::insertionSort);
            long q = measure(base, SortingAlgorithms::quickSort);

            System.out.printf("%-8d %12d %12d %12d %12d%n", n, b, s, i, q);
        }
    }

    static int[] randomArray(int n) {
        Random r = new Random(SEED);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = r.nextInt(n * 10);
        return a;
    }

    static long measure(int[] base, java.util.function.Consumer<int[]> sort) {
        // прогрев
        for (int k = 0; k < 3; k++) {
            int[] c = base.clone();
            sort.accept(c);
        }
        // 5 замеров, медиана
        long[] t = new long[5];
        for (int k = 0; k < 5; k++) {
            int[] c = base.clone();
            long start = System.nanoTime();
            sort.accept(c);
            t[k] = System.nanoTime() - start;
        }
        Arrays.sort(t);
        return t[2];
    }
}
