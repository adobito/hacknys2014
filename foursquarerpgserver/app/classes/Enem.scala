package classes

case class Enemy (var name: String, 
var attack: Int, 
var defense: Int, 
var hp: Int){
  def this() {
    this("", -1, -1, -1);
  }
	var id: Int = -1;
	var exp: Int = -1;
}
