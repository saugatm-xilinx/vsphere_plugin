package com.solarflare.vcp.cim;

public interface CIMConstants
{
    
    String CIM_NAMESPACE = "solarflare/cimv2";
    String WBEMCLIENT_FORMAT = "CIM-XML";
    
    int CIM_PORT = 5989;
    
    
    String SVC_BOOTROM_NAME = "BootROM";
    String SVC_MCFW_NAME = "Firmware";
    
    // SolarFlare CIM Classes
    String SF_SOFTWARE_INSTALLATION_SERVICE = "SF_SoftwareInstallationService";
    String SF_CONTROLLED_BY = "SF_ControlledBy";
    String SF_CONTROLLER_SOFTWARE_IDENTITY = "SF_ControllerSoftwareIdentity";
    
    //Description for Controller and Boot ROM versions
    String DESC_CONTROLLER = "NIC MC Firmware";
    String DESC_BOOT_ROM = "NIC BootROM";
    
    // Different versions for adapter
    String CONTROLLER_VERSION = "ControllerVersion";
    String BOOT_ROM_VERSION = "BootROMVersion";
    String UEFI_ROM_VERSION = "UEFIROMVersion";
    String FIRMARE_VERSION = "FirmareVersion";
    
    String CONTROLLER_FW_IMAGE_PATH = "/firmware/Controller/mcfw.dat";
    String BOOTROM_FW_IMAGE_PATH = "/firmware/BootROM/mcfw.dat";
    String PLUGIN_KEY = "com.solarflare.vcp";
    int FW_IMAGE_PORT = 8888;
    String DEFAULT_VERSION ="0.0.0.0";

}
