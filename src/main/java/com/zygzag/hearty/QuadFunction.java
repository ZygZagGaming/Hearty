package com.zygzag.hearty;

@FunctionalInterface
public interface QuadFunction<A, B, C, D, T> {
    T apply(A a, B b, C c, D d);
}
