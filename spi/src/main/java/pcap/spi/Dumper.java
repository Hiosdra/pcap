/** This code is licenced under the GPL version 2. */
package pcap.spi;

/** @author <a href="mailto:contact@ardikars.com">Ardika Rommy Sanjaya</a> */
public interface Dumper {

  void dump(PacketHeader header, PacketBuffer buffer);

  long position();

  void flush();

  void close();
}
