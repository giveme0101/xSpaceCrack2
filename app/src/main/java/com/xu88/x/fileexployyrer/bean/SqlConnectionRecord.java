package com.xu88.x.fileexployyrer.bean;

import java.io.Serializable;

public class SqlConnectionRecord implements Serializable{

    private String name;

    private String userName;
    private String hostName;
    private String portName;
    private String serviceName;
    private String password;
    private String connectType;

    @Override
    public String toString() {
        return "SqlConnectionRecord{" +
                "name='" + name + '\'' +
                ", userName='" + userName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", portName='" + portName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", password='" + password + '\'' +
                ", connectType='" + connectType + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConnectType() {
        return connectType;
    }

    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }

}
