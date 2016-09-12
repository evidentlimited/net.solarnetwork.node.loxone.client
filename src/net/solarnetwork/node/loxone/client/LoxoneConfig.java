package net.solarnetwork.node.loxone.client;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoxoneConfig {
	
//	Room
	
	public class Room {
		public String uuid;
		public String name;
		public String image;
		
		public Room(String uuid, String name, String image) {
			this.uuid = uuid;
			this.name = name;
			this.image = image;
		}
	}
	
//	Category
	
	public class Category {
		public String uuid;
		public String name;
		public String image;
		public String type;
		public String color;
		
		public Category(String uuid, String name, String image, String type, String color) {
			this.uuid = uuid;
			this.name = name;
			this.image = image;
			this.type = type;
			this.color = color;
		}
	}
	
//	Controls
	
	public class Control {
		public String uuid;
		public String name;
		public String type;
		public String room;
		public String category;
		
		public Control(String uuid, String name, String type, String room, String category) {
			this.uuid = uuid;
			this.name = name;
			this.type = type;
			this.room = room;
			this.category = category;
		}
	}
	
//	Variables
	
	Room[] rooms;
	Category[] categories;
	Control[] controls;
	
	String lastModified;

//	Initializers
	
	public LoxoneConfig() {
		// Do nothing
	}

	public LoxoneConfig(String loxapp) {
		processLoxApp(loxapp);
	}
	
//	Process LoxAPP.json file
	
	private void processLoxApp(String loxapp) {
		
		JSONObject config = new JSONObject(loxapp);
	
//		Last modified
		lastModified = config.getString("lastModified");
		
//		Build rooms
		JSONObject roomConfig = config.getJSONObject("rooms");
		JSONArray roomNames = roomConfig.names();
		rooms = new Room[roomNames.length()];
		
		for(int i = 0; i < roomNames.length(); i++) {
			JSONObject room = roomNames.getJSONObject(i);
			
			String uuid = room.getString("uuid");
			String name = room.getString("name");
			String image = room.getString("image");
			
			rooms[i] = new Room(uuid, name, image);
		}
		
//		Build categories
		JSONObject categoryConfig = config.getJSONObject("cats");
		JSONArray categoryNames = categoryConfig.names();
		categories = new Category[categoryNames.length()];
		
		for(int i = 0; i < categoryNames.length(); i++) {
			JSONObject category = categoryNames.getJSONObject(i);
			
			String uuid = category.getString("uuid");
			String name = category.getString("name");
			String image = category.getString("image");
			String type = category.getString("type");
			String color = category.getString("color");
			
			categories[i] = new Category(uuid, name, image, type, color);
		}
		
//		Build controls
		JSONObject controlConfig = config.getJSONObject("cats");
		JSONArray controlNames = controlConfig.names();
		controls = new Control[controlNames.length()];
		
		for(int i = 0; i < controlNames.length(); i++) {
			JSONObject control = controlNames.getJSONObject(i);
			
			String uuid = control.getString("uuid");
			String name = control.getString("name");
			String type = control.getString("type");
			String room = control.getString("room");
			String category = control.getString("category");
			
			controls[i] = new Control(uuid, name, type, room, category);
		}
	}
}
