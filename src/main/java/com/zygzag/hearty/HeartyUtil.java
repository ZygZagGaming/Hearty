package com.zygzag.hearty;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class HeartyUtil {
    public static <T extends Comparable<T>> List<T> sorted(List<T> list) {
        return sorted(list, Comparable::compareTo);
    }

    public static <T> List<T> sorted(List<T> list, BiFunction<T, T, Integer> compareTo) {
        if (list.size() <= 1) return list;

        int mid = list.size() / 2;
        List<T> a = sorted(subList(list, 0, mid), compareTo);
        List<T> b = sorted(subList(list, mid, list.size()), compareTo);
        List<T> fin = new LinkedList<>();
        int x = 0, y = 0;
        while (x < a.size() || y < b.size()) {
            if (y >= b.size() || (x < a.size() && compareTo.apply(a.get(x), b.get(y)) < 0)) {
                fin.add(a.get(x));
                x++;
            } else {
                fin.add(b.get(y));
                y++;
            }
        }
        return fin;
    }

    public static <T> List<T> subList(List<T> list, int start, int end) {
        List<T> fin = new LinkedList<>();
        for (int i = start; i < end; i++) fin.add(list.get(i));
        return fin;
    }

    public static <A, B> Map<A, B> associate(List<A> list, Function<A, B> gen) {
        HashMap<A, B> fin = new HashMap<>();
        for (A a : list) fin.put(a, gen.apply(a));
        return fin;
    }

    public static <T> int sumOf(Iterable<T> list, Function<T, Integer> func) {
        int s = 0;
        for (T t : list) s += func.apply(t);
        return s;
    }

    public static <T> List<T> insertSorted(Iterable<T> list, T elem, BiFunction<T, T, Integer> compareTo) {
        boolean inserted = false;
        List<T> fin = new LinkedList<>();
        for (T t : list) {
            fin.add(t);
            if (!inserted && compareTo.apply(elem, t) > 0) {
                fin.add(elem);
                inserted = true;
            }
        }
        return fin;
    }

    public static <I, O> List<O> map(List<I> list, Function<I, O> func) {
        List<O> fin = new LinkedList<>();
        for (I elem : list) fin.add(func.apply(elem));
        return fin;
    }

    public static <T> T first(List<T> list, T def, Predicate<T> predicate) {
        for (T elem : list) if (predicate.test(elem)) return elem;
        return def;
    }
}