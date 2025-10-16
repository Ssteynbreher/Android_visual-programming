package com.example.Movable

import com.example.Movable.Human

class Driver(
    fullname: String,
    age: Int,
    override var speed: Double,
    group_number: Int,
    var direction: String = "right"
) : Human(fullname, age, speed, group_number) {

    override fun move() {
        when (direction) {
            "up" -> this.y += speed
            "down" -> this.y -= speed
            "left" -> this.x -= speed
            "right" -> this.x += speed
        }
        println("Водитель $fullname поехал $direction, позиция: ${this.getPosition()}")
    }

    fun changeDirection(newDir: String) {
        direction = newDir
        println("Водитель $fullname сменил направление на $newDir")
    }
}