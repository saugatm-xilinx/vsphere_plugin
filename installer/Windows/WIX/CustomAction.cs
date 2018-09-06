using System;
using Microsoft.Deployment.WindowsInstaller;
using System.Windows.Forms;
using System.Net;
using System.IO;
using Microsoft.Win32;
using System.Net.NetworkInformation;

namespace CustomActions
{
    public class CustomActions
    {
        static String Http_port_str = "80";
        static String Https_port_str = "8443";
        enum HostType
        {
            DNS_TYPE,
            IP_TYPE
        };
        private static bool CheckPort(int portNumber)
        {
            bool isAvailable = true;
            IPGlobalProperties ipGlobalProperties = IPGlobalProperties.GetIPGlobalProperties();
            TcpConnectionInformation[] tcpConnInfoArray = ipGlobalProperties.GetActiveTcpConnections();
            IPEndPoint[] ipEndPointArray = ipGlobalProperties.GetActiveTcpListeners();
            foreach (TcpConnectionInformation tcpi in tcpConnInfoArray)
            {
                if (tcpi.LocalEndPoint.Port == portNumber)
                {
                    isAvailable = false;
                    break;
                }
            }
            if (isAvailable)
            {
                foreach (IPEndPoint ip in ipEndPointArray)
                {
                    if (ip.Port == portNumber)
                    {
                        isAvailable = false;
                        break;
                    }
                }
            }
            return isAvailable;
        }

        private static bool validateHostNameIPAddress(string name, out string hostVal, out bool pingFail, out HostType hostType)
        {
            var host = Dns.GetHostEntry(Dns.GetHostName());
            hostVal = host.HostName;
            pingFail = false;
            hostType = HostType.DNS_TYPE;

            UriHostNameType uritype = Uri.CheckHostName(name);
            switch (uritype)
            {
                case UriHostNameType.Basic:
                case UriHostNameType.Unknown:
                    break;
                case UriHostNameType.Dns:
                    {
                        if (string.Equals(host.HostName, name, StringComparison.OrdinalIgnoreCase))
                            return true;
                        else if (name.Length > host.HostName.Length)
                        {
                            if (name.StartsWith(host.HostName,StringComparison.OrdinalIgnoreCase) && (name[host.HostName.Length] == '.'))
                            {
                                try
                                {
                                    Ping pinger = new Ping();
                                    PingReply reply = pinger.Send(name);
                                    if (reply.Status == IPStatus.Success)
                                        return true;
                                    else
                                        pingFail = true;
                                }
                                catch (PingException pe)
                                {
                                    // PingException Happens in case of Unknown Host
                                    pingFail = true;
                                }
                            }
                        }
                    }
                    break;
                case UriHostNameType.IPv4:
                case UriHostNameType.IPv6:
                    {
                        hostType = HostType.IP_TYPE;
                        foreach (var ip in host.AddressList)
                        {
                            if (ip.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork ||
                                ip.AddressFamily == System.Net.Sockets.AddressFamily.InterNetworkV6)
                                if (ip.ToString() == name)
                                    return true;
                        }
                    }
                    break;
            }
            return false;
        }

        [CustomAction]
        public static ActionResult Get_DefaultBrowser(Session session)
        {
            string urlAssociation = @"Software\Microsoft\Windows\Shell\Associations\UrlAssociations\http";
            string browserPathKey = @"$BROWSER$\shell\open\command";

            RegistryKey userChoiceKey = null;

            try
            {
                //Read default browser path from userChoiceKey
                userChoiceKey = Registry.CurrentUser.OpenSubKey(urlAssociation + @"\UserChoice", false);

                //If user choice was not found, try machine default
                if (userChoiceKey == null)
                {
                    //Read default browser path from Win XP registry key
                    var browserKey = Registry.ClassesRoot.OpenSubKey(@"HTTP\shell\open\command", false);

                    //If browser path wasn’t found, try Win Vista (and newer) registry key
                    if (browserKey == null)
                    {
                        browserKey = Registry.CurrentUser.OpenSubKey(urlAssociation, false);
                    }
                    return ActionResult.Success;
                }
                else
                {
                    // user defined browser choice was found
                    string progId = (userChoiceKey.GetValue("ProgId").ToString());
                    userChoiceKey.Close();

                    // now look up the path of the executable
                    string concreteBrowserKey = browserPathKey.Replace("$BROWSER$", progId);
                    var kp = Registry.ClassesRoot.OpenSubKey(concreteBrowserKey, false);
                    string defaultBrowserPath = (kp.GetValue("").ToString());
                    defaultBrowserPath = defaultBrowserPath.Substring(1, defaultBrowserPath.LastIndexOf(".exe") + 3);
                    session.Log("Default browser path : " + defaultBrowserPath);
                    session["BROWSER"] = defaultBrowserPath;
                    return ActionResult.Success;
                }
            }
            catch(Exception ex)
            {
                session.Log("Exception while getting default browser path" + ex.Message);
                return ActionResult.Failure;
            }
        }


