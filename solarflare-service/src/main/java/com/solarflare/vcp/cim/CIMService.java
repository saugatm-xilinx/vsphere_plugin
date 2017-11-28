package com.solarflare.vcp.cim;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.cim.CIMInstance;
import javax.cim.CIMObjectPath;
import javax.cim.CIMProperty;
import javax.wbem.CloseableIterator;
import javax.wbem.WBEMException;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.WBEMClient;
import javax.wbem.client.WBEMClientFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vmware.vim25.Extension;
import com.vmware.vim25.ExtensionClientInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;

public class CIMService
{

    private static final Log logger = LogFactory.getLog(CIMService.class);

    public Collection<CIMInstance> getAllInstances(CIMHost cimHost, final String namespace,
            final String classname) throws WBEMException
    {

        WBEMClient client = getClient(cimHost, classname);
        CIMObjectPath objectPath = getHostObjectPath(cimHost, null, classname);

        final List<CIMInstance> results = new LinkedList<CIMInstance>();
        CloseableIterator<CIMInstance> enumeration = client.enumerateInstances(objectPath, true, true, true, null);
        while (enumeration.hasNext())
        {
            results.add(enumeration.next());
        }

        return results;
    }

    public WBEMClient getClient(CIMHost cimHost, String cimObjectName)
    {
        return getClient(cimHost, cimObjectName, null);
    }

    public WBEMClient getClient(CIMHost cimHost, String cimObjectName, CIMProperty<?>[] properties)
    {
        javax.security.auth.Subject subject = createSubjectFromHost(cimHost);

        try
        {
            WBEMClient client = WBEMClientFactory.getClient(CIMConstants.WBEMCLIENT_FORMAT);
            client.initialize(getHostObjectPath(cimHost, properties, cimObjectName), subject, Locale.getAvailableLocales());
            return client;
        }
        catch (WBEMException e)
        {
            logger.error("Failed to initialize WBEMClient!" + e.getMessage());
        }
        return null;
    }

    private CIMObjectPath getHostObjectPath(CIMHost cimHost, CIMProperty<?>[] properties, String cimObjectName)
    {
        try
        {
            URL UrlHost = new URL(cimHost.getUrl());
            return new CIMObjectPath(UrlHost.getProtocol(), UrlHost.getHost(), String.valueOf(UrlHost.getPort()),
                    CIMConstants.CIM_NAMESPACE, cimObjectName, properties);
        }
        catch (MalformedURLException e)
        {
            logger.error("Invalid URL '" + cimHost.getUrl() + "'." + e.getMessage());
        }
        return null;
    }

    private javax.security.auth.Subject createSubjectFromHost(CIMHost cimHost)
    {
        String user = "";
        String password = "";
        javax.security.auth.Subject subject = new javax.security.auth.Subject();

        if (cimHost instanceof CIMHostUser)
        {
            user = ((CIMHostUser) cimHost).getUsername();
            password = ((CIMHostUser) cimHost).getPassword();
        }
        else if (cimHost instanceof CIMHostSession)
        {
            user = ((CIMHostSession) cimHost).getSessionId();
            password = user;
        }

        subject.getPrincipals().add(new UserPrincipal(user));
        subject.getPrivateCredentials().add(new PasswordCredential(password));

        return subject;
    }

    private CIMInstance getFirmwareSoftwareInstallationInstance(Collection<CIMInstance> instances)
    {
        CIMInstance svc_mcfw_inst = null;

        for (CIMInstance inst : instances)
        {

            if (inst.getProperty("Name").getValue().equals(CIMConstants.SVC_MCFW_NAME))
            {
                svc_mcfw_inst = inst;
            }
        }

        return svc_mcfw_inst;
    }

    private CIMInstance getBootROMSoftwareInstallationInstance(Collection<CIMInstance> instances)
    {
        CIMInstance svc_bootrom_inst = null;

        for (CIMInstance inst : instances)
        {

            if (inst.getProperty("Name").getValue().equals(CIMConstants.SVC_BOOTROM_NAME))
            {
                svc_bootrom_inst = inst;
            }
        }

        return svc_bootrom_inst;
    }

    private CIMInstance getEthernatePortInstance(CIMHost cimHost, String deviceId) throws WBEMException
    {
        CIMInstance ethernateInstance = null;
        CIMService cimUtil = new CIMService();
        String cimClass = "SF_EthernetPort";

        // Get SF_EthernetPort instance
        Collection<CIMInstance> instances = cimUtil.getAllInstances(cimHost, CIMConstants.CIM_NAMESPACE, cimClass);
        for (CIMInstance inst : instances)
        {
            String devId = (String) inst.getProperty("DeviceID").getValue();
            String macAddress = (String) inst.getProperty("PermanentAddress").getValue();
            if (devId != null && devId.equals(deviceId))
            {
                ethernateInstance = inst;
            }
        }
        return ethernateInstance;
    }

