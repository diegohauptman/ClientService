package com.run4sky.hardware;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class HardwareInfo {

	static SystemInfo si = new SystemInfo();
	static HardwareAbstractionLayer hal = si.getHardware();

	public static String getOS() {

		OperatingSystem os = si.getOperatingSystem();
		System.out.println("OS: " + os.toString());

		return os.toString();
	}

	public static int getQuantityOfCPU() {
		int numberOfProcessors = hal.getProcessor().getPhysicalProcessorCount();
		System.out.println(hal.getProcessor().getPhysicalProcessorCount() + " CPUs");

		return numberOfProcessors;
	}

	public static String getProcessorName() {
		String processorName = hal.getProcessor().getName();
		System.out.println("Modelo procesador: " + processorName);
		return processorName;
	}

	public static  long getMemoryQuantity() {
		long memoryQuantity = hal.getMemory().getTotal();
		System.out.println("Cantidad de memoria: " + hal.getMemory().getTotal());
		return memoryQuantity;
	}

	
}
