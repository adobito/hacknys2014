package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
//	val fact = new Configuration().configure().buildSessionFactory();
  def index = Action {
//	val sess = fact.openSession();
//	val hql = "FROM UserDto";
//	val query = sess.createQuery(hql);
//	val listResults = query.list();
    
    Ok(views.html.index(Database.addUser("sgfdgdf", "gfhfghfghfghfg").toString))
  }

  

}