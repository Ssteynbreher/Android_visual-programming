import kotlin.random.Random

open class Human(
    var fullname: String,
    var age: Int,
    override var speed: Double,
    var group_number: Int
) : Movable {
    override var x: Double = 0.0
    override var y: Double = 0.0

    init {
        println("Создан человек: $fullname, возраст: $age, номер группы: $group_number")
    }

    override fun move() {
        val directionX = Random.nextDouble(-1.0, 1.0)
        val directionY = Random.nextDouble(-1.0, 1.0)
        x += directionX * speed
        y += directionY * speed
        println("$fullname переместился в позицию: ${getPosition()}")
    }

    fun getPosition(): String = "(${"%.2f".format(x)}, ${"%.2f".format(y)})"
}