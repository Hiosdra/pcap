package pcap.codec;

import pcap.codec.tcp.TcpFlags;
import pcap.common.memory.Memory;

import java.util.Objects;

public class BufferData {

  private int index; // packet number
  private int length; // seqment length
  private int sequence; // this sequence
  private int nextSequence; // next (wanted) sequence
  private TcpFlags flags;
  private Memory payload; // payload

  public BufferData(
      int index, int length, int sequence, int nextSequence, TcpFlags flags, Memory payload) {
    this.index = index;
    this.length = length;
    this.sequence = sequence;
    this.nextSequence = nextSequence;
    this.flags = flags;
    this.payload = payload;
  }

  public int getIndex() {
    return index;
  }

  public int getLength() {
    return length;
  }

  public int getSequence() {
    return sequence;
  }

  public int getNextSequence() {
    return nextSequence;
  }

  public TcpFlags getFlags() {
    return flags;
  }

  public Memory getPayload() {
    return payload;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BufferData that = (BufferData) o;
    return index == that.index
        && length == that.length
        && sequence == that.sequence
        && nextSequence == that.nextSequence
        && flags.equals(that.flags)
        && payload.equals(that.payload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, length, sequence, nextSequence, flags, payload);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("BufferData{");
    sb.append("index=").append(index);
    sb.append(", length=").append(length);
    sb.append(", sequence=").append(sequence);
    sb.append(", nextSequence=").append(nextSequence);
    sb.append(", flags=").append(flags);
    sb.append(", payload=").append(payload);
    sb.append('}');
    return sb.toString();
  }
}
