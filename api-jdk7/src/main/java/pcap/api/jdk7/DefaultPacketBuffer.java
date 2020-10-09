package pcap.api.jdk7;

import java.util.ArrayList;
import java.util.List;
import pcap.spi.PacketBuffer;

public class DefaultPacketBuffer extends com.sun.jna.Structure implements PacketBuffer {

  static {
    com.sun.jna.Native.register(
        DefaultPacketBuffer.class,
        com.sun.jna.NativeLibrary.getInstance(com.sun.jna.Platform.C_LIBRARY_NAME));
  }

  public com.sun.jna.Pointer buffer;
  protected ByteOrder byteOrder;
  protected long capacity;
  protected long writtenBytes = 0L; // for setCharSequence and writeCharSequence
  protected long readerIndex;
  protected long writerIndex;
  protected long markedReaderIndex;
  protected long markedWriterIndex;
  com.sun.jna.ptr.PointerByReference reference;

  public DefaultPacketBuffer() {
    this.reference = new com.sun.jna.ptr.PointerByReference();
  }

  public DefaultPacketBuffer(int capacity) {
    this(
        new com.sun.jna.Pointer(com.sun.jna.Native.malloc(capacity)),
        ByteOrder.NATIVE,
        capacity,
        0L,
        0L);
  }

  private DefaultPacketBuffer(
      com.sun.jna.Pointer buffer,
      ByteOrder byteOrder,
      long capacity,
      long readerIndex,
      long writerIndex) {
    this.buffer = buffer;
    this.reference = new com.sun.jna.ptr.PointerByReference(buffer);
    this.byteOrder = byteOrder;
    this.capacity = capacity;
    this.readerIndex = readerIndex;
    this.writerIndex = writerIndex;
  }

  static native com.sun.jna.Pointer memcpy(
      com.sun.jna.Pointer dst, com.sun.jna.Pointer src, long n);

  static native void free(com.sun.jna.Pointer p);

  void userReference(DefaultPacketHeader header) {
    if (reference.getValue() != null) {
      useMemory(reference.getValue());
      this.capacity = header.len;
      this.readerIndex = 0;
      this.writerIndex = header.len;
      this.markedReaderIndex = 0;
      this.markedWriterIndex = 0;
      read();
    }
  }

  @Override
  protected List<String> getFieldOrder() {
    List<String> order = new ArrayList<>(1);
    order.add("buffer");
    return order;
  }

  @Override
  public long capacity() {
    return capacity;
  }

  @Override
  public long readerIndex() {
    return readerIndex;
  }

  @Override
  public PacketBuffer readerIndex(long readerIndex) {
    if (readerIndex < 0 || readerIndex > writerIndex) {
      throw new IndexOutOfBoundsException(
          String.format(
              "readerIndex: %d (expected: 0 <= readerIndex <= writerIndex(%d))",
              readerIndex, writerIndex));
    }
    this.readerIndex = readerIndex;
    return this;
  }

  @Override
  public long writerIndex() {
    return writerIndex;
  }

  @Override
  public PacketBuffer writerIndex(long writerIndex) {
    if (writerIndex < readerIndex || writerIndex > capacity) {
      throw new IndexOutOfBoundsException(
          String.format(
              "writerIndex: %d (expected: readerIndex(%d) <= writerIndex(%d) <= capacity(%d))",
              writerIndex, readerIndex, writerIndex, capacity));
    }
    this.writerIndex = writerIndex;
    return this;
  }

  @Override
  public PacketBuffer setIndex(long readerIndex, long writerIndex) {
    if (readerIndex < 0 || readerIndex > writerIndex || writerIndex > capacity) {
      throw new IndexOutOfBoundsException(
          String.format(
              "readerIndex: %d, writerIndex: %d (expected: 0 <= readerIndex <= writerIndex <= capacity(%d))",
              readerIndex, writerIndex, capacity));
    }
    this.readerIndex = readerIndex;
    this.writerIndex = writerIndex;
    return this;
  }

