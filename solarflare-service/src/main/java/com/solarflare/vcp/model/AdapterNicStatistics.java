package com.solarflare.vcp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AdapterNicStatistics {
	
	String timePeriod_from;
	String timePeriod_to;
	String packetsReceived;
	String packetsSent;
	String bytesReceived;
	String bytesSent;
	String receivePacketsDropped;
	String transmitPacketsDropped;
	String multicastPacketsReceived;
	String broadcastPacketsSent;
	String totalReceiveError;
	String multicastPacketsSent;
	String broadcastPacketsReceived;
	String totalTransmitErrors;
	
	private static String  modifyTimeStamp(String inputData)
	{
		StringTokenizer token = new StringTokenizer(inputData,"To");
		String outputData = token.nextToken() + " " + token.nextToken().replace("Z","");
		return outputData;
	}
	
	public static List<String> performanceCounter = new ArrayList<>();
	static{
		performanceCounter.add("net.packetsRx.SUMMATION");
		performanceCounter.add("net.packetsTx.SUMMATION");
		performanceCounter.add("net.bytesRx.AVERAGE");
		performanceCounter.add("net.bytesTx.AVERAGE");
		performanceCounter.add("net.droppedRx.SUMMATION");
		performanceCounter.add("net.droppedTx.SUMMATION");
		performanceCounter.add("net.multicastRx.SUMMATION");
		performanceCounter.add("net.broadcastRx.SUMMATION");
		performanceCounter.add("net.multicastTx.SUMMATION");
		performanceCounter.add("net.broadcastTx.SUMMATION");
		performanceCounter.add("net.errorsRx.SUMMATION");
		performanceCounter.add("net.errorsTx.SUMMATION");
	}
	public String getTimePeriod_from() {
		return timePeriod_from;
	}
	public void setTimePeriod_from(String timePeriod_from) {
		this.timePeriod_from = modifyTimeStamp(timePeriod_from);
	}
	public String getTimePeriod_to() {
		return timePeriod_to;
	}
	public void setTimePeriod_to(String timePeriod_to) {
		this.timePeriod_to = modifyTimeStamp(timePeriod_to);
	}
	
	
	
	public String getMulticastPacketsSent() {
		return multicastPacketsSent;
	}
	public void setMulticastPacketsSent(String multicastPacketsSent) {
		this.multicastPacketsSent = multicastPacketsSent;
	}
	public String getBroadcastPacketsReceived() {
		return broadcastPacketsReceived;
	}
	public void setBroadcastPacketsReceived(String broadcastPacketsReceived) {
		this.broadcastPacketsReceived = broadcastPacketsReceived;
	}
	public String getPacketsReceived() {
		return packetsReceived;
	}
	public void setPacketsReceived(String packetsReceived) {
		this.packetsReceived = packetsReceived;
	}
	public String getPacketsSent() {
		return packetsSent;
	}
	public void setPacketsSent(String packetsSent) {
		this.packetsSent = packetsSent;
	}
	public String getBytesReceived() {
		return bytesReceived;
	}
	public void setBytesReceived(String bytesReceived) {
		this.bytesReceived = bytesReceived;
	}
	public String getBytesSent() {
		return bytesSent;
	}
	public void setBytesSent(String bytesSent) {
		this.bytesSent = bytesSent;
	}
	public String getReceivePacketsDropped() {
		return receivePacketsDropped;
	}
	public void setReceivePacketsDropped(String receivePacketsDropped) {
		this.receivePacketsDropped = receivePacketsDropped;
	}
	public String getTransmitPacketsDropped() {
		return transmitPacketsDropped;
	}
	public void setTransmitPacketsDropped(String transmitPacketsDropped) {
		this.transmitPacketsDropped = transmitPacketsDropped;
	}
	public String getMulticastPacketsReceived() {
		return multicastPacketsReceived;
	}
	public void setMulticastPacketsReceived(String multicastPacketsReceived) {
		this.multicastPacketsReceived = multicastPacketsReceived;
	}
	
	public String getBroadcastPacketsSent() {
		return broadcastPacketsSent;
	}
	public void setBroadcastPacketsSent(String broadcastPacketsSent) {
		this.broadcastPacketsSent = broadcastPacketsSent;
	}
	public String getTotalReceiveError() {
		return totalReceiveError;
	}
	public void setTotalReceiveError(String totalReceiveError) {
		this.totalReceiveError = totalReceiveError;
	}
	
	public String getTotalTransmitErrors() {
		return totalTransmitErrors;
	}
	public void setTotalTransmitErrors(String totalTransmitErrors) {
		this.totalTransmitErrors = totalTransmitErrors;
	}
	@Override
	public String toString() {
		return "AdapterNicStatistics [timePeriod_from=" + timePeriod_from + ", timePeriod_to=" + timePeriod_to
				+ ", packetsReceived=" + packetsReceived + ", packetsSent=" + packetsSent + ", bytesReceived="
				+ bytesReceived + ", bytesSent=" + bytesSent + ", receivePacketsDropped=" + receivePacketsDropped
				+ ", transmitPacketsDropped=" + transmitPacketsDropped + ", multicastPacketsReceived="
				+ multicastPacketsReceived + ", broadcastPacketsSent=" + broadcastPacketsSent + ", totalReceiveError="
				+ totalReceiveError + ", multicastPacketsSent=" + multicastPacketsSent + ", broadcastPacketsReceived="
				+ broadcastPacketsReceived + ", totalTransmitErrors=" + totalTransmitErrors + "]";
	}

}
