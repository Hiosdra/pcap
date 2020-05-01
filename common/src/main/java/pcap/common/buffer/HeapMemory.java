package pcap.common.buffer;

import pcap.common.util.Bytes;
import pcap.common.util.Validate;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class HeapMemory extends AbstractMemory {

  private static final VarHandle VAR_HANDLE = MethodHandles.arrayElementVarHandle(boolean[].class);

  private final byte[] buffer;

  HeapMemory(
      byte[] buffer,
      int baseIndex,
      int capacity,
      int maxCapacity,
      int readerIndex,
      int writerIndex) {
    super(baseIndex, capacity, maxCapacity, readerIndex, writerIndex);
    this.buffer = buffer;
  }

  @Override
  public Memory capacity(int newCapacity) {
    Validate.notIllegalArgument(
        newCapacity <= maxCapacity,
        String.format(
            "capacity: %d (expected: newCapacity(%d) <= maxCapacity(%d))",
            newCapacity, maxCapacity));
    byte[] buffer = new byte[newCapacity];
    final HeapMemory heapMemory =
        new HeapMemory(buffer, baseIndex, newCapacity, maxCapacity, readerIndex(), writerIndex());
    return heapMemory;
  }

  @Override
  public byte getByte(int index) {
    return (byte) VAR_HANDLE.get(buffer, baseIndex + index);
  }

  @Override
  public short getShort(int index) {
    return (short)
        ((byte) VAR_HANDLE.get(buffer, baseIndex + index) & 0xFF
            | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 1) & 0xFF) << 8);
  }

  @Override
  public short getShortLE(int index) {
    return Short.reverseBytes(getShort(index));
  }

  @Override
  public int getInt(int index) {
    return ((byte) VAR_HANDLE.get(buffer, baseIndex + index) & 0xFF
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 1) & 0xFF) << 8
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 2) & 0xFF) << 16
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 3) & 0xFF) << 24);
  }

  @Override
  public int getIntLE(int index) {
    return Integer.reverseBytes(getInt(index));
  }

  @Override
  public long getLong(int index) {
    return ((byte) VAR_HANDLE.get(buffer, baseIndex + index) & 0xFF
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 1) & 0xFF) << 8
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 2) & 0xFF) << 16
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 2) & 0xFF) << 24
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 2) & 0xFF) << 32
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 2) & 0xFF) << 40
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 2) & 0xFF) << 48
        | ((byte) VAR_HANDLE.get(buffer, baseIndex + index + 3) & 0xFF) << 56);
  }

  @Override
  public long getLongLE(int index) {
    return Long.reverseBytes(getLong(index));
  }

  @Override
  public Memory getBytes(int index, Memory dst, int dstIndex, int length) {
    if (dst instanceof HeapMemory) {
      final HeapMemory dstMem = (HeapMemory) dst;
      System.arraycopy(
          buffer, baseIndex + index, dstMem.buffer, dstMem.baseIndex + dstIndex, length);
    }
    return this;
  }

  @Override
  public Memory getBytes(int index, byte[] dst, int dstIndex, int length) {
    System.arraycopy(buffer, baseIndex + index, dst, dstIndex, length);
    return this;
  }

  @Override
  public Memory setByte(int index, int value) {
    VAR_HANDLE.set(buffer, baseIndex + index, (byte) value);
    return this;
  }

  @Override
  public Memory setShort(int index, int value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray((short) value)) {
      VAR_HANDLE.set(buffer, baseIndex + index + offset++, b);
    }
    return this;
  }

  @Override
  public Memory setShortLE(int index, int value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray((short) value, ByteOrder.LITTLE_ENDIAN)) {
      VAR_HANDLE.set(buffer, baseIndex + index + offset++, b);
    }
    return this;
  }

  @Override
  public Memory setInt(int index, int value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray(value)) {
      VAR_HANDLE.set(buffer, baseIndex + index + offset++, b);
    }
    return this;
  }

  @Override
  public Memory setIntLE(int index, int value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray(value, ByteOrder.LITTLE_ENDIAN)) {
      VAR_HANDLE.set(buffer, baseIndex + index + offset++, b);
    }
    return this;
  }

  @Override
  public Memory setLong(int index, long value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray(value)) {
      VAR_HANDLE.set(buffer, baseIndex + index + offset++, b);
    }
    return this;
  }

  @Override
  public Memory setLongLE(int index, long value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray(value, ByteOrder.LITTLE_ENDIAN)) {
      VAR_HANDLE.set(buffer, baseIndex + index + offset++, b);
    }
    return this;
  }

  @Override
  public Memory setBytes(int index, Memory src, int srcIndex, int length) {
    if (src instanceof HeapMemory) {
      final HeapMemory srcMem = (HeapMemory) src;
      System.arraycopy(
          srcMem.buffer, srcMem.baseIndex + srcIndex, buffer, baseIndex + index, length);
    }
    return this;
  }

  @Override
  public Memory setBytes(int index, byte[] src, int srcIndex, int length) {
    System.arraycopy(src, srcIndex, buffer, baseIndex + index, length);
    return this;
  }

  @Override
  public Memory copy(int index, int length) {
    Validate.notIllegalArgument(
        length <= capacity - (baseIndex + index),
        String.format(
            "copy: %d %d expected(length(%d) <= capacity(%d))",
            index, length, length, capacity - (baseIndex + index)));
    byte[] buffer = new byte[length];
    System.arraycopy(buffer, baseIndex + index, buffer, 0, length);
    return new HeapMemory(buffer, 0, capacity, maxCapacity, readerIndex(), writtenBytes);
  }

  @Override
  public Memory slice(int index, int length) {
    Validate.notIllegalArgument(
        length <= capacity - (baseIndex + index),
        String.format(
            "slice: %d %d expected(length(%d) <= capacity(%d))",
            index, length, length, capacity - (baseIndex + index)));
    return new HeapMemory(
        buffer,
        baseIndex + index,
        length,
        maxCapacity,
        readerIndex() - index,
        writerIndex() - index);
  }

  @Override
  public Memory duplicate() {
    return new HeapMemory(buffer, baseIndex, capacity, maxCapacity, readerIndex(), writerIndex());
  }

  @Override
  public ByteBuffer nioBuffer() {
    return ByteBuffer.wrap(buffer);
  }

  @Override
  public boolean isDirect() {
    return false;
  }

  @Override
  public long memoryAddress() {
    return 0;
  }

  @Override
  public void close() throws Exception {}

  @Override
  public void release() {}
}
