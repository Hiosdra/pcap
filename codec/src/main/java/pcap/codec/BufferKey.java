package pcap.codec;

import pcap.common.net.InetAddress;

import java.util.Objects;

public class BufferKey {

  private InetAddress sourceAddress;
  private int sourcePort;
  private InetAddress destinationAddress;
  private int destinationPort;
  private int acknowledgment;

  public BufferKey(
      InetAddress sourceAddress,
      int sourcePort,
      InetAddress destinationAddress,
      int destinationPort,
      int acknowledgment) {
    this.sourceAddress = sourceAddress;
    this.sourcePort = sourcePort;
    this.destinationAddress = destinationAddress;
    this.destinationPort = destinationPort;
    this.acknowledgment = acknowledgment;
  }

  public InetAddress getSourceAddress() {
    return sourceAddress;
  }

  public int getSourcePort() {
    return sourcePort;
  }

  public InetAddress getDestinationAddress() {
    return destinationAddress;
  }

  public int getDestinationPort() {
    return destinationPort;
  }

  public int getAcknowledgment() {
    return acknowledgment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BufferKey bufferId = (BufferKey) o;
    return sourcePort == bufferId.sourcePort
        && destinationPort == bufferId.destinationPort
        && acknowledgment == bufferId.acknowledgment
        && sourceAddress.equals(bufferId.sourceAddress)
        && destinationAddress.equals(bufferId.destinationAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        sourceAddress, sourcePort, destinationAddress, destinationPort, acknowledgment);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("BufferId{");
    sb.append("sourceAddress=").append(sourceAddress);
    sb.append(", sourcePort=").append(sourcePort);
    sb.append(", destinationAddress=").append(destinationAddress);
    sb.append(", destinationPort=").append(destinationPort);
    sb.append(", acknowledgment=").append(acknowledgment);
    sb.append('}');
    return sb.toString();
  }
}
