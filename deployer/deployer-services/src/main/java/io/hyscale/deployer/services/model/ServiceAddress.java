package io.hyscale.deployer.services.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.constants.ToolConstants;

/**
 * Cluster Service address
 *
 */
public class ServiceAddress {

    private String serviceIP;
    private List<Integer> ports;

    public String getServiceIP() {
	return serviceIP;
    }

    public void setServiceIP(String serviceIP) {
	this.serviceIP = serviceIP;
    }

    public List<Integer> getPorts() {
	return ports;
    }

    public void setPorts(List<Integer> ports) {
	this.ports = ports;
    }

    /**
     * returns 
     * null if serviceIP is null or empty
     * else String representing
     * serviceIP:port1, serviceIP:port2
     */
    @Override
    public String toString() {
	if (StringUtils.isBlank(serviceIP)) {
	    return null;
	}

	StringBuilder address = new StringBuilder(serviceIP);
	if (ports != null && !ports.isEmpty()) {
	    address.append(ToolConstants.COLON);
	    address.append(ports.stream().map(Object::toString)
		    .collect(Collectors.joining(ToolConstants.COMMA + serviceIP + ToolConstants.COLON)));
	}

	return address.toString();
    }
}