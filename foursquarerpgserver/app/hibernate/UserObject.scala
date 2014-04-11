package hibernate

import scala.beans.BeanProperty

case class UserDto(@BeanProperty var id: Int, 
		@BeanProperty var username: String, 
		@BeanProperty var token: String, 
		@BeanProperty var hp: Int,
		@BeanProperty var attack: Int,
		@BeanProperty var defense: Int, 
		@BeanProperty var stamina: Int, 
		@BeanProperty var experience: Int, 
		@BeanProperty var gold: Int) {

	def this() = {
		this(-1, "","", -1, -1, -1, -1, -1, -1)
	}

}