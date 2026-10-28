package com.example

import com.example.vpn.core.ConfigParsers
import com.example.vpn.core.EngineRegistry
import com.example.vpn.data.VpnSettings
import com.example.vpn.model.VpnProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testWireGuardConfigParser() {
        val conf = """
            [Interface]
            PrivateKey = aaaaaaaaabbbbbbbbccccccccdddddddd
            Address = 10.0.0.2/32
            DNS = 1.1.1.1
            MTU = 1420

            [Peer]
            PublicKey = eeeeffffgggghhhhiiiijjjjkkkkllll
            Endpoint = 198.51.100.1:51820
            AllowedIPs = 0.0.0.0/0
        """.trimIndent()

        val parsed = ConfigParsers.parseWireguard(conf, "Work WG").getOrThrow()
        assertEquals(VpnProtocol.WIREGUARD, parsed.protocol)
        assertEquals("198.51.100.1", parsed.serverHost)
        assertEquals(51820, parsed.serverPort)
        assertEquals("1.1.1.1", parsed.dnsServer)
        assertEquals(1420, parsed.mtu)
    }

    @Test
    fun testOpenVpnConfigParser() {
        val ovpn = """
            client
            dev tun
            proto udp
            remote vpn.example.com 1194
            resolv-retry infinite
            nobind
        """.trimIndent()

        val parsed = ConfigParsers.parseOpenVpn(ovpn, "My OpenVPN").getOrThrow()
        assertEquals(VpnProtocol.OPENVPN, parsed.protocol)
        assertEquals("vpn.example.com", parsed.serverHost)
        assertEquals(1194, parsed.serverPort)
    }

    @Test
    fun testSocks5UriParser() {
        val uri = "socks5://testuser:secretpass@192.0.2.1:1080"
        val parsed = ConfigParsers.parseProxyUri(uri).getOrThrow()
        assertEquals(VpnProtocol.SOCKS5_PROXY, parsed.protocol)
        assertEquals("192.0.2.1", parsed.serverHost)
        assertEquals(1080, parsed.serverPort)
        assertEquals("testuser", parsed.username)
        assertEquals("secretpass", parsed.password)
    }

    @Test
    fun testEffectiveDns() {
        val cf = VpnSettings(dnsPreset = "CLOUDFLARE")
        assertEquals("1.1.1.1", cf.getEffectiveDns())

        val google = VpnSettings(dnsPreset = "GOOGLE")
        assertEquals("8.8.8.8", google.getEffectiveDns())

        val custom = VpnSettings(dnsPreset = "CUSTOM", customDns = "10.0.0.1")
        assertEquals("10.0.0.1", custom.getEffectiveDns())
    }

    @Test
    fun testEngineRegistryHasAllProtocols() {
        val engines = EngineRegistry.getAllEngineInfos()
        assertTrue(engines.isNotEmpty())
        assertNotNull(engines.find { it.protocol == VpnProtocol.ANDROID_PLATFORM })
        assertNotNull(engines.find { it.protocol == VpnProtocol.SOCKS5_PROXY })
        assertNotNull(engines.find { it.protocol == VpnProtocol.WIREGUARD })
        assertNotNull(engines.find { it.protocol == VpnProtocol.OPENVPN })
    }
}
