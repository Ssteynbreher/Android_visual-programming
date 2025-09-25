import kotlin.concurrent.thread
import kotlin.random.Random

open class Human(
    var fullname: String,
    var age: Int,
    var speed: Double,
    var group_number: Int,
) {
    var x: Double = 0.0
    var y: Double = 0.0

    init {
        println("Создан человек: $fullname, возраст: $age, номер группы: $group_number")
    }

    open fun move() {
        val directionX = Random.nextDouble(-1.0, 1.0)
        val directionY = Random.nextDouble(-1.0, 1.0)

        x += directionX * speed
        y += directionY * speed

        println("$fullname переместился в позицию: (${"%.2f".format(x)}, ${"%.2f".format(y)})")
    }

    fun getPosition(): String = "(${"%.2f".format(x)}, ${"%.2f".format(y)})"
}

class Driver(
    fullname: String,
    age: Int,
    speed: Double,
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

fun main() {
    val humans = arrayOf(
        Human("Штейнбрехер Софья Владимировна", 20, 1.0, group_number = 433),
        Human("Петров Петр Петрович", 25, 1.5, group_number = 43),
        Human("Сидорова Анна Сергеевна", 18, 0.8, group_number = 25),
        Human("Козлов Михаил Юрьевич", 30, 1.2, group_number = 636)
    )

    val driver = Driver("Иванов Иван Иванович", 30, 2.0, 101)

    val time = 10

    println("\n=== НАЧАЛО СИМУЛЯЦИИ ===")

    val all = humans + driver

    val threads = all.map { obj ->
        thread {
            for (second in 1..time) {
                Thread.sleep(300)
                if (obj is Driver) {
                    // водитель иногда меняет направление
                    if (second % 3 == 0) {
                        val dirs = listOf("up", "down", "left", "right")
                        obj.changeDirection(dirs.random())
                    }
                    obj.move()
                } else {
                    obj.move()
                }
            }
        }
    }

    threads.forEach { it.join() }

    println("\nКонец симуляции")
    println("\nФинальные позиции:")
    for (human in humans) {
        println("${human.fullname}: ${human.getPosition()}")
    }
    println("Водитель ${driver.fullname}: ${driver.getPosition()}")
}