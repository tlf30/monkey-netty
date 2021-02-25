package io.tlf.monkeynetty.client;

import io.netty.handler.logging.LogLevel;
import io.tlf.monkeynetty.NetworkProtocol;
import io.tlf.monkeynetty.client.MessageCacheMode;

import java.io.File;

public class NetworkClientSettings {

    private String service = "monkey-netty";
    private int tcpPort = 13900;
    private int udpPort = 13900;
    private String address = "localhost";
    private boolean ssl = false;
    private boolean sslSelfSigned = false;
    private File sslCertFile = null;
    private File sslKeyFile = null;
    private NetworkProtocol[] protocols = {NetworkProtocol.TCP, NetworkProtocol.UDP};
    private MessageCacheMode cacheMode = MessageCacheMode.TCP_ENABLED;

    /**
     * Connection timeout in milliseconds used when client is unable connect to server
     * Note: Currently it do not apply when server is "off"
     */
    private int connectionTimeout = 10000;

    public void load(File file) {
        //TODO
    }

    public void load(String service) {
        //TODO
    }

    public void save() {
        //TODO
    }

    public void save(File file) {
        //TODO
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isSslSelfSigned() {
        return sslSelfSigned;
    }

    public void setSslSelfSigned(boolean sslSelfSigned) {
        this.sslSelfSigned = sslSelfSigned;
    }

    public File getSslCertFile() {
        return sslCertFile;
    }

    public void setSslCertFile(File sslCertFile) {
        this.sslCertFile = sslCertFile;
    }

    public File getSslKeyFile() {
        return sslKeyFile;
    }

    public void setSslKeyFile(File sslKeyFile) {
        this.sslKeyFile = sslKeyFile;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public NetworkProtocol[] getProtocols() {
        return protocols;
    }

    public void setProtocols(NetworkProtocol[] protocols) {
        this.protocols = protocols;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return The current message cache mode.
     */
    public MessageCacheMode getCacheMode() {
        return cacheMode;
    }

    /**
     * Sets the message cache mode. By default the mode is <code>MessageCacheMode.ENABLE_TCP</code>
     * See <code>MessageCacheMode</code> for more information about the supported mode options.
     *
     * @param cacheMode The desired message cache mode.
     */
    public void setCacheMode(MessageCacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    /**
     * @return The timeout in milliseconds for creating a new connection
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Set the timeout duration in milliseconds for creating a new connection from the client to the server.
     * This does not effect the read/write timeouts for messages after the connection has been established.
     *
     * @param connectionTimeout The timeout in milliseconds for creating a new connection.
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
