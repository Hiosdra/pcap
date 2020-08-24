/** This code is licenced under the GPL version 2. */
package pcap.common.memory.internal.nio;

import java.nio.ByteBuffer;
import pcap.common.annotation.Inclubating;
import pcap.common.memory.Memory;

/** @author <a href="mailto:contact@ardikars.com">Ardika Rommy Sanjaya</a> */
@Inclubating
public class HeapByteBuffer extends AbstractByteBuffer implements Memory.Heap {

  public HeapByteBuffer(
      int baseIndex,
      ByteBuffer buffer,
      int capacity,
      int maxCapacity,
      int readerIndex,
      int writerIndex) {
    super(baseIndex, buffer, capacity, maxCapacity, readerIndex, writerIndex);
  }

  @Override
  public Memory copy(int index, int length) {
    byte[] b = new byte[length];
    int currentIndex = baseIndex + index;
    getBytes(currentIndex, b, 0, length);
    ByteBuffer copy = ByteBuffer.allocate(length);
    copy.put(b);
    return new HeapByteBuffer(
        baseIndex, copy, capacity(), maxCapacity(), readerIndex(), writerIndex());
  }

  @Override
  public Memory slice(int index, int length) {
    return new SlicedHeapByteBuffer(index, length, this);
  }

  @Override
  public Memory duplicate() {
    return new HeapByteBuffer(
        baseIndex, buffer.duplicate(), capacity(), maxCapacity(), readerIndex(), writerIndex());
  }

  public static class SlicedHeapByteBuffer extends HeapByteBuffer implements Memory.Sliced {

    final HeapByteBuffer previous;

    public SlicedHeapByteBuffer(int index, int length, HeapByteBuffer previous) {
      super(
          previous.baseIndex + index,
          previous.buffer(ByteBuffer.class).duplicate(),
          length,
          previous.maxCapacity() - index < 0 ? 0 : previous.maxCapacity() - index,
          previous.readerIndex() - index < 0 ? 0 : previous.readerIndex() - index,
          previous.writerIndex() - index < 0 ? 0 : previous.writerIndex() - index);
      this.previous = previous;
    }

    @Override
    public Memory copy(int index, int length) {
      byte[] b = new byte[length];
      int currentIndex = baseIndex + index;
      getBytes(currentIndex, b, 0, length);
      ByteBuffer copy = ByteBuffer.allocate(length);
      copy.put(b);
      return new HeapByteBuffer(
          baseIndex, copy, capacity(), maxCapacity(), readerIndex(), writerIndex());
    }

    @Override
    public Memory duplicate() {
      return new SlicedHeapByteBuffer(previous.baseIndex - baseIndex, capacity, previous);
    }

    @Override
    public Memory unSlice() {
      return previous;
    }
  }
}
