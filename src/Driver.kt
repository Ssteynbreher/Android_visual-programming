class Driver(
    fullname: String,
    age: Int,
    override var speed: Double,
    group_number: Int,
    var direction: String = "right"
) : Human(fullname, age, speed, group_number) {

    override fun move() {
        when (direction) {
            "up" -> y += speed
            "down" -> y -= speed
            "left" -> x -= speed
            "right" -> x += speed
        }
        println("Водитель $fullname поехал $direction, позиция: ${getPosition()}")
    }

    fun changeDirection(newDir: String) {
        direction = newDir
        println("Водитель $fullname сменил направление на $newDir")
    }
}