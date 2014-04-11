package controllers
import play.api._
import play.api.Play.current
import play.api.db._
import play.api.mvc._
import hibernate.UserDto
import classes.Enemy
import classes.User

object Database {
	val sessionFactory = DB.getDataSource();
	println(sessionFactory.getConnection().prepareStatement("SELECT * FROM user").executeQuery().next());
	def f() {}
	def addUser(username:String, token: String): UserDto = {
			val someUser = getUser(username);
			if(someUser.id > 0) {
				return someUser;
			}
			val conn = sessionFactory.getConnection();
			val statement = conn.prepareStatement("INSERT INTO `foursquarerpg`.`user` " +
					"(`username`, " +
					"`token`, " +
					"`hp`, "+ 
					"`attack`, " +
					"`defense`, " +
					"`stamina`, " +
					"`exp`, " +
					"`gold`) " +
					"VALUES " +
					"('" + username + "', " +
					"'" + token + "', " +
					"10, " +
					"10, " +
					"10, " +
					"10, " +
					"10, " +
					"10);");
			statement.execute();
			getUser(getMaxUser);
	}
	def getMaxUser(): Int = {
			val conn = sessionFactory.getConnection();
			val statement = conn.prepareStatement("select max(id) from user u");
			val rs = statement.executeQuery();
			if(rs.next()) {
				rs.getInt("max(id)");
			}
			else -1
	}
	def getUser(id: Int): UserDto = {
			val conn = sessionFactory.getConnection();
			val statement = conn.prepareStatement("SELECT * FROM user u WHERE u.id = ?");
			statement.setInt(1, id);
			val rs = statement.executeQuery();
			val user = new UserDto();
			while(rs.next()) {
				user.setId(rs.getInt("id"));
				user.setToken(rs.getString("token"));
				user.setUsername(rs.getString("username"));
				user.setAttack(rs.getInt("attack"));
				user.setDefense(rs.getInt("defense"));
				user.setHp(rs.getInt("hp"));
				user.setStamina(rs.getInt("stamina"));
				user.setGold(rs.getInt("gold"));
				user.setExperience(rs.getInt("exp"));
			}
			user
	}
	def getUser(username: String): UserDto = {
			val conn = sessionFactory.getConnection();
			val statement = conn.prepareStatement("SELECT * FROM user u WHERE u.username = ?");
			statement.setString(1,username);
			val rs = statement.executeQuery();
			val user = new UserDto();
			while(rs.next()) {
				user.setId(rs.getInt("id"));
				user.setToken(rs.getString("token"));
				user.setUsername(rs.getString("username"));
				user.setAttack(rs.getInt("attack"));
				user.setDefense(rs.getInt("defense"));
				user.setHp(rs.getInt("hp"));
				user.setStamina(rs.getInt("stamina"));
				user.setGold(rs.getInt("gold"));
				user.setExperience(rs.getInt("exp"));
			}
			user
	}
	def addVenue(venueId: String, category: String, name: String): Boolean = { //change to return venueId
			val ds = DB.getDataSource();
			val conn = ds.getConnection();
			try {
				val statement = conn.prepareStatement("INSERT INTO `foursquarerpg`.`venue` (`venue_id`, `category`, `name`) VALUES (?,?,?);");
				statement.setString(1,venueId);
				statement.setString(2,category);
				statement.setString(3,name);
				statement.execute();
			}
			finally {
				conn.close();
			}
	}
	def getVenueIdFromFsqVenueId(fsqVenueId: String): Option[Long] = {
			val ds = DB.getDataSource();
			val conn = ds.getConnection();
			try {
				val statement = conn.prepareStatement("SELECT * FROM venue WHERE venue_id = ?");
				statement.setString(1,fsqVenueId);
				val result = statement.executeQuery();
				if(result.next()) {
					return Some(result.getLong("id"));
				}else {
					return None;
				}
			}
			finally {
				conn.close();
			}
	}
	def addEnemyToVenue(enemyId: Int, venueId: String) {
		val venue = getVenueIdFromFsqVenueId(venueId);
		val conn = sessionFactory.getConnection();
		val statement = conn.prepareStatement("UPDATE `foursquarerpg`.`venue` " +
				"SET " +
				"`enemy_id` = "+enemyId+" " +
				"WHERE `id` = "+getVenueIdFromFsqVenueId(venueId).get+";");
		statement.executeUpdate();
	}
	def makeEnemy(name: String, attack: Int, defense: Int, hp: Int, exp: Int): Enemy = {
			val conn = sessionFactory.getConnection();
			val statement = conn.prepareStatement("INSERT INTO `foursquarerpg`.`enemy` " + 
					"(`name`, " +
					"`attack`, " +
					"`defense`, " +
					"`hp`, " +
					"`exp`) " +
					"VALUES " +
					"('" + name + "', " +
					attack + ", " +
					defense + ", " +
					hp + ", " +
					exp + "); ");
			statement.execute();
			getEnemy(getMaxEnemy);
	}
	def getMaxEnemy(): Int = {
			val conn = sessionFactory.getConnection();
			val statement = conn.prepareStatement("select max(id) from `foursquarerpg`.`enemy`");
			val rs = statement.executeQuery();
			if(rs.next()) {
				rs.getInt("max(id)");
			}
			else -1;
	}
	def getEnemy(id: Int): Enemy = {
			val conn = sessionFactory.getConnection();
			val statement = conn.prepareStatement("SELECT * FROM `foursquarerpg`.`enemy` e WHERE e.id = ?");
			statement.setInt(1, id);
			val rs = statement.executeQuery();
			val enemy = new Enemy();
			while(rs.next()) {
				enemy.id = (rs.getInt("id"));
				enemy.name = (rs.getString("name"));
				enemy.attack = (rs.getInt("attack"));
				enemy.defense = (rs.getInt("defense"));
				enemy.hp = (rs.getInt("hp"));
				enemy.exp = (rs.getInt("exp"));
			}
			enemy
	}
	def getVenueEnemy(venueId: String): Option[Enemy] = {
			val conn = sessionFactory.getConnection();
			val statement = conn.prepareStatement("SELECT e.* from venue v, `foursquarerpg`.`enemy` e WHERE v.venue_id = '"+venueId+"' AND v.enemy_id = e.id");
			val rs = statement.executeQuery();
			if(rs.next()) {
				val enemy = new Enemy();
				enemy.id = (rs.getInt("id"));
				enemy.name = (rs.getString("name"));
				enemy.attack = (rs.getInt("attack"));
				enemy.defense = (rs.getInt("defense"));
				enemy.hp = (rs.getInt("hp"));
				enemy.exp = (rs.getInt("exp"));
				Some(enemy);
			} else {
				None;
			}
	}
	def updateUser(user: UserDto) {
		val conn = sessionFactory.getConnection();
		val statement = conn.prepareStatement("UPDATE `foursquarerpg`.`user` " +
				"SET " +
				"`attack` = "+ user.attack +", " +
				"`defense` = "+user.defense+", " +
				"`hp` = "+user.hp+", " +
				"`exp` = "+user.experience+ " "  +
				"WHERE `id` = "+user.id+";");
		statement.executeUpdate();

	}
	def updateEnemy(enemy: Enemy) {
		val conn = sessionFactory.getConnection();
		val statement = conn.prepareStatement("UPDATE `foursquarerpg`.`enemy` " +
				"SET " +
				"`attack` = "+ enemy.attack +", " +
				"`defense` = "+enemy.defense+", " +
				"`hp` = "+enemy.hp+" " +
				"WHERE `id` = "+enemy.id+";");
		statement.executeUpdate();

	}
}