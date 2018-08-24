
package com.run4sky.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientManager.ReconnectHandler;
import org.glassfish.tyrus.client.ClientProperties;

import com.gluonhq.charm.glisten.control.TextField;
import com.google.gson.JsonObject;
import com.run4sky.hardware.HardwareInfo;
import com.run4sky.json.JsonDecoder;
import com.run4sky.json.JsonEncoder;
import com.run4sky.network.GetNetworkAddress;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * Clase controller de JavaFX. La anotación @ClientEndpoint indica que esta
 * clase es un cliente Websocket que se conecta al servidor cero.run4sky.com.
 * Además, esta clase controla los eventos de los componentes (botones,
 * textFields etc.) de JavaFX
 * 
 * @author mundakamacbook
 */

@ClientEndpoint(encoders = { JsonEncoder.class }, decoders = { JsonDecoder.class })
public class inicioController implements Initializable {

	public static final List<Session> sessionList = Collections.synchronizedList(new ArrayList<Session>());
	private Session session;
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public static String deviceType;
	private static boolean isOpenConnection = false;
	ClientManager client = ClientManager.createClient();
	URI endpointURI = URI.create("wss://cero.run4sky.com:8443/PuntoCeroServerMavenjdk10/endpoint");


	/**
	 * @return the sessionlist
	 */
	public static List<Session> getSessionlist() {
		return sessionList;
	}

	@OnOpen
	public void onOpen(Session session) {

		this.session = session;
		addSession(session);
		logger.info("dentro de onOpen");
		System.out.println("onOpen -->> Session: " + session.getId());
		createAndSendJson();

	}

	@OnMessage
	public void onMessage(JsonObject message) {
		logger.info("onMessage -> Mensage servidor: " + message.toString());
		serverMessageTxtArea.appendText(message.toString() + "\n");
		deviceType = message.get("ConnectionType").getAsString();
		// messageLatch.countDown();

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("Client on close...");
		logger.info(String.format("Sesion %s ha cerrado porque %s", session.getId(), closeReason));
		// messageLatch.countDown();
	}

	@OnError
	public void onError(Throwable t, Session session) {
		System.out.println("Client on error...");
		// messageLatch.countDown();
	}

	@FXML
	private AnchorPane anchorPaneMaster;

	@FXML
	private HBox hBoxCabeceraControls;

	@FXML
	private HBox hBoxCabeceraUser;

	@FXML
	private ImageView ImageViewPerfil;

	@FXML
	private Button updateConnBtn;

	@FXML
	private Button connBtn;

	@FXML
	private Button disconnBtn;

	@FXML
	private Button sendMessageBtn;

	@FXML
	private TextField txtUpdateConn;

	@FXML
	private TextArea sendMessageTxtArea;

	@FXML
	private TextArea serverMessageTxtArea;

	@FXML
	private TextField connTxt;

	@FXML
	private TextField macTxt;

	@FXML
	private TextField ipTxt;

	@FXML
	private TextField externalIpTxt;

	@FXML
	private TextField protocolTxt;

	@FXML
	private CheckBox registeredCheckBox;

	@FXML
	private CheckBox connCheckBox;

	@FXML
	private CheckBox typeConnCheckBox;
	
	/**
	 * Anade las sesiones abiertas en un lista
	 * @param session
	 */
	public static void addSession(Session session) {
		sessionList.add(session);
	}