  @Override
  public long readableBytes() {
    return writerIndex - readerIndex;
  }

  @Override
  public long writableBytes() {
    return capacity - writerIndex;
  }

  @Override
  public boolean isReadable() {
    return writerIndex > readerIndex;
  }

  //    @Override
  //    public PacketBuffer clear() {
  //      readerIndex = writerIndex = 0;
  //      return this;
  //    }

  @Override
  public boolean isReadable(long numBytes) {
    return numBytes > 0 && writerIndex - readerIndex >= numBytes;
  }

  @Override
  public boolean isWritable() {
    return capacity > writerIndex;
  }

  @Override
  public boolean isWritable(long numBytes) {
    return numBytes > 0 && capacity - writerIndex >= numBytes;
  }

  @Override
  public PacketBuffer markReaderIndex() {
    markedReaderIndex = readerIndex;
    return this;
  }

  @Override
  public PacketBuffer resetReaderIndex() {
    readerIndex(markedReaderIndex);
    return this;
  }

  @Override
  public PacketBuffer markWriterIndex() {
    markedWriterIndex = writerIndex;
    return this;
  }

  @Override
  public PacketBuffer resetWriterIndex() {
    writerIndex(markedWriterIndex);
    return this;
  }

  @Override
  public PacketBuffer ensureWritable(long minWritableBytes) {
    if (minWritableBytes < 0) {
      throw new IllegalArgumentException(
          String.format("minWritableBytes: %d (expected: >= 0)", minWritableBytes));
    }
    checkWritableBytes(minWritableBytes);
    return this;
  }

  @Override
  public boolean getBoolean(long index) {
    return getByte(index) > 0;
  }

  @Override
  public short getUnsignedByte(long index) {
    return (short) (getByte(index) & 0xFF);
  }

  @Override
  public short getShortRE(long index) {
    return Short.reverseBytes(getShort(index));
  }

  @Override
  public int getUnsignedShort(long index) {
    return getShort(index) & 0xFFFF;
  }

  @Override
  public int getUnsignedShortRE(long index) {
    return Short.reverseBytes((short) getUnsignedShort(index));
  }

  @Override
  public int getIntRE(long index) {
    return Integer.reverseBytes(getInt(index));
  }

  @Override
  public long getUnsignedInt(long index) {
    return getInt(index) & 0xFFFFFFFFL;
  }

  @Override
  public long getUnsignedIntRE(long index) {
    return Integer.reverseBytes((int) getUnsignedInt(index));
  }

  @Override
  public long getLongRE(long index) {
    return Long.reverseBytes(getLong(index));
  }

  @Override
  public float getFloat(long index) {
    return Float.intBitsToFloat(getInt(index));
  }

  @Override
  public float getFloatRE(long index) {
    return Float.intBitsToFloat(getIntRE(index));
  }

  @Override
  public double getDouble(long index) {
    return Double.longBitsToDouble(getLong(index));
  }

  @Override
  public double getDoubleRE(long index) {
    return Double.longBitsToDouble(getLongRE(index));
  }

  @Override
  public PacketBuffer getBytes(long index, PacketBuffer dst) {
    return getBytes(index, dst, dst.writableBytes());
  }

  @Override
  public PacketBuffer getBytes(long index, PacketBuffer dst, long length) {
    getBytes(index, dst, dst.writerIndex(), length);
    dst.writerIndex(dst.writerIndex() + length);
    return this;
  }

  @Override
  public PacketBuffer getBytes(long index, byte[] dst) {
    return getBytes(index, dst, 0, dst.length);
  }

  @Override
  public PacketBuffer setBoolean(long index, boolean value) {
    return setByte(index, value ? 1 : 0);
  }

  @Override
  public PacketBuffer setShortRE(long index, int value) {
    return setShort(index, Short.reverseBytes((short) (value & 0xFFFF)));
  }

