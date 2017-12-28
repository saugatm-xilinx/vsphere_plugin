package com.solarflare.vcp.vim.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vmware.vim25.HostPciDevice;
import com.vmware.vim25.PhysicalNic;
import com.vmware.vim25.SoftwarePackage;

public class SfVimServiceHelper {
	public static final String VENDOR_SOLARFLARE = "Solarflare";
	public static final String SLF = "SLF";
	public static final String CIM = "cim";
	public static final String SFC = "sfc";

	public static int getAdapterCount(List<HostPciDevice> sfDevices) {
		Set<String> adapters = new HashSet<>();
		for (HostPciDevice sfDevice : sfDevices) {
			adapters.add(Short.toString(sfDevice.getDeviceId()));
		}
		return adapters.size();
	}

	public static String getDriverVersion(List<SoftwarePackage> softwarePackages) {
		for (SoftwarePackage softwarePackage : softwarePackages) {
			List<String> vendors = Arrays.asList(new String[] { SLF, VENDOR_SOLARFLARE });
			if (vendors.contains(softwarePackage.getVendor()) && softwarePackage.getName().contains("net")) {
				return softwarePackage.getVersion();
			}

		}
		return null;
	}

	public static String getCimProviderVersion(List<SoftwarePackage> softwarePackages) {
		for (SoftwarePackage softwarePackage : softwarePackages) {
			List<String> vendors = Arrays.asList(new String[] { SLF, VENDOR_SOLARFLARE });
			if (vendors.contains(softwarePackage.getVendor()) && softwarePackage.getName().contains(CIM)) {
				return softwarePackage.getVersion();
			}
		}
		return null;
	}

	/**
	 * Filter Physical nics using driver name
	 * 
	 * @param
	 * @return
	 */
	public static List<PhysicalNic> getSfPhysicalNic(List<PhysicalNic> physicalNics) {
		List<PhysicalNic> sfNics = new ArrayList<PhysicalNic>();
		if (physicalNics != null && !physicalNics.isEmpty())
			for (PhysicalNic physicalNic : physicalNics) {
				if (SFC.equalsIgnoreCase(physicalNic.getDriver()))
					sfNics.add(physicalNic);
			}
		return sfNics;
	}

	/**
	 * Filter devices using vendor name
	 * 
	 * @param pciDevices
	 * @return
	 */
	public static List<HostPciDevice> getSfPciDevice(List<HostPciDevice> pciDevices) {
		List<HostPciDevice> sfDevices = new ArrayList<HostPciDevice>();
		if (pciDevices != null && !pciDevices.isEmpty())
			for (HostPciDevice pciDevice : pciDevices) {
				if (VENDOR_SOLARFLARE.equals(pciDevice.getVendorName()))
					sfDevices.add(pciDevice);
			}
		return sfDevices;
	}

}
