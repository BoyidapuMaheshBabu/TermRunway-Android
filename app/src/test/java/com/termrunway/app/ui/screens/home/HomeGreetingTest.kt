package com.termrunway.app.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeGreetingTest {

    @Test
    fun morningGreeting() {
        val greeting = getGreeting("Mahesh", 8)
        assertEquals("Good morning, Mahesh! ☀️", greeting.headline)
        assertEquals("Let’s see how your money is doing today.", greeting.subtext)
    }

    @Test
    fun afternoonGreeting() {
        val greeting = getGreeting("Mahesh", 14)
        assertEquals("Good afternoon, Mahesh! 👋", greeting.headline)
        assertEquals("Here’s your money snapshot for today.", greeting.subtext)
    }

    @Test
    fun eveningGreeting() {
        val greeting = getGreeting("Mahesh", 18)
        assertEquals("Good evening, Mahesh! 🌆", greeting.headline)
        assertEquals("Take a quick look at your progress today.", greeting.subtext)
    }

    @Test
    fun nightGreeting() {
        val greeting = getGreeting("Mahesh", 22)
        assertEquals("Good night, Mahesh! 🌙", greeting.headline)
        assertEquals("Here’s where you stand before the day ends.", greeting.subtext)
    }

    @Test
    fun lateNightGreeting() {
        val greeting = getGreeting("Mahesh", 2)
        assertEquals("Good night, Mahesh! 🌙", greeting.headline)
        assertEquals("Here’s where you stand before the day ends.", greeting.subtext)
    }

    @Test
    fun blankNameGreeting() {
        val greeting = getGreeting("", 9)
        assertEquals("Good morning! ☀️", greeting.headline)
        assertEquals("Let’s see how your money is doing today.", greeting.subtext)
    }
}
