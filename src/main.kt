import kotlin.concurrent.thread
import kotlin.random.Random

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