    @SuppressWarnings("unchecked")
    private CloseableIterator<CIMInstance> getAssociators(WBEMClient client, CIMObjectPath objectPath, String associationClass,
            String resultClass, String role) throws WBEMException
    {

        return client.associators(objectPath, associationClass, resultClass, role, null, true, true, null);

    }

    private CIMInstance getNICCardInstance(CIMHost cimHost, String deviceId) throws WBEMException
    {
        String cimClass = "SF_SoftwareInstallationService";
        WBEMClient client = getClient(cimHost, cimClass);
        // Get EthernatePort Instance
        CIMInstance ethernateInstance = getEthernatePortInstance(cimHost, deviceId);

        // Get SF_ControlledBy instance through association
        CloseableIterator<CIMInstance> inst = getAssociators(client, ethernateInstance.getObjectPath(), "SF_ControlledBy", null,
                "Dependent");
        CIMInstance controlledByInstance = inst.next();

        // Get SF_ControllerSoftwareIdentity instance through association
        inst = getAssociators(client, controlledByInstance.getObjectPath(), "SF_ControllerSoftwareIdentity", null, "Dependent");
        CIMInstance controllerSoftwareIdentityInstance = inst.next();

        // Get SF_CardRealizesController and NICCard instance through association
        inst = getAssociators(client, controlledByInstance.getObjectPath(), "SF_CardRealizesController", "SF_NICCard",
                "Dependent");
        CIMInstance nicCardIntance = inst.next();

        System.out.println(nicCardIntance);

        return nicCardIntance;
    }

    public static void main(String[] args) throws WBEMException
    {
        String url = "https://10.101.10.3:5989/";
        String password = "Ibmx#3750c";
        String user = "root";

        CIMService cimUtil = new CIMService();
        String cimClass = "SF_SoftwareInstallationService";

        CIMHost cimHost = new CIMHostUser(url, user, password);

        // Get SF_SoftwareInstallationService instance
        Collection<CIMInstance> instances = cimUtil.getAllInstances(cimHost, CIMConstants.CIM_NAMESPACE, cimClass);
        for (CIMInstance inst : instances)
        {
            System.out.println(inst);
        }

        // Get Firmware SF_SoftwareInstallationService instance
        CIMInstance svc_mcfw_inst = cimUtil.getFirmwareSoftwareInstallationInstance(instances);

        // Get BootROM SF_SoftwareInstallationService instance
        CIMInstance svc_bootrom_inst = cimUtil.getBootROMSoftwareInstallationInstance(instances);

        // Get EthernatePort Instance for 'vmnic6'
        System.out.println(cimUtil.getEthernatePortInstance(cimHost, "vmnic6"));

        cimUtil.getNICCardInstance(cimHost, "vmnic6");
    }

    public Map<String, String> getAdapterVersions(CIMHost cimHost, String deviceId) throws WBEMException
    {
        logger.info("Getting Adapter Versions for Device Id : " + deviceId);
        String cimClass = CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE;
        Map<String, String> versions = new HashMap<String, String>();
        WBEMClient client = getClient(cimHost, cimClass);
        // Get EthernatePort Instance
        CIMInstance ethernateInstance = getEthernatePortInstance(cimHost, deviceId);

        // Get SF_ControlledBy instance through association
        CloseableIterator<CIMInstance> inst = getAssociators(client, ethernateInstance.getObjectPath(),
                CIMConstants.SF_CONTROLLED_BY, null, "Dependent");
        CIMInstance controlledByInstance = inst.next();

        // Get SF_ControllerSoftwareIdentity instance through association
        inst = getAssociators(client, controlledByInstance.getObjectPath(), CIMConstants.SF_CONTROLLER_SOFTWARE_IDENTITY, null,
                "Dependent");

        while (inst.hasNext())
        {
            CIMInstance instance = inst.next();
            String versionString = instance.getProperty("VersionString").getValue().toString();
            String description = instance.getProperty("Description").getValue().toString();
            if (CIMConstants.DESC_CONTROLLER.equals(description))
            {
                versions.put(CIMConstants.CONTROLLER_VERSION, versionString);
            }
            else if (CIMConstants.DESC_BOOT_ROM.equals(description))
            {
                versions.put(CIMConstants.BOOT_ROM_VERSION, versionString);
            }

            // Adding dummy values for below
            versions.put(CIMConstants.FIRMARE_VERSION, "1.1.1.0");
            versions.put(CIMConstants.UEFI_ROM_VERSION, "1.1.1.0");
        }
        return versions;
    }

    /**
     * The default PasswordCredential will prevent us from using sessionId's that can be over 16
     * characters in length. Instead use inheritance to force the PasswordCredential class to hold
     * values longer than 16 chars.
     * <p>
     * 
     * @see javax.wbem.client.PasswordCredential
     */
    public static class PasswordCredential extends javax.wbem.client.PasswordCredential
    {
        private final String longPassword;

        public PasswordCredential(String userPassword)
        {
            super("fake password"); // the parent class' password is ignored
            longPassword = userPassword;
        }

        @Override
        public char[] getUserPassword()
        {
            return longPassword.toCharArray(); // use our long password instead
        }
    }

