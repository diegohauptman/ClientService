package com.run4sky.json;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;
import com.google.gson.JsonObject;



public class JsonEncoder implements Encoder.Text<JsonObject>  {
	
	private static Gson gson = new Gson();
	
	@Override
	public String encode(JsonObject object) throws EncodeException {
		return gson.toJson(object);
	}
	
	@Override
	public void init(EndpointConfig endpointConfig) {
		//no op
	}

	@Override
	public void destroy() {
		//no op
	}


	

	

	

	

}
