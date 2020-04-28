/** This code is licenced under the GPL version 2. */
package pcap.spring.boot.starter.java;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pcap.api.PcapLive;
import pcap.api.PcapLiveOptions;
import pcap.api.PcapOfflineOptions;
import pcap.api.Pcaps;
import pcap.api.handler.EventLoopHandler;
import pcap.codec.Packet;
import pcap.common.logging.Logger;
import pcap.common.logging.LoggerFactory;
import pcap.common.net.MacAddress;
import pcap.spi.Interface;
import pcap.spi.PacketHeader;
import pcap.spi.Pcap;
import pcap.spring.boot.autoconfigure.annotation.EnablePcapPacket;
import pcap.spring.boot.autoconfigure.handler.PcapPacketHandler;

/** @author <a href="mailto:contact@ardikars.com">Ardika Rommy Sanjaya</a> */
@EnablePcapPacket
@SpringBootApplication
public class PcapJavaApplication implements CommandLineRunner {

  private final Logger log = LoggerFactory.getLogger(PcapJavaApplication.class);
  private final PcapLiveOptions pcapLiveOptions;
  private final PcapOfflineOptions pcapOfflineOptions;
  private final Interface defaultInterface;
  private final MacAddress defaultMacAddress;

  public PcapJavaApplication(
      PcapLiveOptions pcapLiveOptions,
      PcapOfflineOptions pcapOfflineOptions,
      Interface defaultInterface,
      MacAddress defaultMacAddress) {
    this.pcapLiveOptions = pcapLiveOptions;
    this.pcapOfflineOptions = pcapOfflineOptions;
    this.defaultInterface = defaultInterface;
    this.defaultMacAddress = defaultMacAddress;
  }

  public static void main(String[] args) {
    SpringApplication.run(PcapJavaApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    log.info("Running {}", PcapJavaApplication.class.getSimpleName());
    log.info("Pcap live properties     : {}", pcapLiveOptions);
    log.info("Pcap offline properties  : {}", pcapOfflineOptions);
    log.info("Pcap default interface   : {}", defaultInterface.name());
    log.info("Pcap default MAC address : {}", defaultMacAddress);
    log.info("Live capture...");

    Pcap pcap = Pcaps.live(new PcapLive(defaultInterface));
    pcap.loop(10, new EventLoopPcapPacketHandler(), log);

    pcap.close();
  }

  static class EventLoopPcapPacketHandler
      implements PcapPacketHandler<Logger>, EventLoopHandler<Logger> {

    private final AtomicInteger count = new AtomicInteger();

    @Override
    public void gotPacket(Logger log, PacketHeader header, Packet packet) {
      log.info("Packet number {}", count.incrementAndGet());
      log.info("Packet header {}", header);
      log.info("Packet buffer: ");
      if (packet != null) {
        packet.forEach(p -> log.info(p != null ? p.toString() : ""));
      }
    }
  }
}
