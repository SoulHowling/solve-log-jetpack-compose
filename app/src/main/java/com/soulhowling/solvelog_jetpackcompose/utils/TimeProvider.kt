package com.soulhowling.solvelog_jetpackcompose.utils

interface TimeProvider {
    fun currentTimeMillis(): Long
}

object DefaultTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
