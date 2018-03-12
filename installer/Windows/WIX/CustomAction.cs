using System;
using Microsoft.Deployment.WindowsInstaller;
using System.Windows.Forms;
using System.Net;
using System.IO;

namespace CustomActions
{
    public class CustomActions
    {

        [CustomAction]
        public static ActionResult Get_HostName(Session session)
        {
            //First get the hostname of local machine.
            String strHostName = Dns.GetHostName();

            // Assign Hostname to the property ""HOSTNAME_IPADDRESS"
            session["HOSTNAME_IPADDRESS"] = strHostName;
            session.Log("Got the Hostname successfully.\n");
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

            for (int i = 1; i <= 30; i = i + 1)
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
