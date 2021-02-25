package io.tlf.monkeynetty.server;

import io.tlf.monkeynetty.NetworkProtocol;

import java.io.File;

public class NetworkServerSettings {

    private String service = "monkey-netty";
    private int tcpPort = 13900;
    private int udpPort = 13900;
    private boolean ssl = false;
    private boolean sslSelfSigned = false;
    private File sslCertFile = null;
    private File sslKeyFile = null;
    private int maxConnections = 10;
    private boolean blocking = false;
    private NetworkProtocol[] protocols = {NetworkProtocol.TCP, NetworkProtocol.UDP};

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

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
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
}
