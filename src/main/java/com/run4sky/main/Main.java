package com.run4sky.main;

import java.io.IOException;
import java.util.List;

import javax.websocket.Session;

import com.run4sky.controller.inicioController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Parent root = FXMLLoader.load(getClass().getResource("/view/inicio.fxml"));
		Scene scene = new Scene(root);

		primaryStage.setScene(scene);
		primaryStage.show();
		
		//Cuando se cierra la aplicacion se cierra cualquier sesion que pueda estar abierta.
		primaryStage.setOnCloseRequest((event)->{
			System.out.println("App closing");
			List <Session> sessionList =inicioController.getSessionlist();
			synchronized (sessionList) {
				for (Session session : sessionList) {
					try {
						session.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}
	

	public static void main(String[] args) {
		launch(args);
	}
}
