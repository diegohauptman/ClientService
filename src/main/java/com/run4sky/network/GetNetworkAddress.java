package com.run4sky.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
/**
 * Clase que recupera la IP privada, IP publica y la Mac del dispositivo.
 * @author mundakamacbook
 *
 */
public class GetNetworkAddress {
	/**
	 * Recupera la IP publica a partir de una pagina web de amazon.
	 * @return
	 */
	public static String getPublicIp() {
		String ip = null;
		try {
			
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			ip = in.readLine();
			// you get the IP as a String
			System.out.println(ip);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ip;
	}
	/**
	 * Recupera la Ip privada o la Mac segun lo que se pasa por parametro en addressType,
	 * la string "mac" devolvera la mac, la string "ip" devolvera la ip privada.
	 * @param addressType
	 * @return
	 */
	public static String getAddress(String addressType) {
		String address = "";
		InetAddress lanIp = null;
		try {

			String ipAddress = null;
			Enumeration<NetworkInterface> net = null;
			net = NetworkInterface.getNetworkInterfaces();

			while (net.hasMoreElements()) {
				NetworkInterface element = net.nextElement();
				Enumeration<InetAddress> addresses = element.getInetAddresses();

				while (addresses.hasMoreElements() && !isVMMac(element.getHardwareAddress())) {
					InetAddress ip = addresses.nextElement();
					if (ip instanceof Inet4Address) {

						if (ip.isSiteLocalAddress()) {
							ipAddress = ip.getHostAddress();
							lanIp = InetAddress.getByName(ipAddress);
						}

					}

				}
			}

			if (lanIp == null)
				return null;

			if (addressType.equals("ip")) {

				address = lanIp.toString().replaceAll("^/+", "");

			} else if (addressType.equals("mac")) {

				address = getMacAddress(lanIp);

			} else {

				throw new Exception("Specify \"ip\" or \"mac\"");

			}

		} catch (UnknownHostException ex) {

			ex.printStackTrace();

		} catch (SocketException ex) {

			ex.printStackTrace();

		} catch (Exception ex) {

			ex.printStackTrace();

		}

		return address;

	}
	/**
	 * Metodo privado que recupera la mac address
	 * @param ip
	 * @return
	 */
	private static String getMacAddress(InetAddress ip) {
		String address = null;
		try {

			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
			address = sb.toString();

		} catch (SocketException ex) {

			ex.printStackTrace();

		}

		return address;
	}
	/**
	 * Metodo privado que devuelve true si la direccion Mac es de una maquina virtual.
	 * @param mac
	 * @return
	 */
	private static boolean isVMMac(byte[] mac) {
		if (null == mac)
			return false;
		byte invalidMacs[][] = { { 0x00, 0x05, 0x69 }, // VMWare
				{ 0x00, 0x1C, 0x14 }, // VMWare
				{ 0x00, 0x0C, 0x29 }, // VMWare
				{ 0x00, 0x50, 0x56 }, // VMWare
				{ 0x08, 0x00, 0x27 }, // Virtualbox
				{ 0x0A, 0x00, 0x27 }, // Virtualbox
				{ 0x00, 0x03, (byte) 0xFF }, // Virtual-PC
				{ 0x00, 0x15, 0x5D } // Hyper-V
		};

		for (byte[] invalid : invalidMacs) {
			if (invalid[0] == mac[0] && invalid[1] == mac[1] && invalid[2] == mac[2])
				return true;
		}

		return false;
	}
}