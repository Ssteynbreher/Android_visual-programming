import kotlin.random.Random

class Human(
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

    fun move() {
        val directionX = Random.nextDouble(-1.0, 1.0)
        val directionY = Random.nextDouble(-1.0, 1.0)

        x += directionX * speed
        y += directionY * speed

        println("$fullname переместился в позицию: (${"%.2f".format(x)}, ${"%.2f".format(y)})")
    }

    fun getPosition(): String = "(${"%.2f".format(x)}, ${"%.2f".format(y)})"
}

fun main() {
    val humans = arrayOf(
        Human("Штейнбрехер Софья Владимировна", 20, 1.0, group_number = 433),
        Human("Петров Петр Петрович", 25, 1.5, group_number = 43),
        Human("Сидорова Анна Сергеевна", 18, 0.8, group_number = 25),
        Human("Козлов Михаил Юрьевич", 30, 1.2, group_number = 636),
        Human("Смирнова Елена Викторовна", 22, 1.1, group_number = 71),
        Human("Васильев Алексей Игоревич", 27, 1.3, group_number = 75),
        Human("Морозова Ольга Павловна", 19, 0.9, group_number = 85),
        Human("Ковалев Сергей Викторович", 32, 1.4, group_number = 88),
        Human("Попова Мария Александровна", 21, 1.0, group_number = 255),
        Human("Лебедев Дмитрий Олегович", 26, 1.2, group_number = 637),
        Human("Кузнецова Светлана Игоревна", 23, 1.1, group_number = 774),
        Human("Федоров Артем Сергеевич", 28, 1.5, group_number = 44),
        Human("Зайцева Юлия Викторовна", 20, 0.7, group_number = 55),
        Human("Белов Андрей Михайлович", 31, 1.3, group_number = 66),
        Human("Соколова Екатерина Петровна", 24, 1.0, group_number = 77),
        Human("Егоров Павел Андреевич", 29, 1.4, group_number = 99),
        Human("Лазарева Анастасия Ивановна", 22, 0.9, group_number = 456),
        Human("Григорьев Олег Сергеевич", 27, 1.2, group_number = 789),
        Human("Волкова Ирина Юрьевна", 19, 0.8, group_number = 123),
        Human("Николаев Виктор Павлович", 33, 1.5, group_number = 31),
        Human("Тихонова Дарья Алексеевна", 21, 1.0, group_number = 223),
        Human("Михайлов Игорь Денисович", 26, 1.3, group_number = 668),
        Human("Романова Алина Олеговна", 23, 1.1, group_number = 993),
        Human("Степанов Борис Викторович", 30, 1.4, group_number = 552),
        Human("Орлова Наталья Сергеевна", 20, 0.9, group_number = 112),
        Human("Крылов Арсений Иванович", 25, 1.2, group_number = 333)
    )

    val time = 10

    println("\n=== НАЧАЛО СИМУЛЯЦИИ ===")

    for (second in 1..time) {
        println("\n--- Секунда $second ---")

        for (human in humans) {
            human.move()
        }
    }

    println("\nКонец стимуляции")
    println("\nФинальные позиции:")
    for (human in humans) {
        println("${human.fullname}: ${human.getPosition()}")
    }
}