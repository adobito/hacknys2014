package controllers

import scala.collection.mutable.HashMap
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.http.json.JsonHttpContent
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.util.GenericData
import com.google.gson.Gson
import fi.foyt.foursquare.api.FoursquareApi
import other.FoursquareCredentials
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import classes.Enemy


object Foursquare extends Controller {
	val fsq = new FoursquareApi(FoursquareCredentials.CLIENT_ID, FoursquareCredentials.CLIENT_SECRET, FoursquareCredentials.PUSH_SECRET);
	val requestFactory = new NetHttpTransport().createRequestFactory();
	val gson = new Gson();
	val categoriesMap = HashMap("Food" -> "HP", "Nightlife Spot" -> "Attack",
			"Arts & Entertainment" -> "HP", "College & University" -> "Defense",
			"Professional & Other Places" -> "Attack", "Residence" -> "Defense",
			"Outdoors & Recreation" -> "Attack", "Shop & Service" -> "HP",
			"Travel & Transport" -> "Defense");

	def redirectGet(code: String) = Action {
		println(code)
		fsq.authenticateCode(code)
		Ok("{\"token\":" +fsq.getOAuthToken() +  "}").as("application/json");
	}

	def some = Action {
		Redirect(fsq.getAuthenticationUrl());
	}
	def redirectPost = Action {
		//	      request => values = request.body.asFormUrlEncoded.get;

		Redirect(fsq.getAuthenticationUrl());
	}
	def sendToken(token: String) = Action {

		fsq.setoAuthToken(token);
		val username = fsq.user("self").getResult().getId();
		Ok(gson.toJson(Database.addUser(username, token)))
	}
	def handlePush = Action {
		request => val values = request.body.asFormUrlEncoded.get;
		println(values);
		val checkinId = values.get("checkin").get(0);
		val userId = Json.parse(values.get("user").get(0)).\("id");
		val js = Json.parse(checkinId);
		val fsqVenueId = js.\("venue").\("id").as[String];
		val categoryList:List[String] = (js.\\("parents")(0).as[List[String]]);
		val category = categoryList(0);
		val name = (js \ ("venue")).\("name").as[String];
		println(fsqVenueId);
		println(category);
		println(name);

		var dbId = Database.getVenueIdFromFsqVenueId(fsqVenueId);
		if(!dbId.isDefined) {
			Database.addVenue(fsqVenueId, category, name);
			dbId = Database.getVenueIdFromFsqVenueId(fsqVenueId);
		}
		val enemyOption = Database.getVenueEnemy(fsqVenueId);
		var enemy:Enemy = null;	
		if(enemyOption.isDefined) {
			enemy = enemyOption.get;
		} else {
			enemy = Database.makeEnemy("UglyGuy", 10, 10, 10, 10);
			Database.addEnemyToVenue(enemy.id, fsqVenueId);

		}
		categoriesMap(category.trim()) match {
		case "HP" => {}
		case "Attack" => {}
		case "Defense" => {}
		}
		val monsterIntId = enemy.id;
		val enemyName = enemy.name;
		val userIntId = userId.toString.replace("\"", "").toInt;
		println(userIntId);
		val user = Database.getUser(userIntId.toString);
		println(user);
		categoriesMap(category.trim()) match {
		case "HP" => {user.hp += 1}
		case "Attack" => {user.attack +=1}
		case "Defense" => {user.defense +=1}
		}
		Database.updateUser(user);
		sendPush(enemyName,monsterIntId, userIntId);
		Ok(views.html.index("Success!"))
	}

	def getUserGet(username: String) = Action {
		Ok(gson.toJson(Database.getUser(username)));
	}
	def sendPush(enemyName: String, monsterId: Int, userId: Int) {
		val generic = new GenericData();
		generic.put("registration_ids", Array("APA91bFTwIqwYXxPCg9IOKN28K-M6l5FhcBFw8SBzPr1925ndG07SAVIPGv9MyiNCZpt4WDNvIsowPjOnGKwlm4bUGu07xPZZ7JteU8amPxN9NZUfxCJ-dPDjbYT8FZdJ99xqg6y8HU9ZOrkUb8KEh-bmPtcX2iCkVJ5VI2xrUtDocfWLspLfHE","APA91bHzY6VlZSi5nUBJdHCy8ncMXWc5rLw3YqEaSlnxpbIAQu65ddYL-cI5UymgI9wVEAVvGI2Zo-7djrGza_dqnHY436fvuqruxve-IpJNuva8BoaSPbtTyFDoCYOMoiP3Mh-MJ79ujD88Ft3Tf9YU5emXWHadZVj1OIe-zoSrf0mPNHBtDtk"))
		val otherGeneric = new GenericData();
		otherGeneric.put("key", enemyName);
		otherGeneric.put("monsterKey", monsterId);
		otherGeneric.put("userKey", userId);
		generic.put("data", otherGeneric);
		val json = new JsonHttpContent(new JacksonFactory(),generic);
		val post = requestFactory.buildPostRequest(new GenericUrl("https://android.googleapis.com/gcm/send"), json);
		val header = new HttpHeaders();
		header.setContentType("application/json");
		header.setAuthorization("key=AIzaSyAe1TgUXvuoWJTcYPNCIvCgM0r4yD4MnC0");
		post.setHeaders(header);
		post.execute();

	}
	def doBattle(userId: Int, enemyId: Int, battleType: String) = Action {
		val user = Database.getUser(userId.toString);
		val enemy = Database.getEnemy(enemyId);
		battleType match {
		case "playerAttack" => { user.stamina -= 1; enemy.hp -= Math.max( user.attack - enemy.defense,1); user.hp -= Math.max(enemy.attack - user.defense,1)}
		case "playerDefense" => { user.hp -= Math.max(enemy.attack - user.defense * 2,1); user.stamina += 1}
		}
		val map = Map("userHP" -> user.hp, "userStam" -> user.stamina, "enemyHP" -> enemy.hp);
		Database.updateEnemy(enemy);
		Database.updateUser(user);
		Ok(Json.toJson(map));
	}
	def getEnemyGet(id: Int) = Action {
		Ok(gson.toJson(Database.getEnemy(id)));
	}
	def getBattleInfo(userId: Int, enemyId: Int) = Action {
	  
	  val user = Database.getUser(userId.toString);
	  val enemy = Database.getEnemy(enemyId);
	  val map = Map("user" -> gson.toJson(user), "enemy" -> gson.toJson(enemy));
	  Ok(Json.toJson(map));
	}
}