  @Override
  public PacketBuffer setIntRE(long index, int value) {
    return setInt(index, Integer.reverseBytes(value));
  }

  @Override
  public PacketBuffer setLongRE(long index, long value) {
    return setLong(index, Long.reverseBytes(value));
  }

  @Override
  public PacketBuffer setFloat(long index, float value) {
    return setInt(index, Float.floatToRawIntBits(value));
  }

  @Override
  public PacketBuffer setFloatRE(long index, float value) {
    return setIntRE(index, Float.floatToRawIntBits(value));
  }

  @Override
  public PacketBuffer setDouble(long index, double value) {
    return setLong(index, Double.doubleToRawLongBits(value));
  }

  @Override
  public PacketBuffer setDoubleRE(long index, double value) {
    return setLongRE(index, Double.doubleToRawLongBits(value));
  }

  @Override
  public PacketBuffer setBytes(long index, PacketBuffer src) {
    return setBytes(index, src, src.readableBytes());
  }

  @Override
  public PacketBuffer setBytes(long index, PacketBuffer src, long length) {
    checkIndex(index, length);
    if (src == null) {
      throw new IllegalArgumentException("src must be not null.");
    }
    if (length > src.readableBytes()) {
      throw new IndexOutOfBoundsException(
          String.format(
              "length(%d) exceeds src.readableBytes(%d) where src is: %s",
              length, src.readableBytes(), src));
    }

    setBytes(index, src, src.readerIndex(), length);
    src.readerIndex(src.readerIndex() + length);
    return this;
  }

  @Override
  public PacketBuffer setBytes(long index, byte[] src) {
    return setBytes(index, src, 0, src.length);
  }

  @Override
  public PacketBuffer setCharSequence(long index, CharSequence seq, Charset charset) {
    this.writtenBytes = StringUtils.setCharSequence(this, index, seq, charset);
    return this;
  }

  @Override
  public boolean readBoolean() {
    return readByte() != 0;
  }

  @Override
  public byte readByte() {
    checkReadableBytes(1);
    long i = readerIndex;
    byte b = getByte(i);
    readerIndex = i + 1;
    return b;
  }

  @Override
  public short readUnsignedByte() {
    return (short) (readByte() & 0xFF);
  }

  @Override
  public short readShort() {
    checkReadableBytes(2);
    short v = getShort(readerIndex);
    readerIndex += 2;
    return v;
  }

  @Override
  public short readShortRE() {
    checkReadableBytes(2);
    short v = getShortRE(readerIndex);
    readerIndex += 2;
    return v;
  }

  @Override
  public int readUnsignedShort() {
    return readShort() & 0xFFFF;
  }

  @Override
  public int readUnsignedShortRE() {
    return readShortRE() & 0xFFFF;
  }

  @Override
  public int readInt() {
    checkReadableBytes(4);
    int v = getInt(readerIndex);
    readerIndex += 4;
    return v;
  }

  @Override
  public int readIntRE() {
    checkReadableBytes(4);
    int v = getIntRE(readerIndex);
    readerIndex += 4;
    return v;
  }

  @Override
  public long readUnsignedInt() {
    return readInt() & 0xFFFFFFFFL;
  }

  @Override
  public long readUnsignedIntRE() {
    return readIntRE() & 0xFFFFFFFFL;
  }

  @Override
  public float readFloat() {
    return Float.intBitsToFloat(readInt());
  }

  @Override
  public float readFloatRE() {
    return Float.intBitsToFloat(readIntRE());
  }

  @Override
  public double readDouble() {
    return Double.longBitsToDouble(readLong());
  }

  @Override
  public double readDoubleRE() {
    return Double.longBitsToDouble(readLongRE());
  }

  @Override
  public long readLong() {
    checkReadableBytes(8);
    long v = getLong(readerIndex);
    readerIndex += 8;
    return v;
  }

  @Override
  public long readLongRE() {
    checkReadableBytes(8);
    long v = getLongRE(readerIndex);
    readerIndex += 8;
    return v;
  }

