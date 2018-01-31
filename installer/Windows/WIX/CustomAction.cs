using System;
using Microsoft.Deployment.WindowsInstaller;
using System.Windows.Forms;
using System.Net;
using System.Text.RegularExpressions;
using System.IO;
using System.Security.AccessControl;
using System.Security.Principal;

namespace CustomActions
{
    public class CustomActions
    {
        static int index = 1;

        public static void FillComboBox(Session session, string text, string value)
        {
            Microsoft.Deployment.WindowsInstaller.View view = session.Database.OpenView(
                "SELECT * FROM ComboBox WHERE Property = 'IP_ADDRESS'"
                );
            view.Execute();
            Record record = session.Database.CreateRecord(4);
            record.SetString(1, "IP_ADDRESS");
            record.SetInteger(2, index);
            record.SetString(3, value);
            record.SetString(4, text);
            view.Modify(ViewModifyMode.InsertTemporary, record);
            view.Close();
            index++;
        }


        [CustomAction]
        public static ActionResult Get_IPAddress(Session session)
        {
            Microsoft.Deployment.WindowsInstaller.View view = session.Database.OpenView(
                "DELETE FROM ComboBox WHERE ComboBox.Property = 'IP_ADDRESS'"
                );
            view.Execute();

            //First get the host name of local machine.
            String strHostName = Dns.GetHostName();

            // Then using host name, get the IP address list..
            IPHostEntry ipEntry = Dns.GetHostByName(strHostName);
            IPAddress[] addr = ipEntry.AddressList;

            // Fill ComboBox with the IP Addresses
            foreach (IPAddress address in addr)
                FillComboBox(session, address.ToString(), address.ToString());

            session.Log("Got the IP address list successfully.\n");
            return ActionResult.Success;
        }


        [CustomAction]
        public static ActionResult Validate_IPAddress(Session session)
        {           
            string ipAddress = session["IP_ADDRESS"];
            var Pattern = new string[]
            {
                "^",                                                                        // Start of string
                @"(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.",         // Between 0 and 255 and "."
                @"(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.",
                @"(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.",
                @"(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])",
                "$"                                                                         // End of string
            };

            // Match 'ipAddress' with the 'Pattern'
            bool ValidateIP = Regex.IsMatch(
                                ipAddress,
                                string.Join(string.Empty, Pattern)
                                );
            try
            {
                if (ValidateIP)
                {
                    session["IP_VALID"] = "1";
                    session.Log("This is a valid ip address");
                    return ActionResult.Success;
                }
                else
                {
                    session["IP_VALID"] = string.Empty;
                    MessageBox.Show(
                        "Please select IP Address",
                        "Setup",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    session.Log("This is not a valid ip address");
                    return ActionResult.Success;
                }
            }
            catch (Exception ex)
            {
                session.Log("Exception while validating ip address" + ex.Message);
                return ActionResult.Failure;
            }
        }


        [CustomAction]
        public static ActionResult Modify_PropertiesFile(Session session)
        {
            string InstallFolder = session.CustomActionData["INSTALLDIR"];
            string file = InstallFolder + "Tomcat_Server\\webapps\\plugin-registration\\WEB-INF\\registerPlugin.properties";
            string Dirpath = InstallFolder + "Tomcat_Server\\webapps\\plugin-registration\\WEB-INF";

            // Adding access permission to the current directory (Dirpath)           
            DirectoryInfo dInfo = new DirectoryInfo(Dirpath);
            DirectorySecurity dSecurity = dInfo.GetAccessControl();
            dSecurity.AddAccessRule(new FileSystemAccessRule(new SecurityIdentifier(WellKnownSidType.WorldSid, null),
                                                     FileSystemRights.FullControl,
                                                     InheritanceFlags.ObjectInherit | InheritanceFlags.ContainerInherit,
                                                     PropagationFlags.NoPropagateInherit, AccessControlType.Allow)
                                                     );
            dInfo.SetAccessControl(dSecurity);

            try
            {
                if (File.Exists(file))
                {
                    string rows = File.ReadAllText(file);
                    string ip = session.CustomActionData["IP_ADDRESS"];

                    // Replacing 'localhost' with the given 'ip'
                    rows = rows.Replace("localhost", ip);
                    File.WriteAllText(file, rows);
                    session.Log("registerPlugin properties file updated successfully");
                }
                else
                    session.Log("Properties file doesn't exist");

                return ActionResult.Success;
            }
            catch (Exception ex)
            {
                session.Log("Exception while modifying properties file : " + ex.Message);
                return ActionResult.Failure;
            }
        }
    }
}
