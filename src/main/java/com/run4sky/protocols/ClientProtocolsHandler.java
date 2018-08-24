package com.run4sky.protocols;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.google.gson.JsonObject;
import com.run4sky.controller.inicioController;
import com.run4sky.hardware.HardwareInfo;
import com.run4sky.network.GetNetworkAddress;

/**
 * Classe que maneja los protocolos
 * @author user
 *
 */
public class ClientProtocolsHandler {
	
	public void prot200() {
		
		// Crea el objeto Json y lo envia al servidor
		createAndSendJson();
		// Thread sleep para esperar que el servidor conteste antes de cargar los
		//threadSleep();
		// rellena los campos de texto de la aplicacion con informaciones acerca de la conexion.
		//setTextFields();
		// Busca cual es el tipo del dispositivo y actualiza la pantalla con la informacion que corresponde.
		//showConnectionType(deviceType);
		
	}
	
	/**
	 * Construye mensage en Json y envia al servidor.
	 */
	private void createAndSendJson() {

		// recoge informaciones del sistema y de red.
		String publicIp = GetNetworkAddress.getPublicIp();
		String privateIp = GetNetworkAddress.getAddress("ip");
		String mac = GetNetworkAddress.getAddress("mac");
		String os = HardwareInfo.getOS();
		String cpuModel = HardwareInfo.getProcessorName();
		int numberOfCPU = HardwareInfo.getQuantityOfCPU();
		long memoryQuantity = HardwareInfo.getMemoryQuantity();

		// crea un objeto JSON e lo rellena con las informaciones
		JsonObject gsonObject = new JsonObject();
		gsonObject.addProperty("private ip", privateIp);
		gsonObject.addProperty("public ip", publicIp);
		gsonObject.addProperty("mac", mac);
		gsonObject.addProperty("protocol", 100);
		gsonObject.addProperty("os", os);
		gsonObject.addProperty("cpuModel", cpuModel);
		gsonObject.addProperty("numberOfCPU", numberOfCPU);
		gsonObject.addProperty("memoryQuantity", memoryQuantity);
		System.out.println("Gson message: " + gsonObject.toString());

		// envia el mensage en JSON al servidor
		sendMessage(gsonObject);

	}
	
	/**
	 * Metodo que envia mensages al servidor Websocket.
	 * 
	 * @param message
	 */
	private void sendMessage(JsonObject message) {

		System.out.println("dentro de sendMessage");
		System.out.println("Mensage: " + message.toString());

		synchronized (inicioController.sessionList) {
			for (Session session : inicioController.sessionList) {

				System.out.println("Session: " + session.getId());

				try {
					session.getBasicRemote().sendObject(message);
				} catch (EncodeException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