  @Override
  public PacketBuffer readBytes(PacketBuffer dst) {
    readBytes(dst, dst.writableBytes());
    return this;
  }

  @Override
  public PacketBuffer readBytes(PacketBuffer dst, long length) {
    return readBytes(dst, 0, length);
  }

  @Override
  public PacketBuffer readBytes(PacketBuffer dst, long dstIndex, long length) {
    checkReadableBytes(length);
    getBytes(readerIndex, dst, dstIndex, length);
    readerIndex += length;
    return this;
  }

  @Override
  public PacketBuffer readBytes(byte[] dst) {
    return readBytes(dst, 0, dst.length);
  }

  @Override
  public PacketBuffer readBytes(byte[] dst, long dstIndex, long length) {
    checkReadableBytes(length);
    getBytes(readerIndex, dst, dstIndex, length);
    readerIndex += length;
    return this;
  }

  @Override
  public PacketBuffer skipBytes(long length) {
    checkReadableBytes(length);
    readerIndex += length;
    return this;
  }

  @Override
  public CharSequence readCharSequence(long length, Charset charset) {
    CharSequence sequence = getCharSequence(readerIndex, length, charset);
    readerIndex += length;
    return sequence;
  }

  @Override
  public PacketBuffer writeBoolean(boolean value) {
    return writeByte(value ? 1 : 0);
  }

  @Override
  public PacketBuffer writeByte(int value) {
    checkWritableBytes(1);
    return setByte(writerIndex++, value);
  }

  @Override
  public PacketBuffer writeShort(int value) {
    checkWritableBytes(2);
    setShort(writerIndex, value);
    writerIndex += 2;
    return this;
  }

  @Override
  public PacketBuffer writeShortRE(int value) {
    checkWritableBytes(2);
    setShortRE(writerIndex, value);
    writerIndex += 2;
    return this;
  }

  @Override
  public PacketBuffer writeInt(int value) {
    checkWritableBytes(4);
    setInt(writerIndex, value);
    writerIndex += 4;
    return this;
  }

  @Override
  public PacketBuffer writeIntRE(int value) {
    checkWritableBytes(4);
    setIntRE(writerIndex, value);
    writerIndex += 4;
    return this;
  }

  @Override
  public PacketBuffer writeLong(long value) {
    checkWritableBytes(8);
    setLong(writerIndex, value);
    writerIndex += 8;
    return this;
  }

  @Override
  public PacketBuffer writeLongRE(long value) {
    checkWritableBytes(8);
    setLongRE(writerIndex, value);
    writerIndex += 8;
    return this;
  }

  @Override
  public PacketBuffer writeFloat(float value) {
    return writeInt(Float.floatToRawIntBits(value));
  }

  @Override
  public PacketBuffer writeFloatRE(float value) {
    return writeIntRE(Float.floatToRawIntBits(value));
  }

  @Override
  public PacketBuffer writeDoubleRE(double value) {
    return writeLongRE(Double.doubleToRawLongBits(value));
  }

  @Override
  public PacketBuffer writeDouble(double value) {
    return writeLong(Double.doubleToRawLongBits(value));
  }

  @Override
  public PacketBuffer writeBytes(PacketBuffer src) {
    return writeBytes(src, src.readableBytes());
  }

  @Override
  public PacketBuffer writeBytes(PacketBuffer src, long length) {
    return writeBytes(src, 0, length);
  }

  @Override
  public PacketBuffer writeBytes(PacketBuffer src, long srcIndex, long length) {
    ensureWritable(length);
    setBytes(writerIndex, src, srcIndex, length);
    writerIndex += length;
    return this;
  }

  @Override
  public PacketBuffer writeBytes(byte[] src) {
    return writeBytes(src, 0, src.length);
  }

  @Override
  public PacketBuffer writeBytes(byte[] src, long srcIndex, long length) {
    ensureWritable(length);
    setBytes(writerIndex, src, srcIndex, length);
    writerIndex += length;
    return this;
  }

