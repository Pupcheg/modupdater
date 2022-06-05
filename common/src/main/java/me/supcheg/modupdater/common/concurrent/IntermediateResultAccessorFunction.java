package me.supcheg.modupdater.common.concurrent;

import java.util.function.Function;

@FunctionalInterface
public interface IntermediateResultAccessorFunction<I, R> extends Function<IntermediateResultProcess<I, R>.IntermediateResultAccessor, R> {}
