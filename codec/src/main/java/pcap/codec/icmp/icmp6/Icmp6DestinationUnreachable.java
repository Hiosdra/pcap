/** This code is licenced under the GPL version 2. */
package pcap.codec.icmp.icmp6;

import pcap.codec.icmp.Icmp;
import pcap.codec.icmp.Icmp6;
import pcap.common.annotation.Inclubating;

/** @author <a href="mailto:contact@ardikars.com">Ardika Rommy Sanjaya</a> */
@Inclubating
public class Icmp6DestinationUnreachable extends Icmp.IcmpTypeAndCode {

  /**
   * A Destination Unreachable message SHOULD be generated by a router, or by the IPv6 layer in the
   * originating node, in response to a packet that cannot be delivered to its destination address
   * for reasons other than congestion. (An Icmp6InverseNeighborDiscoverySolicitation message MUST
   * NOT be generated if a packet is dropped due to congestion.)
   *
   * <p>If the reason for the failure to deliver is lack of a matching entry in the forwarding
   * node's routing table, the Code field is set to 0.
   */
  public static final Icmp6DestinationUnreachable NO_ROUTE_TO_DESTINATION =
      new Icmp6DestinationUnreachable((byte) 0, "No route to destination");

  public static final Icmp6DestinationUnreachable
      COMMUNICATION_WITH_DESTINATION_ADMINIS_TRATIVELY_PROHIBITED =
          new Icmp6DestinationUnreachable(
              (byte) 1, "Communication with destination administratively prohibited");

  public static final Icmp6DestinationUnreachable BEYOND_SCOPE_OF_SOURCE_ADDRESS =
      new Icmp6DestinationUnreachable((byte) 2, "Beyond scope of source address");

  public static final Icmp6DestinationUnreachable ADDRESS_UNREACHABLE =
      new Icmp6DestinationUnreachable((byte) 3, "Address unreachable");

  public static final Icmp6DestinationUnreachable PORT_UNREACHABLE =
      new Icmp6DestinationUnreachable((byte) 4, "Address unreachable");

  public static final Icmp6DestinationUnreachable SOURCE_ADDRESS_FAILED =
      new Icmp6DestinationUnreachable((byte) 5, "Source address failed ingress/egress policy");

  public static final Icmp6DestinationUnreachable REJECT_ROUTE_TO_DESTINATION =
      new Icmp6DestinationUnreachable((byte) 6, "Reject route to destination");

  public static final Icmp6DestinationUnreachable ERROR_IN_SOURCE_ROUTING_HEADER =
      new Icmp6DestinationUnreachable((byte) 7, "Error in Source Routing HeaderAbstract");

  public Icmp6DestinationUnreachable(Byte code, String name) {
    super((byte) 1, code, name);
  }

  /**
   * Add new {@link Icmp6DestinationUnreachable} to registry.
   *
   * @param code icmp type code.
   * @param name icmp type name.
   * @return returns {@link Icmp6DestinationUnreachable}.
   */
  public static Icmp6DestinationUnreachable register(Byte code, String name) {
    return new Icmp6DestinationUnreachable(code, name);
  }

  @Override
  public String toString() {
    return super.toString();
  }

  static {
    Icmp6.ICMP6_REGISTRY.add(NO_ROUTE_TO_DESTINATION);
    Icmp6.ICMP6_REGISTRY.add(COMMUNICATION_WITH_DESTINATION_ADMINIS_TRATIVELY_PROHIBITED);
    Icmp6.ICMP6_REGISTRY.add(BEYOND_SCOPE_OF_SOURCE_ADDRESS);
    Icmp6.ICMP6_REGISTRY.add(ADDRESS_UNREACHABLE);
    Icmp6.ICMP6_REGISTRY.add(PORT_UNREACHABLE);
    Icmp6.ICMP6_REGISTRY.add(SOURCE_ADDRESS_FAILED);
    Icmp6.ICMP6_REGISTRY.add(REJECT_ROUTE_TO_DESTINATION);
    Icmp6.ICMP6_REGISTRY.add(ERROR_IN_SOURCE_ROUTING_HEADER);
  }
}
