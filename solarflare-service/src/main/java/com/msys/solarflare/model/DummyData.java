package com.msys.solarflare.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyData {
	Map<String, Host> data;

	public List<Host> getHostAdapters() {
		ArrayList<Host> result = new ArrayList<>(data.values());
		Collections.sort(result);
		return result;
	}

	public Host getHostAdapters(String id) {
		return data.get(id);
	}

	public DummyData() {
		// create some dummy data
		data = new HashMap<>();
		Host hostA = getHost("Host-A", "Host-A-id");

		List<Adapter> adpaters = new ArrayList<>();
		Adapter adapterA = getAdapter("Adapter 1", "1.1.1.0",
				"1.1.2.0", "1.1.3.0");
		List<VMNIC> nics = new ArrayList<>();
		nics.add(getVmNic("vmnic-0", "connected", "a2:2b:22:43:33:4f"));
		nics.add(getVmNic("vmnic-1", "Disconnected", "b2:4f:2f:34:93:87"));
		adapterA.setChildren(nics);
		adpaters.add(adapterA);

		Adapter adapterB = getAdapter("Adapter 2", "2.1.1.0",
				"2.1.2.0", "2.1.3.0");

		List<VMNIC> nicsB = new ArrayList<>();
		nicsB.add(getVmNic("vmnic-2", "connected", "a2:2b:22:43:33:4f"));
		adapterB.setChildren(nicsB);
		adpaters.add(adapterB);

		hostA.setChildren(adpaters);

		data.put(hostA.getId(), hostA);

		Host hostB = getHost("Host-B", "Host-B-id");

		List<Adapter> adpatersHostB = new ArrayList<>();
		Adapter adpaterHostB = getAdapter("Adapter	Intel Corporation 82546EB Gigabit Ethernet Controller", "1.1.1.0",
				"1.1.2.0", "1.1.3.0");
		List<VMNIC> nicsHostB = new ArrayList<>();
		nicsHostB.add(getVmNic("vmnic-0", "connected", "q2:2f:55:99:33:4f"));
		adpaterHostB.setChildren(nicsHostB);
		adpatersHostB.add(adpaterHostB);

		hostB.setChildren(adpatersHostB);
		data.put(hostB.getId(), hostB);
	}

	private Host getHost(String name, String id) {
		Host host = new Host();
		host.setId(id);
		host.setName(name);
		return host;
	}

	private Adapter getAdapter(String name, String bootROM, String controller, String uefiROM) {
		Adapter adapter = new Adapter();
		adapter.setName(name);
		adapter.setVersionBootROM(bootROM);
		adapter.setVersionController(controller);
		adapter.setVersionUEFIROM(uefiROM);
		return adapter;
	}

	private VMNIC getVmNic(String name, String status, String macAddress) {
		VMNIC nic = new VMNIC();
		nic.setCurrentMTU("100");
		nic.setDeviceId("sfvmk");
		nic.setDriverName("sfvmk");
		nic.setDriverVersion("1.0.0");
		nic.setStatus(status);
		nic.setMacAddress(macAddress);
		nic.setName(name);
		nic.setMaxMTU("1000");
		nic.setInterfaceName(name);
		nic.setPciBusNumber("a:b:c");
		nic.setPciFunction(name);
		nic.setPortSpeed("10 Gbps");
		return nic;
	}

	public static void main(String[] args) {
		System.out.println(new DummyData().getHostAdapters());
	}
}
