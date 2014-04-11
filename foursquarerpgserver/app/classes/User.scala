package classes

class User(var id: Int, var username: String, var attack: Int, var defense: Int,var hp: Int,var stamina: Int,var exp: Int) {
	def this() = {
	  this(-1,"",-1,-1,-1,-1,-1);
	}
}