        [CustomAction]
        public static ActionResult Get_HostName(Session session)
        {
            // Get the host entry of current machine
            string hostName = Dns.GetHostEntry("").HostName;

            // Assign fully qualified name to the property "HOSTNAME_IPADDRESS"
            session["HOSTNAME_IPADDRESS"] = hostName;
            session["HTTP_PORT"] = Http_port_str;
            session["HTTP_SECURE_PORT"] = Https_port_str;
            session.Log("Got the Hostname OR FQDN :: " + hostName);
            return ActionResult.Success;
        }


        [CustomAction]
        public static ActionResult Validate_HostName_IPAddress(Session session)
        {
            string HostName_IPAddress = session["HOSTNAME_IPADDRESS"];
            string HTTP_PORT = session["HTTP_PORT"];
            string HTTPS_PORT = session["HTTP_SECURE_PORT"];
            int http_port_val = 0;
            int https_port_val = 0;
            string hostVal ;
            bool pingFail = false;
            try
            {
                // check if "HostName_IPAddress" is an empty string
                if (HostName_IPAddress == String.Empty)
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "Please enter fully qualified Hostname or IP Address",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("This is not a valid Hostname or IP Address");
                    return ActionResult.Success;
                }
                else if (HostName_IPAddress.Contains(" "))
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "No Spaces allowed in Hostname or IP Address",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("No Spaces allowed in Hostname or IP Address");
                    return ActionResult.Success;
                }
                else if (!validateHostNameIPAddress(HostName_IPAddress, out hostVal, out pingFail, out HostType hostType))
                {
                    string statusMsg;
                    string hostTypeStr = "";
                    if (hostType == HostType.DNS_TYPE)
                        hostTypeStr = "host name";
                    else
                        hostTypeStr = "IP Address";

                    if (pingFail)
                        statusMsg = "Ping Failed for "+  hostTypeStr +" " + HostName_IPAddress;
                    else
                        statusMsg = HostName_IPAddress + " is not a valid " +  hostTypeStr + " for " + hostVal;
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        statusMsg,
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("Hostname/IP Address invalid. Not found on host.");
                    return ActionResult.Success;
                }
                else if (HTTP_PORT == String.Empty)
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "Please enter HTTP Port number",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("This is not a valid HTTP Port number");
                    return ActionResult.Success;
                }
                else if (HTTPS_PORT == String.Empty)
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "Please enter HTTPS port number",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("This is not a valid HTTPS Port number");
                    return ActionResult.Success;
                }
                else if (HTTP_PORT.Contains(" "))
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "HTTP Port number cannot have spaces",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("HTTP Port Number cannot have spaces");
                    return ActionResult.Success;
                }
                else if (HTTPS_PORT.Contains(" "))
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "HTTPS Port number cannot have spaces",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("HTTPS Port Number cannot have spaces");
                    return ActionResult.Success;
                }
                else if (!int.TryParse(HTTP_PORT, out http_port_val))
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "Please enter valid HTTP port number",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("Integer value of HTTP Port cannot be determined");
                    return ActionResult.Success;
                }
                else if (!int.TryParse(HTTPS_PORT, out https_port_val))
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "Please enter valid HTTPS port number",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("Integer value of HTTPS Port cannot be determined");
                    return ActionResult.Success;
                }
                if (http_port_val == https_port_val)
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "HTTP & HTTPS port numbers cannot be same. Please enter again",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("HTTP & HTTPS port numbers are same");
                    return ActionResult.Success;
                }
                if (http_port_val <= 0 || http_port_val > 65535  || https_port_val <= 0 || https_port_val > 65535)
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "Port numbers must be in the range of 1 to 65535 ",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("Incorrect port number");
                    return ActionResult.Success;
                }
                if (!CheckPort(http_port_val))
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "HTTP Port in use. Please enter another HTTP port number",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("HTTP port in use");
                    return ActionResult.Success;
                }
                if (!CheckPort(https_port_val))
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "HTTPS Port in use. Please enter another HTTPS port number",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("HTTPS port in use");
                    return ActionResult.Success;
                }
                session["HOSTNAME_IPADDRESS_VALID"] = "1";
                session.Log("This is a valid Hostname or IP Address and HTTP/HTTPS port numbers");
                return ActionResult.Success;
            }
            catch (Exception ex)
            {
                session.Log("Exception while validating Hostname or IP Address" + ex.Message);
                return ActionResult.Failure;
            }
        }


        [CustomAction]
        public static ActionResult Modify_PropertiesFile(Session session)
        {
            string InstallFolder = session.CustomActionData["INSTALLDIR"];
            string file = InstallFolder + "Tomcat_Server\\webapps\\plugin-registration\\WEB-INF\\registerPlugin.properties";

            for (int i = 1; i <= 60; i++)
            {
                if (File.Exists(file))
                {
                    session.Log("registerPlugin.properties file found");
                    break;
                }
                System.Threading.Thread.Sleep(500);
            }

            try
            {
                if (File.Exists(file))
                {
                    string rows = File.ReadAllText(file);
                    string HostName_IPAddress = session.CustomActionData["HOSTNAME_IPADDRESS"];
                    string Https_Port = session.CustomActionData["HTTPS_PORT"];
                    // Replacing 'localhost' with the given 'HostName_IPAddress'
                    rows = rows.Replace("localhost", HostName_IPAddress);
                    rows = rows.Replace("pluginPort=8443", "pluginPort=" + Https_Port);
                    File.WriteAllText(file, rows);
                    session.Log("registerPlugin.properties file updated successfully");
                    return ActionResult.Success;
                }
                else
                {
                    session.Log("registerPlugin.properties file doesn't exist");
                    return ActionResult.Failure;
                }
            }
            catch (Exception ex)
            {
                session.Log("Exception while modifying properties file : " + ex.Message);
                return ActionResult.Failure;
            }
        }

        [CustomAction]
        public static ActionResult Modify_ServerConfigFile(Session session)
        {
            string http_port = session.CustomActionData["HTTP_PORT"];
            string https_port = session.CustomActionData["HTTPS_PORT"];

            if (http_port.Equals(Http_port_str) && https_port.Equals(Https_port_str))
            {
                //Nothing to do
                session.Log("No Change in default port numbers");
                return ActionResult.Success;

            }
            string InstallFolder = session.CustomActionData["INSTALLDIR"];
            string file = InstallFolder + "Tomcat_Server\\conf\\server.xml";

            for (int i = 1; i <= 60; i++)
            {
                if (File.Exists(file))
                {
                    session.Log("Tomcat\\conf\\server.xml file found");
                    break;
                }
                System.Threading.Thread.Sleep(500);
            }

            try
            {
                if (File.Exists(file))
                {
                    string rows = File.ReadAllText(file);
                    rows = rows.Replace("Connector port=\"80\"", "Connector port=\""+http_port+"\"" );
                    rows = rows.Replace("redirectPort=\"8443\"", "redirectPort =\""+https_port+"\"");
                    rows = rows.Replace("Connector port=\"8443\"", "Connector port=\"" + https_port + "\"");
                    File.WriteAllText(file, rows);
                    session.Log("Tomcat\\conf\\server.xml file updated successfully");
                    return ActionResult.Success;
                }
                else
                {
                    session.Log("Tomcat\\conf\\server.xml file doesn't exist");
                    return ActionResult.Failure;
                }
            }
            catch (Exception ex)
            {
                session.Log("Exception while modifying properties file : " + ex.Message);
                return ActionResult.Failure;
            }
        }
    }
}
