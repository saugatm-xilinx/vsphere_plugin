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
    String SF_PROVIDER_LOG = "SF_ProviderLog";
    String  SF_ETHERNET_PORT = "SF_EthernetPort";
    //Description for Controller and Boot ROM versions
    String DESC_CONTROLLER = "NIC MC Firmware";
    String DESC_BOOT_ROM = "NIC BootROM";
    
    // Different versions for adapter
    String CONTROLLER_VERSION = "ControllerVersion";
    String BOOT_ROM_VERSION = "BootROMVersion";
    String UEFI_ROM_VERSION = "UEFIROMVersion";
    String FIRMARE_VERSION = "FirmareVersion";
    
    String CONTROLLER_FW_IMAGE_PATH = "/firmware/v6.2.5.1000/mcfw.dat";
    String BOOTROM_FW_IMAGE_PATH = "/firmware/BootROM/mcfw.dat";
    String METADATA_PATH = "/firmware/FirmwareMetadata.json";
    String PLUGIN_KEY = "com.solarflare.vcp";
    int FW_IMAGE_PORT = 8888;
    String DEFAULT_VERSION ="0.0.0.0";

    String SF_LOG_MANAGES_RECORD = "SF_LogManagesRecord";
    //SolarFlare CIM methods
    String GET_FW_IMAGE_NAME =  "GetRequiredFwImageName";
    String START_FW_IMAGE_SEND = "StartFwImageSend";
    String SEND_FW_IMAGE_DATA = "SendFwImageData";
    String GET_LOCAL_FW_IMAGE_VERSION = "GetLocalFwImageVersion";
    String INSTALL_FROM_URI = "InstallFromURI";
    String REMOVE_FW_IMAGE = "RemoveFwImage";
    
    //SolarFlare CIM methods params
    String TARGET = "Target";
    String NAME = "Name";
    String TYPE = "Type";
    String SUB_TYPE = "Subtype";
    String CURRENT_VERSION = "CurrentVersion";
    String FILE_NAME = "FileName";
    String BASE64STR = "Base64Str";
    String URI = "URI";
    String INSTALL_OPTIONS = "InstallOptions";
    String INSTALL_OPTIONS_VALUES = "InstallOptionsValues";
}