	/**
	 * Metodo que conecta al servidor Websocket manualmente
	 * por el boton conectar.
	 * 
	 * @param event
	 */
	@FXML
	private void connectToWebSocket(ActionEvent event) {

		// URI.create("ws://localhost:8080/PuntoCeroServerMavenjdk10/endpoint");
		try {
			isOpenConnection = client.connectToServer(this, endpointURI).isOpen();
			if (isOpenConnection) {
				connCheckBox.setSelected(true);
				connCheckBox.setText("Connected");
				connTxt.setText(endpointURI.toString());
				//createAndSendJson();
				threadSleep();
				setTextFields();
				showConnectionType(deviceType);
				
			} else {
				connCheckBox.setSelected(false);
			}
		} catch (DeploymentException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	private void connectToWebSocket() {

		// URI endpointURI =
		// URI.create("ws://localhost:8080/PuntoCeroServerMavenjdk10/endpoint/" +
		// connectionType);
		try {
			isOpenConnection = client.connectToServer(this, endpointURI).isOpen();
			System.out.println("Conexion esta abierta? -->> " + isOpenConnection);
			if (isOpenConnection) {
				connCheckBox.setSelected(true);
				connTxt.setText(endpointURI.toString());
				connCheckBox.setText("Connected");
				// Crea el objeto Json y lo envia al servidor
				//createAndSendJson();
				// Thread sleep para esperar que el servidor conteste antes de cargar los
				threadSleep();
				// rellena los campos de texto de la aplicacion con informaciones acerca de la conexion.
				setTextFields();
				// Busca cual es el tipo del dispositivo y actualiza la pantalla con la informacion que corresponde.
				showConnectionType(deviceType);
			} else {
				connCheckBox.setSelected(false);
			}
		} catch (DeploymentException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Metodo que conecta al websocket al iniciar el programa.
	 * Si desconecta, conecta automaticamente hacia un numero 
	 * limitado de intentos Importante para evitar DDOS.
	 *  
	 * @return reconnectHandler
	 */
	private ReconnectHandler getReconnectHandler() {
		ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {

		  private int counter = 0;

		  @Override
		  public boolean onDisconnect(CloseReason closeReason) {
		    counter++;
		    if (counter <= 3) {
		      System.out.println("***Reconnecting... (reconnect count: " + counter + ")");
		      return true;
		    } else {
		      return false;
		    }
		  }

		  @Override
		  public boolean onConnectFailure(Exception exception) {
		    counter++;
		    if (counter <= 3) {
		      System.out.println("*** Reconnecting... (reconnect count: " + counter + ") " + exception.getMessage());

		      // Thread.sleep(...) or something other "sleep-like" expression can be put here - you might want
		      // to do it here to avoid potential DDoS when you don't limit number of reconnects.
		      return true;
		    } else {
		      return false;
		    }
		  }

		  @Override
		  public long getDelay() {
			//delay en segundos
		    return 3;
		  }
		};

		client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);
		
		return reconnectHandler;
	}

	/**
	 * Metodo sendMessage sin parametro.
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

		synchronized (sessionList) {
			for (Session session : sessionList) {

				System.out.println("Session: " + session.getId());

				try {
					session.getBasicRemote().sendObject(message);
				} catch (EncodeException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 */
	private void setTextFields() {
		macTxt.setText(GetNetworkAddress.getAddress("mac"));
		ipTxt.setText(GetNetworkAddress.getAddress("ip"));
		externalIpTxt.setText(GetNetworkAddress.getPublicIp());
	}
	
	/**
	 * 
	 */
	private void threadSleep() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 
	 * @param deviceType
	 */
	public void showConnectionType(String deviceType) {
		if (deviceType != null) {
			switch (deviceType) {
			case "com.run4sky.beans.ExternalDisp":
				typeConnCheckBox.setSelected(true);
				registeredCheckBox.setSelected(true);
				typeConnCheckBox.setText("Conexion Externa");
				break;
			case "com.run4sky.beans.SecureDisp":
				typeConnCheckBox.setSelected(true);
				registeredCheckBox.setSelected(true);
				typeConnCheckBox.setText("Dispositivo de Seguridad");
				break;
			case "com.run4sky.beans.InsernalService":
				typeConnCheckBox.setSelected(true);
				registeredCheckBox.setSelected(true);
				typeConnCheckBox.setText("Servicio Interno");
				break;
			case "com.run4sky.beans.ClienService":
				typeConnCheckBox.setSelected(true);
				registeredCheckBox.setSelected(true);
				typeConnCheckBox.setText("Servicio Cliente");
				break;
			default:
				logger.info("Switch case default");
				System.out.println("Device Type: " + deviceType);
				typeConnCheckBox.setText("Dispositivo no encontrado");
				break;
			}
		} else {
			System.out.println("Device Type: " + deviceType);
			typeConnCheckBox.setText("Dispositivo no encontrado");
		}
	}

	@Override
	public void initialize(URL name, ResourceBundle resources) {
		// reconecta si se desconecta.
		getReconnectHandler();
		// conecta al servidor Websocket al iniciar la aplicacion.
		connectToWebSocket();
		// evento para desconectar del servidor atraves del boton "Disconnect".
		disconnBtn.setOnAction((event) -> {
			try {
				for (Session session : sessionList) {
					session.close();
				}
				sessionList.clear();
				isOpenConnection = false;
				connCheckBox.setText("Disconnected");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			connTxt.setText("Disconnected");
		});
	}
}
