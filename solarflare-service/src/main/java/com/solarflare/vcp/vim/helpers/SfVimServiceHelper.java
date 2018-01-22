package com.solarflare.vcp.vim.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.VMNIC;
import com.vmware.vim25.HostPciDevice;
import com.vmware.vim25.PhysicalNic;
import com.vmware.vim25.SoftwarePackage;

public class SfVimServiceHelper {
	public static final String VENDOR_SOLARFLARE = "Solarflare";
	public static final String SLF = "SLF";
	public static final String CIM = "cim";
	public static final String SFC = "sfc";
	public static final short SOLARFLARE_VENDOR_ID = 6436;
	// TODO : Add logger and null check

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
	public static Map<String, PhysicalNic> getSfPhysicalNic(List<PhysicalNic> physicalNics, List<String> pciIDs) {
		Map<String, PhysicalNic> sfNics = new HashMap<>();
		if (physicalNics != null && !physicalNics.isEmpty())
			for (PhysicalNic physicalNic : physicalNics) {
				String pci = physicalNic.getPci();
				if (pciIDs.contains(pci))
					sfNics.put(pci, physicalNic);
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
				if (SOLARFLARE_VENDOR_ID == pciDevice.getVendorId())
					sfDevices.add(pciDevice);
			}
		return sfDevices;
	}

	/**
	 * Returns the IDs of PCI devices
	 * 
	 * @param pciDevices
	 * @return
	 */
	public static List<String> getSfPciDeviceIds(List<HostPciDevice> pciDevices) {
		List<String> sfDeviceIds = new ArrayList<String>();
		if (pciDevices != null && !pciDevices.isEmpty())
			for (HostPciDevice pciDevice : pciDevices) {
				sfDeviceIds.add(pciDevice.getId());
			}
		return sfDeviceIds;
	}

	/**
	 * Returns Adapter ID from host name and PCI ID
	 * 
	 * @param hostName
	 * @param pciId
	 * @return
	 */
	public static String getAdapterId(String hostName, String pciId) {
		String adapterId = null;
		if (hostName != null && pciId != null) {
			// TODO : Review Comment : Use constants instead of hard coding
			adapterId = hostName + "::" + pciId.split("\\.")[0];
		}
		return adapterId;

	}

	public static Map<String, List<VMNIC>> mergeToVMNICObject(List<HostPciDevice> pciDevices,
			Map<String, PhysicalNic> pNICs, String hostName) {
		Map<String, List<VMNIC>> vmNICmap = new HashMap<>();
		for (HostPciDevice pciDevice : pciDevices) {
			PhysicalNic pNIC = pNICs.get(pciDevice.getId());
			if (pNIC == null) {
				// TODO : add log and skip
			} else {
				String id = SfVimServiceHelper.getAdapterId(hostName, pciDevice.getId());

				VMNIC vmNIC = new VMNIC();
				vmNIC.setName(pNIC.getDevice());
				vmNIC.setId(pNIC.getDevice());
				vmNIC.setDeviceName(pciDevice.getDeviceName());
				vmNIC.setMacAddress(pNIC.getMac());
				vmNIC.setPciId(pciDevice.getId());
				vmNIC.setPciFunction(Byte.toString(pciDevice.getFunction()));
				vmNIC.setPciBusNumber(Byte.toString(pciDevice.getBus()));

				List<VMNIC> vmNICList = vmNICmap.get(id);
				if (vmNICList == null) {
					vmNICList = new ArrayList<>();

				}

				vmNICList.add(vmNIC);
				vmNICmap.put(id, vmNICList);
			}
		}

		return vmNICmap;
	}

	public static int getPortCount(List<Adapter> adapters) {
		int portCount = 0;
		for (Adapter adp : adapters) {
			if (adp.getChildren() != null) {
				portCount += adp.getChildren().size();
			}
		}
		return portCount;
	}

	public static String getMinMacAddress(List<VMNIC> nicList) {
		if (nicList != null) {
			String[] macAddresses = new String[nicList.size()];
			int i = 0;
			for (VMNIC nic : nicList) {
				macAddresses[i++] = nic.getMacAddress();
			}
			Arrays.sort(macAddresses);
			// TODO : Review Comment : Use constants
			String plainMinMac = macAddresses[0].replaceAll(":", "");
			return plainMinMac;
		}
		return null;
	}
}