  @Override
  public PacketBuffer writeCharSequence(CharSequence sequence, Charset charset) {
    setCharSequence(writerIndex, sequence, charset);
    writerIndex += writtenBytes;
    return this;
  }

  @Override
  public ByteOrder byteOrder() {
    return byteOrder;
  }

  @Override
  public PacketBuffer copy() {
    return copy(0, capacity);
  }

  @Override
  public PacketBuffer slice() {
    return slice(readerIndex, readableBytes());
  }

  private void checkWritableBytes(long minWritableBytes) {
    if (minWritableBytes > capacity - writerIndex) {
      throw new IndexOutOfBoundsException(
          String.format(
              "writerIndex(%d) + minWritableBytes(%d) exceeds capacity(%d): %s",
              writerIndex, minWritableBytes, capacity, this));
    }
  }

  private void checkReadableBytes(long minimumReadableBytes) {
    if (minimumReadableBytes < 0) {
      throw new IllegalArgumentException(
          "minimumReadableBytes: " + minimumReadableBytes + " (expected: >= 0)");
    }
    if (readerIndex > writerIndex - minimumReadableBytes) {
      throw new IndexOutOfBoundsException(
          String.format(
              "readerIndex(%d) + length(%d) exceeds writerIndex(%d): %s",
              readerIndex, minimumReadableBytes, writerIndex, this));
    }
  }

  void checkIndex(long index, long fieldLength) {
    if (isOutOfBounds(index, fieldLength, capacity)) {
      throw new IndexOutOfBoundsException(
          String.format(
              "index: %d, length: %d (expected: range(0, %d))", index, fieldLength, capacity));
    }
  }

  boolean isOutOfBounds(long index, long length, long capacity) {
    return (index | length | (index + length) | (capacity - (index + length))) < 0;
  }

  @Override
  public String toString() {
    String format =
        "[%s] => [capacity: %d, readerIndex: %d, writerIndex: %d, markedReaderIndex: %d, markedWriterIndex: %d]";
    return String.format(
        format,
        getClass().getSimpleName(),
        capacity,
        readerIndex,
        writerIndex,
        markedReaderIndex,
        markedWriterIndex);
  }

  /** Implementation */
  @Override
  public byte getByte(long index) {
    // check buffer overflow
    checkIndex(index, Byte.BYTES);
    return buffer.getByte(index);
  }

  @Override
  public short getShort(long index) {
    // check buffer overflow
    checkIndex(index, Short.BYTES);
    return buffer.getShort(index);
  }

  @Override
  public int getInt(long index) {
    // check buffer overflow
    checkIndex(index, Integer.BYTES);
    return buffer.getInt(index);
  }

  @Override
  public long getLong(long index) {
    // check buffer overflow
    checkIndex(index, Long.BYTES);
    return buffer.getLong(index);
  }

  @Override
  public PacketBuffer getBytes(long index, PacketBuffer dst, long dstIndex, long length) {
    // check buffer overflow
    checkIndex(index, length);
    if (isOutOfBounds(dstIndex, length, dst.capacity())) {
      throw new IndexOutOfBoundsException(
          String.format(
              "dstIdx: %d, length: %d (expected: dstIdx(%d) <= length(%d)))",
              dstIndex, length, dstIndex, length));
    }
    com.sun.jna.Pointer dstPtr = ((DefaultPacketBuffer) dst).buffer.share(dstIndex, length);
    memcpy(dstPtr, buffer.share(index, length), length);
    return this;
  }

  @Override
  public PacketBuffer getBytes(long index, byte[] dst, long dstIndex, long length) {
    // check buffer overflow
    checkIndex(index, length);
    if (isOutOfBounds(dstIndex, length, dst.length)) {
      throw new IndexOutOfBoundsException(
          String.format(
              "dstIdx: %d, length: %d (expected: dstIdx(%d) <= length(%d)))",
              dstIndex, length, dstIndex, length));
    }
    buffer.read(index, dst, (int) dstIndex, (int) length);
    return this;
  }

