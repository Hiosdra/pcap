package pcap.codec;

import pcap.codec.ethernet.Ethernet;
import pcap.codec.ip.Ip4;
import pcap.codec.tcp.Tcp;
import pcap.codec.tcp.TcpFlags;
import pcap.common.memory.Memory;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamFollower {

  private final AtomicInteger number = new AtomicInteger();
  private final Map<BufferKey, Stream> buffer = new WeakHashMap<>();

  public Packet process(Packet packet) {
    if (packet.getPayload() instanceof Ip4 && packet.getPayload().getPayload() instanceof Tcp) {
      Ip4 ip4 = (Ip4) packet.getPayload();
      Ip4.Header ip4Header = ip4.getHeader();
      Tcp tcp = (Tcp) packet.getPayload().getPayload();
      Tcp.Header tcpHeader = tcp.getHeader();
      BufferKey bufferId =
          new BufferKey(
              ip4Header.getSourceAddress(),
              tcpHeader.getSourcePort(),
              ip4Header.getDestinationAddress(),
              tcpHeader.getDestinationPort(),
              tcpHeader.getAcknowledge());
      int length = tcpHeader.getLength();
      int sequence = tcpHeader.getSequence();
      int nextSequence = tcpHeader.getSequence() + length;
      TcpFlags flags = tcpHeader.getFlags();

      if (flags.isSyn() && buffer.containsKey(bufferId)) {
        Memory memory = reassemble(bufferId);
        buffer.remove(bufferId);
        return Ethernet.newPacket(memory);
      }

      BufferData bufferData =
          new BufferData(
              number.getAndIncrement(),
              length,
              sequence,
              nextSequence,
              flags,
              tcp.getPayloadBuffer());

      if (!buffer.containsKey(bufferId)) {
        buffer.put(bufferId, new Stream());
      } else {
        buffer.get(bufferId).add(bufferData);
      }

      if (flags.isFin() || flags.isRst()) {
        Memory memory = reassemble(bufferId);
        buffer.remove(bufferId);
        return Ethernet.newPacket(memory);
      }
    }
    return packet;
  }

  public Memory reassemble(BufferKey bufferId) {
    return buffer.get(bufferId).reassemble(false);
  }
}
