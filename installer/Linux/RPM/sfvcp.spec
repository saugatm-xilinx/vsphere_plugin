# ----------------------------------------------------------------------------
# Define self explanatory params for cross unix building capability
# ----------------------------------------------------------------------------
#
%define _binaries_in_noarch_packages_terminate_build   0
%define _binary_filedigest_algorithm  1
%define _binary_payload 1
%define _rollback_transaction_on_failure	1
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Build Metadata for RPM
# ----------------------------------------------------------------------------
#
Name:    sfvcp
Summary: Solarflare vCenter Plugin
Version: SOFTWARE_VERSION_TEMPLATE
Release: RELEASE_VERSION_TEMPLATE
Group:   Applications/System
License: Apache
Source0: %{name}-%{version}-%{release}.tar.gz
BuildRoot:  %_tmppath/%{name}
Provides: %{name}
AutoReqProv: no
%description
Solarflare VCP Registration installer
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Prep Section : Extracts tar package into build dir in RPM dir structure
# ----------------------------------------------------------------------------
#
%prep
%setup -n vcp
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Install Section : Copies build files from build dir to buildroot
# ----------------------------------------------------------------------------
#
%install
cd  %{_builddir}
mkdir -p %{buildroot}
cp -R vcp/* %{buildroot}
# ----------------------------------------------------------------------------
# ----------------------------------------------------------------------------
# Pre Install Section : Prepare host for sfvcp installation
# ----------------------------------------------------------------------------
#
%pre
# ----------------------------------------------------------------------------
# Firewall changes
# ----------------------------------------------------------------------------
#
if ! type "firewall-cmd" > /dev/null 2>&1; then
iptables -I OUTPUT -p tcp --sport 80 -j ACCEPT
iptables -I INPUT -p tcp --dport 80 -j ACCEPT
iptables -I OUTPUT -p tcp --sport 8443 -j ACCEPT
iptables -I INPUT -p tcp --dport 8443 -j ACCEPT
/sbin/iptables-save | awk '!x[$0]++' > /tmp/iptables.conf
/sbin/iptables -F
/sbin/iptables-restore < /tmp/iptables.conf
/sbin/iptables-save > /etc/sysconfig/iptables
/sbin/service iptables restart > /dev/null 2>&1
if [ -f /tmp/iptables.conf ] ; then /bin/rm -f /tmp/iptables.conf ; fi
else
firewall-cmd --permanent --add-port=80/tcp > /dev/null 2>&1
firewall-cmd --permanent --add-port=8443/tcp > /dev/null 2>&1
firewall-cmd --reload > /dev/null 2>&1
fi
# ----------------------------------------------------------------------------
unset CATALINA_HOME CATALINA_BASE JAVA_HOME JRE_HOME
# ----------------------------------------------------------------------------
# Post Install Section : Start sfvcp-tomcat to finalize installation
# ----------------------------------------------------------------------------
#
%post
# ----------------------------------------------------------------------------
# Load JRE env vars
# ----------------------------------------------------------------------------
#
unset CATALINA_HOME CATALINA_BASE JAVA_HOME JRE_HOME
export JRE_HOME=/opt/solarflare/jre
# ----------------------------------------------------------------------------
if [ -d "/usr/local/bin/sfvcp_register" ]; then
  # ----------------------------------------------------------------------------
  # Remove existing file. If exists
  # ----------------------------------------------------------------------------
  #
    rm /usr/local/bin/sfvcp_register
fi
ln -s /usr/bin/sfvcp_register /usr/local/bin > /dev/null 2>&1
chmod a+x /opt/solarflare/tomcat/bin/startup.sh /opt/solarflare/tomcat/bin/shutdown.sh
chmod a+x /opt/solarflare/tomcat/bin/catalina.sh
chmod a+x /usr/bin/sfvcp_register /usr/local/bin/sfvcp_register
# ----------------------------------------------------------------------------
# Start Tomcat
# ----------------------------------------------------------------------------
#
# /opt/solarflare/tomcat/bin/startup.sh > /dev/null 2>&1
# ----------------------------------------------------------------------------
# Exit Message
# ----------------------------------------------------------------------------
#
echo
echo 'Run "sudo sfvcp_register --ip <this-machine-ip-address>" to start plugin registration process'
echo
# ----------------------------------------------------------------------------
# Pre Un-Install Section : Prepare to remove sfvcp
# ----------------------------------------------------------------------------
#
%preun
unset CATALINA_HOME CATALINA_BASE JAVA_HOME JRE_HOME
export JRE_HOME=/opt/solarflare/jre
# ----------------------------------------------------------------------------
if [ -d "/usr/local/bin/sfvcp_register" ]; then
  # ----------------------------------------------------------------------------
  # Remove file. If exists
  # ----------------------------------------------------------------------------
  #
    rm /usr/local/bin/sfvcp_register
fi
# ----------------------------------------------------------------------------
# ----------------------------------------------------------------------------
# Stop Tomcat
# ----------------------------------------------------------------------------
#
/opt/solarflare/tomcat/bin/shutdown.sh > /dev/null 2>&1
# ----------------------------------------------------------------------------
# Post Un-Install Section : Finalize removeal of sfvcp
# ----------------------------------------------------------------------------
#
%postun
rm -rf /opt/solarflare
# ----------------------------------------------------------------------------
# ----------------------------------------------------------------------------
# Install Section : Copy all files required for vcp (controlled by rpmdb)
# config(noreplace) -> Will do a .rpmsave if conf files were changed
# ----------------------------------------------------------------------------
#
%files
/opt/solarflare/jre/*
/opt/solarflare/tomcat/*
/usr/bin/sfvcp_register
# ----------------------------------------------------------------------------
# ----------------------------------------------------------------------------
# Cleanup after building RPM
# ----------------------------------------------------------------------------
#
%clean
rm -rf $buildroot
# ----------------------------------------------------------------------------
# ----------------------------------------------------------------------------
# Changelog
# ----------------------------------------------------------------------------
#
%changelog

* Fri Mar 09 2018 Ashish Koushik <ashish.koushik@msystechnologies.com>
- 2-1
- Bug Fixes - Release

* Wed Mar 07 2018 Ashish Koushik <ashish.koushik@msystechnologies.com>
- 1.98-0.4
- Reform installation procedure

* Tue Mar 06 2018 Ashish Koushik <ashish.koushik@msystechnologies.com>
- 1.98-0.3
- Pre-package Tomcat and JRE

* Fri Jan 12 2018 Ashish Koushik <ashish.koushik@msystechnologies.com>
- 1.97-0.1
- RHEL/Centos Compatible

* Wed Jan 10 2018 Ashish Koushik <ashish.koushik@msystechnologies.com>
- 1.95-0.1
- Initial Build - For CentoOS 7 Only
- Project Initiated
# ----------------------------------------------------------------------------