  @Override
  public CharSequence getCharSequence(long index, long length, Charset charset) {
    byte[] bytes = new byte[(int) length & 0x7FFFFFFF];
    getBytes(index, bytes);
    return new String(bytes, java.nio.charset.Charset.forName(charset.name()));
  }

  @Override
  public PacketBuffer setByte(long index, int value) {
    // check buffer overflow
    checkIndex(index, Byte.BYTES);
    buffer.setByte(index, (byte) value);
    return this;
  }

  @Override
  public PacketBuffer setShort(long index, int value) {
    // check buffer overflow
    checkIndex(index, Short.BYTES);
    buffer.setShort(index, (short) value);
    return this;
  }

  @Override
  public PacketBuffer setInt(long index, int value) {
    // check buffer overflow
    checkIndex(index, Integer.BYTES);
    buffer.setInt(index, value);
    return this;
  }

  @Override
  public PacketBuffer setLong(long index, long value) {
    // check buffer overflow
    checkIndex(index, Long.BYTES);
    buffer.setLong(index, value);
    return this;
  }

  @Override
  public PacketBuffer setBytes(long index, PacketBuffer src, long srcIndex, long length) {
    // check buffer overflow
    checkIndex(index, length);
    if (isOutOfBounds(srcIndex, length, src.capacity())) {
      throw new IndexOutOfBoundsException(
          String.format(
              "srcIdx: %d, length: %d (expected: srcIdx(%d) <= length(%d)))",
              srcIndex, length, srcIndex, length));
    }
    com.sun.jna.Pointer srcPtr = ((DefaultPacketBuffer) src).buffer.share(srcIndex, length);
    memcpy(buffer.share(index, length), srcPtr, length);
    return this;
  }

  @Override
  public PacketBuffer setBytes(long index, byte[] src, long srcIndex, long length) {
    // check buffer overflow
    checkIndex(index, length);
    if (isOutOfBounds(srcIndex, length, src.length)) {
      throw new IndexOutOfBoundsException(
          String.format(
              "srcIdx: %d, length: %d (expected: srcIdx(%d) <= length(%d)))",
              srcIndex, length, srcIndex, length));
    }
    buffer.write(index, src, (int) srcIndex, (int) length);
    return this;
  }

  @Override
  public PacketBuffer copy(long index, long length) {
    // check buffer overflow
    checkIndex(index, length);
    com.sun.jna.Pointer newBuf = new com.sun.jna.Pointer(com.sun.jna.Native.malloc(length));
    memcpy(newBuf, buffer.share(index, length), length);
    return new DefaultPacketBuffer(newBuf, byteOrder, length, 0L, 0L);
  }

  @Override
  public PacketBuffer slice(long index, long length) {
    // check buffer overflow
    checkIndex(index, length);
    return new Sliced(this, index, length);
  }

  @Override
  public PacketBuffer duplicate() {
    return new DefaultPacketBuffer(buffer, byteOrder, capacity, readerIndex, writerIndex);
  }

  @Override
  public PacketBuffer byteOrder(ByteOrder byteOrder) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean release() {
    if (buffer != null) {
      free(buffer);
      return true;
    }
    return false;
  }

  static class Sliced extends DefaultPacketBuffer implements PacketBuffer.Sliced {

    private final DefaultPacketBuffer prev;

    Sliced(DefaultPacketBuffer prev, long index, long length) {
      super(
          prev.buffer.share(index),
          prev.byteOrder,
          length,
          prev.readerIndex - index < 0 ? 0 : prev.readerIndex - index,
          prev.writerIndex - index < 0 ? 0 : prev.writerIndex - index);
      this.prev = prev;
    }

    @Override
    public DefaultPacketBuffer unSlice() {
      return prev;
    }
  }
}
