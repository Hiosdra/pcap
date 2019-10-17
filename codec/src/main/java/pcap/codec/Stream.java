package pcap.codec;

import pcap.common.memory.Memory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Stream {

  private final Set<BufferData> data = new HashSet<>();

  void add(BufferData bufferData) {
    this.data.add(bufferData);
  }

  public Memory reassemble(boolean checked) {
    List<Memory> memories =
        data.stream()
            .sorted(Comparator.comparing(BufferData::getSequence))
            .map(bufferData -> bufferData.getPayload())
            .collect(Collectors.toList());
    Memory memory = pcap.common.memory.Stream.stream(memories.toArray(new Memory[0]), checked);
    return memory.setIndex(0, memory.capacity());
  }
}
