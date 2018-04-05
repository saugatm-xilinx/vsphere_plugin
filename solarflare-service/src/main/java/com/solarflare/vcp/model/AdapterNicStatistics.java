package com.solarflare.vcp.model;

import java.util.ArrayList;
import java.util.List;

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
	String receiveLengthErrors;
	String receiveOverErrors;
	String receiveCRCErrors;
	String reveiveFrameErrors;
	String reveiveFIFOErrors;
	String receiveMissedErrors;
	String totalTransmitErrors;
	String transmitAbortedErrors;
	String transmitCarrierErrors;
	String transmitFIFOErrors;
	String multicastPacketsSent;
	String broadcastPacketsReceived;
	

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
		this.timePeriod_from = timePeriod_from;
	}
	public String getTimePeriod_to() {
		return timePeriod_to;
	}
	public void setTimePeriod_to(String timePeriod_to) {
		this.timePeriod_to = timePeriod_to;
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
	public String getReceiveLengthErrors() {
		return receiveLengthErrors;
	}
	public void setReceiveLengthErrors(String receiveLengthErrors) {
		this.receiveLengthErrors = receiveLengthErrors;
	}
	public String getReceiveOverErrors() {
		return receiveOverErrors;
	}
	public void setReceiveOverErrors(String receiveOverErrors) {
		this.receiveOverErrors = receiveOverErrors;
	}
	public String getReceiveCRCErrors() {
		return receiveCRCErrors;
	}
	public void setReceiveCRCErrors(String receiveCRCErrors) {
		this.receiveCRCErrors = receiveCRCErrors;
	}
	public String getReveiveFrameErrors() {
		return reveiveFrameErrors;
	}
	public void setReveiveFrameErrors(String reveiveFrameErrors) {
		this.reveiveFrameErrors = reveiveFrameErrors;
	}
	public String getReveiveFIFOErrors() {
		return reveiveFIFOErrors;
	}
	public void setReveiveFIFOErrors(String reveiveFIFOErrors) {
		this.reveiveFIFOErrors = reveiveFIFOErrors;
	}
	public String getReceiveMissedErrors() {
		return receiveMissedErrors;
	}
	public void setReceiveMissedErrors(String receiveMissedErrors) {
		this.receiveMissedErrors = receiveMissedErrors;
	}
	public String getTotalTransmitErrors() {
		return totalTransmitErrors;
	}
	public void setTotalTransmitErrors(String totalTransmitErrors) {
		this.totalTransmitErrors = totalTransmitErrors;
	}
	public String getTransmitAbortedErrors() {
		return transmitAbortedErrors;
	}
	public void setTransmitAbortedErrors(String transmitAbortedErrors) {
		this.transmitAbortedErrors = transmitAbortedErrors;
	}
	public String getTransmitCarrierErrors() {
		return transmitCarrierErrors;
	}
	public void setTransmitCarrierErrors(String transmitCarrierErrors) {
		this.transmitCarrierErrors = transmitCarrierErrors;
	}
	public String getTransmitFIFOErrors() {
		return transmitFIFOErrors;
	}
	public void setTransmitFIFOErrors(String transmitFIFOErrors) {
		this.transmitFIFOErrors = transmitFIFOErrors;
	}
	@Override
	public String toString() {
		return "AdapterNicStatistics [timePeriod_from=" + timePeriod_from + ", timePeriod_to=" + timePeriod_to
				+ ", packetsReceived=" + packetsReceived + ", packetsSent=" + packetsSent + ", bytesReceived="
				+ bytesReceived + ", bytesSent=" + bytesSent + ", receivePacketsDropped=" + receivePacketsDropped
				+ ", transmitPacketsDropped=" + transmitPacketsDropped + ", multicastPacketsReceived="
				+ multicastPacketsReceived + ", broadcastPacketsSent=" + broadcastPacketsSent + ", totalReceiveError="
				+ totalReceiveError + ", receiveLengthErrors=" + receiveLengthErrors + ", receiveOverErrors="
				+ receiveOverErrors + ", receiveCRCErrors=" + receiveCRCErrors + ", reveiveFrameErrors="
				+ reveiveFrameErrors + ", reveiveFIFOErrors=" + reveiveFIFOErrors + ", receiveMissedErrors="
				+ receiveMissedErrors + ", totalTransmitErrors=" + totalTransmitErrors + ", transmitAbortedErrors="
				+ transmitAbortedErrors + ", transmitCarrierErrors=" + transmitCarrierErrors + ", transmitFIFOErrors="
				+ transmitFIFOErrors + "]";
	}

	
}
