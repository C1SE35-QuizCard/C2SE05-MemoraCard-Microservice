package com.example.quizcards.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DistinctFunction {
    public static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