    /**
     * builds a base URL to use for CIMObjectPath objects based on the host and connection objects
     * already present in this object on initialization
     *
     * @return a URL to talk to the CIM server on
     */
    public URL cimBaseUrl(String hostname)
    {
        URL url = null;
        try
        {
            url = new URL("https", hostname, CIMConstants.CIM_PORT, "/");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return check(url) ? url : null;
    }

    /**
     * checks a URL to see if we can open a connection to it
     *
     * @param url
     *            - to examine
     * @return true if we can talk to the host
     */
    public boolean check(final URL url)
    {
        boolean valid = false;

        try
        {
            url.openConnection();
            valid = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return valid;
    }

    public String getLatestControllerFWImageVersion(ServiceContent serviceContent,
            VimPortType vimPort) throws MalformedURLException, RuntimeFaultFaultMsg, URISyntaxException
    {
        String versionString = null;
        String urlPath = getPluginURL(serviceContent, vimPort, CIMConstants.PLUGIN_KEY);
        URL pluginURL = new URL(urlPath);

        // URL controllerFWImagePath = new
        // URL(pluginURL.getProtocol(),pluginURL.getHost(),pluginURL.getPort(),CONTROLLER_FW_IMAGE_PATH);
        URL controllerFWImagePath = new URL("http", pluginURL.getHost(), CIMConstants.FW_IMAGE_PORT,
                CIMConstants.CONTROLLER_FW_IMAGE_PATH);
        System.out.println("controllerFWImagePath : " + controllerFWImagePath);

        versionString = getVersionFromBinaryFile(controllerFWImagePath);
        return versionString;
    }

    public String getLatestBootROMFWImageVersion(ServiceContent serviceContent,
            VimPortType vimPort) throws MalformedURLException, RuntimeFaultFaultMsg, URISyntaxException
    {
        String versionString = null;
        String urlPath = getPluginURL(serviceContent, vimPort, CIMConstants.PLUGIN_KEY);
        URL pluginURL = new URL(urlPath);

        URL controllerFWImagePath = new URL("http", pluginURL.getHost(), CIMConstants.FW_IMAGE_PORT,
                CIMConstants.BOOTROM_FW_IMAGE_PATH);
        System.out.println("controllerFWImagePath : " + controllerFWImagePath);

        versionString = getVersionFromBinaryFile(controllerFWImagePath);
        return versionString;
    }

    public String getVersionFromBinaryFile(URL filePath) throws URISyntaxException
    {
        Path path = null;
        byte[] bytes = null;
        String versionString = "";
        
        boolean readComplete = false;
        bytes = readData(filePath, readComplete);
        if (bytes == null)
        {
            versionString = CIMConstants.DEFAULT_VERSION;
        }
        else
        {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int ih_magic = buffer.getInt();
            int ih_version = buffer.getInt();
            int ih_type = buffer.getInt();
            int ih_subtype = buffer.getInt();
            int ih_code_size = buffer.getInt();
            int ih_size = buffer.getInt();

            int ih_controller_version_min = buffer.getInt();

            int ih_controller_version_max = buffer.getInt();

            short ih_code_version_a = buffer.getShort();
            short ih_code_version_b = buffer.getShort();
            short ih_code_version_c = buffer.getShort();
            short ih_code_version_d = buffer.getShort();

            System.out.println("ih_code_version_a : " + ih_code_version_a);
            System.out.println("ih_code_version_b : " + ih_code_version_b);
            System.out.println("ih_code_version_c : " + ih_code_version_c);
            System.out.println("ih_code_version_d : " + ih_code_version_d);

            StringBuffer version = new StringBuffer();
            version.append(ih_code_version_a);
            version.append(".");
            version.append(ih_code_version_b);
            version.append(".");
            version.append(ih_code_version_c);
            version.append(".");
            version.append(ih_code_version_d);

            versionString = version.toString();
        }
        return versionString;
    }

    private byte[] readData(URL toDownload, boolean readComplete)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try
        {
            byte[] chunk = new byte[1000];
            int bytesRead;
            InputStream stream = toDownload.openStream();
            if (readComplete)
            {
                while ((bytesRead = stream.read(chunk)) > 0)
                {
                    outputStream.write(chunk, 0, bytesRead);
                }
            }
            else
            {
                bytesRead = stream.read(chunk);
                outputStream.write(chunk, 0, bytesRead);
            }
        }
        catch (IOException e)
        {
            return null;
        }

        return outputStream.toByteArray();
    }

    private String getPluginURL(ServiceContent serviceContent, VimPortType vimPort, String pluginKey) throws RuntimeFaultFaultMsg,
                                                                                                      MalformedURLException
    {
        String urlPath = "";
        ManagedObjectReference extensionManager = serviceContent.getExtensionManager();
        Extension ext = vimPort.findExtension(extensionManager, pluginKey);
        List<ExtensionClientInfo> extClientInfo = ext.getClient();
        for (ExtensionClientInfo clientInfo : extClientInfo)
        {
            urlPath = clientInfo.getUrl();
        }

        return urlPath;

    }
}
