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
            session.Log("Got the Hostname OR FQDN :: " + hostName);
            return ActionResult.Success;          
        }


        [CustomAction]
        public static ActionResult Validate_HostName_IPAddress(Session session)
        {
            string HostName_IPAddress = session["HOSTNAME_IPADDRESS"];

            try
            {
                // check if "HostName_IPAddress" is an empty string
                if (HostName_IPAddress == String.Empty)
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = string.Empty;
                    MessageBox.Show(
                        "Please enter Hostname or IP Address",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("This is not a valid Hostname or IP Address");
                    return ActionResult.Success;
                }
                else
                {
                    session["HOSTNAME_IPADDRESS_VALID"] = "1";
                    session.Log("This is a valid Hostname or IP Address");
                    return ActionResult.Success;
                }
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

            for (int i = 1; i <= 60; i = i + 1)
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

                    // Replacing 'localhost' with the given 'HostName_IPAddress'
                    rows = rows.Replace("localhost", HostName_IPAddress);
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
    }
}
