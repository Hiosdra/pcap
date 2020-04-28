/** This code is licenced under the GPL version 2. */
package pcap.spring.boot.starter.kotlin

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pcap.api.PcapLive
import pcap.api.PcapLiveOptions
import pcap.api.PcapOfflineOptions
import pcap.api.Pcaps
import pcap.api.handler.EventLoopHandler
import pcap.codec.Packet
import pcap.common.logging.Logger
import pcap.common.logging.LoggerFactory
import pcap.common.net.MacAddress
import pcap.spi.Interface
import pcap.spi.PacketHeader
import pcap.spring.boot.autoconfigure.annotation.EnablePcapPacket
import pcap.spring.boot.autoconfigure.handler.PcapPacketHandler
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/**
 * @author <a href="mailto:contact@ardikars.com">Ardika Rommy Sanjaya</a>
 */
@EnablePcapPacket
@SpringBootApplication
open class PcapKotlinApplication : CommandLineRunner {

    var log = LoggerFactory.getLogger(PcapKotlinApplication::class.java)
    var pcapLiveOptions: PcapLiveOptions
    var pcapOfflineOptions: PcapOfflineOptions
    var defaultInterface: Interface
    var defaultMacAddress: MacAddress

    constructor(
            pcapLiveOptions: PcapLiveOptions,
            pcapOfflineOptions: PcapOfflineOptions,
            defaultInterface: Interface,
            defaultMacAddress: MacAddress) {
        this.pcapLiveOptions = pcapLiveOptions
        this.pcapOfflineOptions = pcapOfflineOptions
        this.defaultInterface = defaultInterface
        this.defaultMacAddress = defaultMacAddress
    }

    override fun run(vararg args: String?) {
        log.info("Running {}", PcapKotlinApplication::class.simpleName)
        log.info("Pcap live properties     : {}", pcapLiveOptions)
        log.info("Pcap offline properties  : {}", pcapOfflineOptions)
        log.info("Pcap default interface   : {}", defaultInterface.name())
        log.info("Pcap default MAC address : {}", defaultMacAddress)
        log.info("Live capture...");

        val pcap = Pcaps.live(PcapLive(defaultInterface))

        pcap.loop(10, EventLoopPcapPacketHandler(), log)
        pcap.close()
    }

    class EventLoopPcapPacketHandler : PcapPacketHandler<Logger>, EventLoopHandler<Logger> {

        var count = AtomicInteger()

        override fun gotPacket(log: Logger, header: PacketHeader?, packet: Packet?) {
            log.info("Packet number {}", count.incrementAndGet())
            log.info("Packet header {}", header)
            log.info("Packet buffer: ")
            packet?.forEach(Consumer { p: Packet -> log.info(p?.toString()) })
        }
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(PcapKotlinApplication::class.java, *args)
}
