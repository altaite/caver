package caver.util;

import java.util.ArrayList;
import java.util.List;

/*
 * Class for testing memory bottlenecks by measuring the amount of memory needed
 * to throw OutOfMemoryException.
 */
public class Memory {

    public static void fill() {
        int MB = 1000000 / 4;
        int x = 10;
        List list = new ArrayList();
        for (int i = 0; i < 1000; i++) {
            list.add(new int[x * MB]);
            System.out.println(((i + 1) * x) + " MB allocated");
        }

    }
}
