package com.namnv.core;

public interface BufferEventDispatcher<T extends BufferEvent> {
  void dispatch(T event);
}
