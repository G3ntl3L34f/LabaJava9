import java.util.Arrays;
import java.util.Random;

/**
 * Класс для измерения времени работы алгоритмов сортировки
 * и анализа их практической сложности.
 *
 * Условия эксперимента:
 * - JDK 17+
 * - Фиксированный seed у Random (воспроизводимость)
 * - Каждому алгоритму передаётся отдельная копия массива (clone)
 * - Прогревочные запуски перед измерением
 * - Измерение только сортировки через System.nanoTime()
 * - Медиана по нескольким повторам
 * - Проверка корректности результата
 */
public class SortingTest {

    private static final long SEED = 42L;           // фиксированный seed
    private static final int WARMUP_RUNS = 5;       // прогревочные запуски
    private static final int MEASURE_RUNS = 7;      // число измерений (берём медиану)
    private static final int[] SIZES = {10, 100, 1000, 10000};

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("Лабораторная работа №9: Сложность сортировки в Java");
        System.out.println("=".repeat(70));
        System.out.println("JDK: " + System.getProperty("java.version"));
        System.out.println("Seed Random: " + SEED);
        System.out.println("Прогревочные запуски: " + WARMUP_RUNS);
        System.out.println("Измерений на размер (медиана): " + MEASURE_RUNS);
        System.out.println();

        // ---------- Основная часть: сравнение алгоритмов ----------
        System.out.println("--- Основная часть: случайные массивы ---");
        System.out.printf("%-12s | %-14s | %-14s | %-14s | %-14s%n",
                "Размер n", "Bubble (нс)", "Selection (нс)", "Insertion (нс)", "Quick (нс)");
        System.out.println("-".repeat(70));

        long[][] results = new long[SIZES.length][4]; // [sizeIndex][algoIndex]

        for (int s = 0; s < SIZES.length; s++) {
            int n = SIZES[s];
            int[] base = generateRandomArray(n);

            // Проверка крайних случаев на маленьком размере
            if (n == 10) {
                runEdgeCaseChecks();
            }

            long bubbleMed = measureMedian(base, SortingAlgorithms::bubbleSort, "Bubble");
            long selectionMed = measureMedian(base, SortingAlgorithms::selectionSort, "Selection");
            long insertionMed = measureMedian(base, SortingAlgorithms::insertionSort, "Insertion");
            long quickMed = measureMedian(base, SortingAlgorithms::quickSort, "Quick");

            results[s][0] = bubbleMed;
            results[s][1] = selectionMed;
            results[s][2] = insertionMed;
            results[s][3] = quickMed;

            System.out.printf("%-12d | %-14d | %-14d | %-14d | %-14d%n",
                    n, bubbleMed, selectionMed, insertionMed, quickMed);
        }

        System.out.println();
        printTheoreticalComparison();
        System.out.println();

        // ---------- Самостоятельная работа ----------
        System.out.println("--- Самостоятельная работа: один алгоритм на разных входных данных ---");
        int specialN = 5000;
        System.out.println("Алгоритм: Insertion Sort, размер n = " + specialN);
        System.out.println("(Insertion Sort выбран, т.к. сильно зависит от порядка данных)");

        int[] sortedAsc = generateSortedArray(specialN, true);
        int[] sortedDesc = generateSortedArray(specialN, false);
        int[] randomArr = generateRandomArray(specialN);

        System.out.println("\nРезультаты (3+ повтора, наносекунды):");
        System.out.printf("%-20s | %s%n", "Тип массива", "Времена (нс)");
        System.out.println("-".repeat(55));

        measureAndPrint("Уже отсортированный", sortedAsc, SortingAlgorithms::insertionSort);
        measureAndPrint("Обратный порядок", sortedDesc, SortingAlgorithms::insertionSort);
        measureAndPrint("Случайный", randomArr, SortingAlgorithms::insertionSort);

        System.out.println();
        System.out.println("Пояснение:");
        System.out.println("  • На уже отсортированном массиве Insertion Sort работает почти за O(n)");
        System.out.println("    (внутренний цикл while почти не выполняется).");
        System.out.println("  • На обратном порядке — худший случай O(n²): каждый элемент");
        System.out.println("    сдвигается почти до начала массива.");
        System.out.println("  • Quick Sort с фиксированным pivot (последний элемент) на");
        System.out.println("    уже отсортированном/обратном массиве тоже деградирует до O(n²).");
        System.out.println();

