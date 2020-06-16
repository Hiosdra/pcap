package pcap.common.memory.internal.nio;

import java.nio.ByteBuffer;
import pcap.common.memory.Memory;
import pcap.common.memory.internal.allocator.AbstractPooledMemoryAllocator;

public class PooledHeapByteBuffer extends AbstractPooledByteBuffer {

  public PooledHeapByteBuffer(
      int id,
      AbstractPooledMemoryAllocator allocator,
      int baseIndex,
      ByteBuffer buffer,
      int capacity,
      int maxCapacity,
      int readerIndex,
      int writerIndex) {
    super(id, allocator, baseIndex, buffer, capacity, maxCapacity, readerIndex, writerIndex);
  }

  @Override
  public Memory copy(int index, int length) {
    byte[] b = new byte[length];
    int currentIndex = baseIndex + index;
    getBytes(currentIndex, b, 0, length);
    ByteBuffer copy = ByteBuffer.allocate(length);
    copy.put(b);
    return new PooledHeapByteBuffer(
        id(), allocator, baseIndex, copy, capacity(), maxCapacity(), readerIndex(), writerIndex());
  }

  @Override
  public Memory slice(int index, int length) {
    if (length > capacity - index) {
      throw new IllegalArgumentException(
          String.format("length: %d (expected: length <= %d)", length, capacity - index));
    }
    return new SlicedPooledHeapByteBuffer(index, length, this);
  }

  @Override
  public Memory duplicate() {
    return new PooledHeapByteBuffer(
        id(),
        allocator,
        baseIndex,
        buffer.duplicate(),
        capacity(),
        maxCapacity(),
        readerIndex(),
        writerIndex());
  }
}
