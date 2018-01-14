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
Summary: RPM for Solarflare VCP
Version: SOFTWARE_VERSION_TEMPLATE
Release: RELEASE_VERSION_TEMPLATE
Group:   Applications/System
License: Apache
Source0: %{name}-%{version}-%{release}.tar.gz
BuildRoot:  %_tmppath/%{name}
Requires: tomcat
Provides: %{name}
%description
Solarflare VCP Registration plugin
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
# Identify init type
# ----------------------------------------------------------------------------
#
init_type=$(cat -e /sbin/init | grep -waoe 'upstart\|sysvinit\|systemd' | head -n1)
# ----------------------------------------------------------------------------
case "$init_type" in
        upstart)
        ;;

        systemd)
        systemctl stop tomcat
        ;;

        sysvinit)
        ;;

        *)
        echo "Unsupported init sytem: $init_type"
esac
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Firewall changes
# ----------------------------------------------------------------------------
#
firewall-cmd --permanent --add-port=8080/tcp > /dev/null 2>&1
firewall-cmd --reload > /dev/null 2>&1
/sbin/iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080 > /dev/null 2>&1

# ----------------------------------------------------------------------------


# ----------------------------------------------------------------------------
# Post Install Section : Start sfvcp-tomcat to finalize installation
# ----------------------------------------------------------------------------
#
%post
# ----------------------------------------------------------------------------
# Identify init type
# ----------------------------------------------------------------------------
#
init_type=$(cat -e /sbin/init | grep -waoe 'upstart\|sysvinit\|systemd' | head -n1)
# ----------------------------------------------------------------------------
case "$init_type" in
        upstart)
        ;;

        systemd)
        systemctl start tomcat
        ;;

        sysvinit)
        ;;

        *)
        echo "Unsupported init sytem: $init_type"
esac
# ----------------------------------------------------------------------------
if [ -d "/usr/local/bin/sfvcp_register" ]; then
  # ----------------------------------------------------------------------------
  # Remove existing file. If exists
  # ----------------------------------------------------------------------------
  #
    rm /usr/local/bin/sfvcp_register
fi

ln -s /usr/bin/sfvcp_register /usr/local/bin

echo
echo 'Run "sudo sfvcp_register --ip <machine-ip-address>" to start plugin registration process'
echo

# ----------------------------------------------------------------------------
# Pre Un-Install Section : Prepare to remove sfvcp
# ----------------------------------------------------------------------------
#
%preun
# ----------------------------------------------------------------------------
# Identify init type
# ----------------------------------------------------------------------------
#
init_type=$(cat -e /sbin/init | grep -waoe 'upstart\|sysvinit\|systemd' | head -n1)
# ----------------------------------------------------------------------------
case "$init_type" in
        upstart)
        ;;

        systemd)
        systemctl stop tomcat
        ;;

        sysvinit)
        ;;

        *)
        echo "Unsupported init sytem: $init_type"
esac
# ----------------------------------------------------------------------------
if [ -d "/usr/local/bin/sfvcp_register" ]; then
  # ----------------------------------------------------------------------------
  # Remove existing file. If exists
  # ----------------------------------------------------------------------------
  #
    rm /usr/local/bin/sfvcp_register
fi
# ----------------------------------------------------------------------------


# ----------------------------------------------------------------------------
# Post Un-Install Section : Finalize removeal of sfvcp
# ----------------------------------------------------------------------------
#
%postun
# ----------------------------------------------------------------------------
# Identify init type
# ----------------------------------------------------------------------------
#
init_type=$(cat -e /sbin/init | grep -waoe 'upstart\|sysvinit\|systemd' | head -n1)
# ----------------------------------------------------------------------------
rm -rf /var/lib/tomcat/webapps/plugin-registration

case "$init_type" in
        upstart)
        ;;

        systemd)
        systemctl start tomcat
        ;;

        sysvinit)
        ;;

        *)
        echo "Unsupported init sytem: $init_type"
esac
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Install Section : Copy all files required for vcp (controlled by rpmdb)
# config(noreplace) -> Will do a .rpmsave if conf files were changed
# ----------------------------------------------------------------------------
#
%files
/var/lib/tomcat/webapps/*
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

* Fri Jan 12 2018 Ashish Koushik <ashish.koushik@msystechnologies.com>
- 1.97-0.1
- RHEL/Centos Compatible

* Wed Jan 10 2018 Ashish Koushik <ashish.koushik@msystechnologies.com>
- 1.95-0.1
- Initial Build - For CentoOS 7 Only
- Project Initiated
# ----------------------------------------------------------------------------