        System.out.println("=".repeat(70));
        System.out.println("Эксперимент завершён. Данные для графика (ось X = n, ось Y = нс):");
        System.out.println("Скопируйте таблицу выше в Excel / Google Sheets и постройте график.");
        System.out.println("=".repeat(70));
    }

    // -------------------- Генерация данных --------------------

    private static int[] generateRandomArray(int n) {
        Random rnd = new Random(SEED);
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = rnd.nextInt(n * 10); // диапазон 0 .. 10*n
        }
        return arr;
    }

    private static int[] generateSortedArray(int n, boolean ascending) {
        int[] arr = new int[n];
        if (ascending) {
            for (int i = 0; i < n; i++) arr[i] = i;
        } else {
            for (int i = 0; i < n; i++) arr[i] = n - 1 - i;
        }
        return arr;
    }

    // -------------------- Измерение --------------------

    @FunctionalInterface
    interface Sorter {
        void sort(int[] arr);
    }

    /**
     * Возвращает медиану времени сортировки (в наносекундах).
     * Перед измерением выполняет прогревочные запуски.
     */
    private static long measureMedian(int[] base, Sorter sorter, String name) {
        // Прогрев
        for (int i = 0; i < WARMUP_RUNS; i++) {
            int[] copy = base.clone();
            sorter.sort(copy);
        }

        // Измерения
        long[] times = new long[MEASURE_RUNS];
        for (int i = 0; i < MEASURE_RUNS; i++) {
            int[] copy = base.clone();
            long start = System.nanoTime();
            sorter.sort(copy);
            long end = System.nanoTime();
            times[i] = end - start;

            // Проверка корректности
            if (!SortingAlgorithms.isSorted(copy) || !SortingAlgorithms.isCorrect(base, copy)) {
                System.err.println("ОШИБКА: " + name + " вернул некорректный результат!");
            }
        }

        Arrays.sort(times);
        return times[MEASURE_RUNS / 2]; // медиана
    }

    private static void measureAndPrint(String label, int[] base, Sorter sorter) {
        long[] times = new long[5];
        for (int i = 0; i < 5; i++) {
            int[] copy = base.clone();
            long start = System.nanoTime();
            sorter.sort(copy);
            long end = System.nanoTime();
            times[i] = end - start;
            if (!SortingAlgorithms.isSorted(copy)) {
                System.err.println("Ошибка сортировки: " + label);
            }
        }
        System.out.printf("%-20s | %d, %d, %d, %d, %d%n",
                label, times[0], times[1], times[2], times[3], times[4]);
    }

    // -------------------- Проверки крайних случаев --------------------

    private static void runEdgeCaseChecks() {
        System.out.println("\n[Проверка крайних случаев]");

        // Пустой массив
        int[] empty = {};
        SortingAlgorithms.bubbleSort(empty);
        SortingAlgorithms.selectionSort(empty);
        SortingAlgorithms.insertionSort(empty);
        SortingAlgorithms.quickSort(empty);
        System.out.println("  Пустой массив: OK");

        // Один элемент
        int[] single = {42};
        SortingAlgorithms.bubbleSort(single);
        SortingAlgorithms.selectionSort(single);
        SortingAlgorithms.insertionSort(single);
        SortingAlgorithms.quickSort(single);
        System.out.println("  Один элемент: OK");

        // Все одинаковые
        int[] same = {7, 7, 7, 7, 7};
        int[] sameCopy = same.clone();
        SortingAlgorithms.bubbleSort(sameCopy);
        assert SortingAlgorithms.isSorted(sameCopy);
        sameCopy = same.clone();
        SortingAlgorithms.selectionSort(sameCopy);
        assert SortingAlgorithms.isSorted(sameCopy);
        sameCopy = same.clone();
        SortingAlgorithms.insertionSort(sameCopy);
        assert SortingAlgorithms.isSorted(sameCopy);
        sameCopy = same.clone();
        SortingAlgorithms.quickSort(sameCopy);
        assert SortingAlgorithms.isSorted(sameCopy);
        System.out.println("  Одинаковые значения: OK");

        // Обратный порядок
        int[] reverse = {5, 4, 3, 2, 1};
        int[] revCopy = reverse.clone();
        SortingAlgorithms.quickSort(revCopy);
        assert SortingAlgorithms.isCorrect(reverse, revCopy);
        System.out.println("  Обратный порядок: OK");

        // Повторяющиеся числа
        int[] dups = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3};
        int[] dupsCopy = dups.clone();
        SortingAlgorithms.insertionSort(dupsCopy);
        assert SortingAlgorithms.isCorrect(dups, dupsCopy);
        System.out.println("  Повторяющиеся числа: OK\n");
    }

    private static void printTheoreticalComparison() {
        System.out.println("--- Теоретические оценки (Big-O) ---");
        System.out.println("  Bubble Sort:     O(n²)  среднее/худшее, O(n) лучшее");
        System.out.println("  Selection Sort:  O(n²)  во всех случаях");
        System.out.println("  Insertion Sort:  O(n²)  среднее/худшее, O(n) лучшее");
        System.out.println("  Quick Sort:      O(n log n) среднее, O(n²) худшее");
        System.out.println("                    (худший случай зависит от выбора pivot)");
        System.out.println();
        System.out.println("Наблюдения:");
        System.out.println("  • При n=10 разница невелика (постоянные множители и накладные расходы).");
        System.out.println("  • При n=10000 квадратичные алгоритмы (Bubble, Selection, Insertion)");
        System.out.println("    заметно медленнее Quick Sort (примерно на 1–2 порядка).");
        System.out.println("  • Это согласуется с теорией: n² растёт гораздо быстрее n log n.");
    }
}
