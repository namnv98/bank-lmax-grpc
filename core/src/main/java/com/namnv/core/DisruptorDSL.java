package com.namnv.core;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;

public interface DisruptorDSL<T> {
  Disruptor<T> build(int bufferSize, WaitStrategy waitStrategy);
}
