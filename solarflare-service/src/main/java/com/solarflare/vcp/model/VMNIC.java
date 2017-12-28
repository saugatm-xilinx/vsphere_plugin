package com.solarflare.vcp.model;

public class VMNIC
{
    private String type = "NIC";
    // key
    private String id;
    // vimnic0
    private String name;

    // pci hardware device
    private String deviceName;
        
    private String vendorName;
    

    private String driverName;
    private String driverVersion;

    private String macAddress;

    private String status;
    private String interfaceName;
    private String portSpeed;
    private String currentMTU;
    private String maxMTU;

    private String pciId;
    private String pciFunction;
    private String pciBusNumber;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

   
    
    public String getDriverName()
    {
        return driverName;
    }

    public void setDriverName(String driverName)
    {
        this.driverName = driverName;
    }

    public String getDriverVersion()
    {
        return driverVersion;
    }

    public void setDriverVersion(String driverVersion)
    {
        this.driverVersion = driverVersion;
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public void setMacAddress(String macAddress)
    {
        this.macAddress = macAddress;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName)
    {
        this.interfaceName = interfaceName;
    }

    public String getPortSpeed()
    {
        return portSpeed;
    }

    public void setPortSpeed(String portSpeed)
    {
        this.portSpeed = portSpeed;
    }

    public String getCurrentMTU()
    {
        return currentMTU;
    }

    public void setCurrentMTU(String currentMTU)
    {
        this.currentMTU = currentMTU;
    }

    public String getMaxMTU()
    {
        return maxMTU;
    }

    public void setMaxMTU(String maxMTU)
    {
        this.maxMTU = maxMTU;
    }

    public String getPciFunction()
    {
        return pciFunction;
    }

    public void setPciFunction(String pciFunction)
    {
        this.pciFunction = pciFunction;
    }

    public String getPciBusNumber()
    {
        return pciBusNumber;
    }

    public void setPciBusNumber(String pciBusNumber)
    {
        this.pciBusNumber = pciBusNumber;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

   

    public String getPciId()
    {
        return pciId;
    }

    public void setPciId(String pciId)
    {
        this.pciId = pciId;
    }

    public String getVendorName()
    {
        return vendorName;
    }

    public void setVendorName(String vendorName)
    {
        this.vendorName = vendorName;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

	@Override
	public String toString() {
		return "VMNIC [type=" + type + ", id=" + id + ", name=" + name + ", deviceName=" + deviceName + ", vendorName="
				+ vendorName + ", driverName=" + driverName + ", driverVersion=" + driverVersion + ", macAddress="
				+ macAddress + ", status=" + status + ", interfaceName=" + interfaceName + ", portSpeed=" + portSpeed
				+ ", currentMTU=" + currentMTU + ", maxMTU=" + maxMTU + ", pciId=" + pciId + ", pciFunction="
				+ pciFunction + ", pciBusNumber=" + pciBusNumber + "]";
	}
    
    

}
