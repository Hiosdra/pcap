package pcap.jdk7.internal;

import com.sun.jna.Pointer;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import pcap.spi.*;
import pcap.spi.exception.ErrorException;
import pcap.spi.exception.TimeoutException;
import pcap.spi.exception.error.BreakException;
import pcap.spi.exception.error.NotActivatedException;

public class DefaultPcap implements Pcap {

  private static final String READ_LOCK_FAIL = "Failed to lock (READ_LOCK)";
  private static final String WRITE_LOCK_FAIL = "Failed to lock (WRITE_LOCK)";

  final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
  final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
  final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

  final Pointer pointer;
  final int netmask;
  final DefaultStatistics statistics = new DefaultStatistics();

  DefaultPcap(Pointer pointer, int netmask) {
    this.pointer = pointer;
    this.netmask = netmask;
  }

  @Override
  public DefaultDumper dumpOpen(String file) throws ErrorException {
    if (StringUtils.blank(file)) {
      throw new IllegalArgumentException("file: null (expected: file != null && notBlank(file))");
    }
    Pointer dumper;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      dumper = NativeMappings.pcap_dump_open(pointer, file);
      nullCheck(dumper);
    } finally {
      readLock.unlock();
    }
    return new DefaultDumper(dumper);
  }

  @Override
  public DefaultDumper dumpOpenAppend(String file) throws ErrorException {
    if (StringUtils.blank(file)) {
      throw new IllegalArgumentException("file: null (expected: file != null && notBlank(file))");
    }
    Pointer dumper;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      dumper = NativeMappings.pcap_dump_open_append(pointer, file);
      nullCheck(dumper);
    } finally {
      readLock.unlock();
    }
    return new DefaultDumper(dumper);
  }

  @Override
  public void setFilter(String filter, boolean optimize) throws ErrorException {
    if (StringUtils.blank(filter)) {
      throw new IllegalArgumentException(
          "filter: null (expected: filter != null && notBlank(filter))");
    }
    int rc;
    NativeMappings.bpf_program fp = new NativeMappings.bpf_program();
    if (!writeLock.tryLock()) {
      throw new RuntimeException(WRITE_LOCK_FAIL);
    }
    try {
      rc = NativeMappings.pcap_compile(pointer, fp, filter, optimize ? 1 : 0, netmask);
      compileCheck(rc, fp);
    } finally {
      writeLock.unlock();
    }
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      rc = NativeMappings.pcap_setfilter(pointer, fp);
      filterCheck(rc, fp);
      NativeMappings.pcap_freecode(fp);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public <T> void loop(int count, final PacketHandler<T> handler, final T args)
      throws BreakException, ErrorException {
    if (handler == null) {
      throw new IllegalArgumentException("handler: null (expected: handler != null)");
    }
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      int rc =
          NativeMappings.pcap_loop(
              pointer,
              count,
              new NativeMappings.pcap_handler() {
                @Override
                public void got_packet(Pointer user, Pointer header, Pointer packet) {
                  DefaultPacketHeader packetHeader = new DefaultPacketHeader(header);
                  DefaultPacketBuffer packetBuffer =
                      new DefaultPacketBuffer(
                          packet,
                          PacketBuffer.ByteOrder.NATIVE,
                          packetHeader.captureLength(),
                          0,
                          packetHeader.captureLength());
                  handler.gotPacket(args, packetHeader, packetBuffer);
                }
              },
              Pointer.NULL);
      loopCheck(rc);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public PacketBuffer next(PacketHeader header) {
    if (header == null) {
      throw new IllegalArgumentException("header: null (expected: header != null)");
    }
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    PacketBuffer buffer;
    try {
      final DefaultPacketHeader[] packetHeader = new DefaultPacketHeader[1];
      packetHeader[0] = (DefaultPacketHeader) header;
      final DefaultPacketBuffer[] packetBuffer = new DefaultPacketBuffer[1];
      int rc =
          NativeMappings.pcap_dispatch(
              pointer,
              1,
              new NativeMappings.pcap_handler() {
                @Override
                public void got_packet(Pointer user, Pointer header, Pointer packet) {
                  Pointer hdrPtr = packetHeader[0].getPointer();
                  hdrPtr.setNativeLong(
                      DefaultTimestamp.TV_SEC_OFFSET,
                      header.getNativeLong(DefaultTimestamp.TV_SEC_OFFSET));
                  hdrPtr.setNativeLong(
                      DefaultTimestamp.TV_USEC_OFFSET,
                      header.getNativeLong(DefaultTimestamp.TV_USEC_OFFSET));
                  hdrPtr.setInt(
                      DefaultPacketHeader.LEN_OFFSET,
                      header.getInt(DefaultPacketHeader.LEN_OFFSET));
                  hdrPtr.setInt(
                      DefaultPacketHeader.CAPLEN_OFFSET,
                      header.getInt(DefaultPacketHeader.CAPLEN_OFFSET));
                  packetBuffer[0] =
                      new DefaultPacketBuffer(
                          packet,
                          PacketBuffer.ByteOrder.NATIVE,
                          packetHeader[0].captureLength(),
                          0,
                          packetHeader[0].captureLength());
                }
              },
              Pointer.NULL);
      if (rc > 0) {
        buffer = packetBuffer[0];
      } else {
        buffer = null;
      }
    } finally {
      readLock.unlock();
    }
    return buffer;
  }

  @Override
  public void nextEx(PacketHeader packetHeader, PacketBuffer packetBuffer)
      throws BreakException, ErrorException, TimeoutException {
    if (packetHeader == null) {
      throw new IllegalArgumentException("header: null (expected: header != null)");
    }
    if (packetBuffer == null) {
      throw new IllegalArgumentException("buffer: null (expected: buffer != null)");
    }
    DefaultPacketHeader header = (DefaultPacketHeader) packetHeader;
    DefaultPacketBuffer buffer = (DefaultPacketBuffer) packetBuffer;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      int rc = NativeMappings.pcap_next_ex(pointer, header.reference, buffer.reference);
      nextExCheck(rc, header, buffer);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public <T> void dispatch(int count, final PacketHandler<T> handler, final T args)
      throws BreakException, ErrorException, TimeoutException {
    if (handler == null) {
      throw new IllegalArgumentException("handler: null (expected: handler != null)");
    }
    int rc;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      rc =
          NativeMappings.pcap_dispatch(
              pointer,
              count,
              new NativeMappings.pcap_handler() {
                @Override
                public void got_packet(Pointer user, Pointer header, Pointer packet) {
                  DefaultPacketHeader packetHeader = new DefaultPacketHeader(header);
                  DefaultPacketBuffer packetBuffer =
                      new DefaultPacketBuffer(
                          packet,
                          PacketBuffer.ByteOrder.NATIVE,
                          packetHeader.captureLength(),
                          0,
                          packetHeader.captureLength());
                  handler.gotPacket(args, packetHeader, packetBuffer);
                }
              },
              Pointer.NULL);
      dispatchCheck(rc);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public Statistics stats() throws ErrorException {
    int rc;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      rc = NativeMappings.pcap_stats(pointer, statistics.getPointer());
      statsCheck(rc);
    } finally {
      readLock.unlock();
    }
    return statistics;
  }

  @Override
  public void breakLoop() {
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      NativeMappings.pcap_breakloop(pointer);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void sendPacket(PacketBuffer directBuffer) throws ErrorException {
    if (directBuffer == null) {
      throw new IllegalArgumentException("buffer: null (expected: buffer != null)");
    }
    if (directBuffer.capacity() <= 0) {
      throw new IllegalArgumentException(
          String.format(
              "buffer.capacity: %d (expected: buffer.capacity(%d) > 0)",
              directBuffer.capacity(), directBuffer.capacity()));
    }
    DefaultPacketBuffer buffer = (DefaultPacketBuffer) directBuffer;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      int rc =
          NativeMappings.pcap_sendpacket(pointer, buffer.buffer, (int) directBuffer.writerIndex());
      sendCheck(rc);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void setDirection(Direction direction) throws ErrorException {
    if (direction == null) {
      throw new IllegalArgumentException("direction: null (expected: direction != null)");
    }
    int result = 0;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      if (Direction.PCAP_D_IN == direction) {
        result = NativeMappings.pcap_setdirection(pointer, 1);
      } else if (Direction.PCAP_D_OUT == direction) {
        result = NativeMappings.pcap_setdirection(pointer, 2);
      } else {
        result = NativeMappings.pcap_setdirection(pointer, 0);
      }
      directionCheck(result);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public boolean isSwapped() throws NotActivatedException {
    int rc;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      rc = NativeMappings.pcap_is_swapped(pointer);
    } finally {
      readLock.unlock();
    }
    return swappedCheck(rc);
  }

  @Override
  public Timestamp.Precision getTimestampPrecision() {
    return timestampPrecision(NativeMappings.pcap_get_tstamp_precision(pointer));
  }

  @Override
  public int majorVersion() {
    int rc;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      rc = NativeMappings.pcap_major_version(pointer);
    } finally {
      readLock.unlock();
    }
    return rc;
  }

  @Override
  public int minorVersion() {
    int rc;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      rc = NativeMappings.pcap_minor_version(pointer);
    } finally {
      readLock.unlock();
    }
    return rc;
  }

  @Override
  public int snapshot() {
    int rc;
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      rc = NativeMappings.pcap_snapshot(pointer);
    } finally {
      readLock.unlock();
    }
    return rc;
  }

  @Override
  public boolean getNonBlock() throws ErrorException {
    int rc;
    NativeMappings.ErrorBuffer errbuf = new NativeMappings.ErrorBuffer();
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      rc = NativeMappings.pcap_getnonblock(pointer, errbuf);
      getNonBlockCheck(rc);
    } finally {
      readLock.unlock();
    }
    return rc == NativeMappings.TRUE;
  }

  @Override
  public void setNonBlock(boolean blocking) throws ErrorException {
    int rc;
    NativeMappings.ErrorBuffer errbuf = new NativeMappings.ErrorBuffer();
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      rc = NativeMappings.pcap_setnonblock(pointer, blocking ? 1 : 0, errbuf);
      setNonBlockCheck(rc);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void close() {
    if (!readLock.tryLock()) {
      throw new RuntimeException(READ_LOCK_FAIL);
    }
    try {
      NativeMappings.pcap_close(pointer);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public <T> T allocate(Class<T> cls) throws IllegalArgumentException {
    if (cls == null) {
      throw new IllegalArgumentException(
          "type: null (expected: type is PacketHeader.class or PacketBuffer.class)");
    }
    if (cls.isAssignableFrom(PacketHeader.class)) {
      return (T) new DefaultPacketHeader();
    } else if (cls.isAssignableFrom(PacketBuffer.class)) {
      return (T) new DefaultPacketBuffer();
    }
    throw new IllegalArgumentException("Class: " + cls + " is unsupported.");
  }

  static Timestamp.Precision timestampPrecision(int rc) {
    if (Timestamp.Precision.NANO.value() == rc) {
      return Timestamp.Precision.NANO;
    } else {
      return Timestamp.Precision.MICRO;
    }
  }

  void nullCheck(Pointer newPointer) throws ErrorException {
    if (newPointer == null) {
      throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
    }
  }

  void compileCheck(int rc, NativeMappings.bpf_program fp) throws ErrorException {
    if (rc != NativeMappings.OK) {
      NativeMappings.pcap_freecode(fp);
      throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
    }
  }

  void filterCheck(int rc, NativeMappings.bpf_program fp) throws ErrorException {
    if (rc != NativeMappings.OK) {
      NativeMappings.pcap_freecode(fp);
      throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
    }
  }

  void loopCheck(int rc) throws BreakException, ErrorException {
    if (rc == 0) {
      return;
    } else if (rc == -2) {
      throw new BreakException("Break loop.");
    } else {
      throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
    }
  }

  void dispatchCheck(int rc) throws ErrorException, BreakException {
    if (rc < 0) {
      if (rc == -1) {
        throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
      } else if (rc == -2) {
        throw new BreakException("Break loop.");
      } else {
        throw new ErrorException("Generic error");
      }
    }
  }

  void nextExCheck(int rc, DefaultPacketHeader header, DefaultPacketBuffer buffer)
      throws BreakException, ErrorException, TimeoutException {
    if (rc == 0) {
      throw new TimeoutException("Read packet timeout.");
    } else if (rc == 1) {
      header.useReferece();
      header.read();
      buffer.userReference(header);
    } else {
      if (rc == -2) {
        throw new BreakException("Break loop.");
      } else {
        throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
      }
    }
  }

  void statsCheck(int rc) throws ErrorException {
    if (rc < 0) {
      throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
    }
  }

  void sendCheck(int rc) throws ErrorException {
    if (rc < 0) {
      throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
    }
  }

  void directionCheck(int result) throws ErrorException {
    if (result < 0) {
      throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
    }
  }

  void getNonBlockCheck(int rc) throws ErrorException {
    if (rc < 0) {
      throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
    }
  }

  void setNonBlockCheck(int rc) throws ErrorException {
    if (rc < 0) {
      throw new ErrorException(NativeMappings.pcap_geterr(pointer).getString(0));
    }
  }

  boolean swappedCheck(int swapped) throws NotActivatedException {
    if (swapped == NativeMappings.TRUE) {
      return true;
    } else if (swapped == NativeMappings.FALSE) {
      return false;
    } else {
      if (swapped == -3) {
        throw new NotActivatedException("Not activated.");
      } else {
        return false;
      }
    }
  }
}
