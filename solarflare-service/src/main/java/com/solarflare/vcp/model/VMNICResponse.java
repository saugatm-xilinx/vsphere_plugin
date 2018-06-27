package com.solarflare.vcp.model;


public class VMNICResponse {

    private String type = "VMNICResponse";
    private String deviceId;
    private String subSystemDeviceId;
    private String vendorId;
    private String subSystemVendorId;
    private String driverVersion;
    private String driverName;
    private String macAddress;
    private String linkStatus;
    private String portSpeed;
    private String interfaceName;
    private String pciFunction;
    private String pciBusNumber;

    private static String hexFormatter(String variable, int hexLength)
    {
        int i = 0;
        String hexSymbol = "0x";
	for (i=variable.length(); i<hexLength;i++)
	    hexSymbol = hexSymbol.concat("0");
	return hexSymbol.concat(variable);
    }

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = hexFormatter(deviceId,4);
	}
	public String getSubSystemDeviceId() {
		return subSystemDeviceId;
	}
	public void setSubSystemDeviceId(String subSystemDeviceId) {
		this.subSystemDeviceId = hexFormatter(subSystemDeviceId,4);
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = hexFormatter(vendorId,4);
	}
	public String getSubSystemVendorId() {
		return subSystemVendorId;
	}
	public void setSubSystemVendorId(String subSystemVendorId) {
		this.subSystemVendorId = hexFormatter(subSystemVendorId,4);
	}
	public String getDriverVersion() {
		return driverVersion;
	}
	public void setDriverVersion(String driverVersion) {
		this.driverVersion = driverVersion;
	}
	public String getDriverName() {
		return driverName;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public String getLinkStatus() {
		return linkStatus;
	}
	public void setLinkStatus(String linkStatus) {
		this.linkStatus = linkStatus;
	}
	public String getPortSpeed() {
		return portSpeed;
	}
	public void setPortSpeed(String portSpeed) {
		this.portSpeed = portSpeed;
	}
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	public String getPciFunction() {
		return pciFunction;
	}
	public void setPciFunction(String pciFunction) {
		this.pciFunction = pciFunction;
	}
	public String getPciBusNumber() {
		return pciBusNumber;
	}
	public void setPciBusNumber(String pciBusNumber) {
		this.pciBusNumber = pciBusNumber;
	}
	@Override
	public String toString() {
		return "VMNICResponse [type=" + type + ", deviceId=" + deviceId + ", subSystemDeviceId=" + subSystemDeviceId
				+ ", vendorId=" + vendorId + ", subSystemVendorId=" + subSystemVendorId + ", driverVersion="
				+ driverVersion + ", driverName=" + driverName + ", macAddress=" + macAddress + ", linkStatus="
				+ linkStatus + ", portSpeed=" + portSpeed + ", interfaceName=" + interfaceName + ", pciFunction="
				+ pciFunction + ", pciBusNumber=" + pciBusNumber + "]";
	}
    
    
    
}
