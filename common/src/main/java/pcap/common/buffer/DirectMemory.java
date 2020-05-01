package pcap.common.buffer;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import pcap.common.util.Bytes;
import pcap.common.util.Validate;

import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class DirectMemory extends AbstractMemory {

  private static final VarHandle VAR_HANDLE =
      MemoryHandles.varHandle(byte.class, ByteOrder.BIG_ENDIAN);

  private final MemorySegment segment;
  private final MemoryAddress address;

  public DirectMemory(
      MemorySegment segment,
      int baseIndex,
      int capacity,
      int maxCapacity,
      int readerIndex,
      int writerIndex) {
    super(baseIndex, capacity, maxCapacity, readerIndex, writerIndex);
    this.segment = segment;
    address = segment.baseAddress();
  }

  @Override
  public Memory capacity(int newCapacity) {
    Validate.notIllegalArgument(
        newCapacity <= maxCapacity,
        String.format(
            "capacity: %d (expected: newCapacity(%d) <= maxCapacity(%d))",
            newCapacity, maxCapacity));
    final MemorySegment segment = MemorySegment.allocateNative(newCapacity);
    final DirectMemory directMemory =
        new DirectMemory(
            segment, baseIndex, newCapacity, maxCapacity, readerIndex(), writerIndex());
    this.segment.close();
    return directMemory;
  }

  @Override
  public byte getByte(int index) {
    return (byte) VAR_HANDLE.get(address.addOffset(baseIndex + index));
  }

  @Override
  public short getShort(int index) {
    return (short)
        ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index)) & 0xFF
            | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 1)) & 0xFF) << 8);
  }

  @Override
  public short getShortLE(int index) {
    return Short.reverseBytes(getShort(index));
  }

  @Override
  public int getInt(int index) {
    return (byte) VAR_HANDLE.get(address.addOffset(baseIndex + index)) & 0xFF
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 1)) & 0xFF) << 8
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 2)) & 0xFF) << 16
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 3)) & 0xFF) << 24;
  }

  @Override
  public int getIntLE(int index) {
    return Integer.reverseBytes(getInt(index));
  }

  @Override
  public long getLong(int index) {
    return (byte) VAR_HANDLE.get(address.addOffset(baseIndex + index)) & 0xFF
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 1)) & 0xFF) << 8
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 2)) & 0xFF) << 16
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 3)) & 0xFF) << 24
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 4)) & 0xFF) << 32
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 5)) & 0xFF) << 40
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 6)) & 0xFF) << 48
        | ((byte) VAR_HANDLE.get(address.addOffset(baseIndex + index + 7)) & 0xFF) << 56;
  }

  @Override
  public long getLongLE(int index) {
    return Long.reverseBytes(getLong(index));
  }

  @Override
  public Memory getBytes(int index, Memory dst, int dstIndex, int length) {
    if (dst instanceof DirectMemory) {
      final DirectMemory dstMem = (DirectMemory) dst;
      final MemoryAddress dstAddr =
          dstMem.segment.baseAddress().addOffset(dstMem.baseIndex + dstIndex);
      MemoryAddress.copy(address.addOffset(baseIndex + index), dstAddr, length);
    }
    return this;
  }

  @Override
  public Memory getBytes(int index, byte[] dst, int dstIndex, int length) {
    MemorySegment arrSegment = MemorySegment.ofArray(dst);
    MemoryAddress.copy(address.addOffset(baseIndex + index), arrSegment.baseAddress(), length);
    return this;
  }

  @Override
  public Memory setByte(int index, int value) {
    VAR_HANDLE.set(address.addOffset(baseIndex + index), (byte) value);
    return this;
  }

  @Override
  public Memory setShort(int index, int value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray((short) value)) {
      VAR_HANDLE.set(address.addOffset(baseIndex + index + offset++), b);
    }
    return this;
  }

  @Override
  public Memory setShortLE(int index, int value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray((short) value, ByteOrder.LITTLE_ENDIAN)) {
      VAR_HANDLE.set(address.addOffset(baseIndex + index + offset++), b);
    }
    return this;
  }

  @Override
  public Memory setInt(int index, int value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray(value)) {
      VAR_HANDLE.set(address.addOffset(baseIndex + index + offset++), b);
    }
    return this;
  }

  @Override
  public Memory setIntLE(int index, int value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray((short) value, ByteOrder.LITTLE_ENDIAN)) {
      VAR_HANDLE.set(address.addOffset(baseIndex + index + offset++), b);
    }
    return this;
  }

  @Override
  public Memory setLong(int index, long value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray(value)) {
      VAR_HANDLE.set(address.addOffset(baseIndex + index + offset++), b);
    }
    return this;
  }

  @Override
  public Memory setLongLE(int index, long value) {
    int offset = 0;
    for (byte b : Bytes.toByteArray(value, ByteOrder.LITTLE_ENDIAN)) {
      VAR_HANDLE.set(address.addOffset(baseIndex + index + offset++), b);
    }
    return this;
  }

  @Override
  public Memory setBytes(int index, Memory src, int srcIndex, int length) {
    if (src instanceof DirectMemory) {
      final DirectMemory srcMem = (DirectMemory) src;
      final MemoryAddress srcAddr =
          srcMem.segment.baseAddress().addOffset(srcMem.baseIndex + srcIndex);
      MemoryAddress.copy(srcAddr, address.addOffset(baseIndex + index), length);
    }
    return this;
  }

  @Override
  public Memory setBytes(int index, byte[] src, int srcIndex, int length) {
    MemorySegment arrSegment = MemorySegment.ofArray(src);
    MemoryAddress.copy(arrSegment.baseAddress(), address, length);
    return this;
  }

  @Override
  public Memory copy(int index, int length) {
    Validate.notIllegalArgument(
        length <= capacity - (baseIndex + index),
        String.format(
            "copy: %d %d expected(length(%d) <= capacity(%d))",
            index, length, length, capacity - (baseIndex + index)));
    final MemorySegment segment = MemorySegment.allocateNative(length);
    final MemoryAddress address = segment.baseAddress();
    MemoryAddress.copy(address.addOffset(baseIndex + index), address, length);
    return new DirectMemory(segment, 0, length, length, readerIndex(), writerIndex());
  }

  @Override
  public Memory slice(int index, int length) {
    Validate.notIllegalArgument(
        length <= capacity - (baseIndex + index),
        String.format(
            "slice: %d %d expected(length(%d) <= capacity(%d))",
            index, length, length, capacity - (baseIndex + index)));
    Memory duplicated =
        new DirectMemory(
            segment,
            baseIndex + index,
            length,
            maxCapacity,
            readerIndex() - index,
            writerIndex() - index);
    return duplicated;
  }

  @Override
  public Memory duplicate() {
    return new DirectMemory(
        segment, baseIndex, capacity, maxCapacity, readerIndex(), writerIndex());
  }

  @Override
  public ByteBuffer nioBuffer() {
    return segment.asByteBuffer();
  }

  @Override
  public boolean isDirect() {
    return true;
  }

  @Override
  public long memoryAddress() {
    return address.offset();
  }

  @Override
  public void close() throws Exception {
    segment.close();
  }

  @Override
  public void release() {
    segment.close();
  }
}
