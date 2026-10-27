package com.miladrostami.moed.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AlarmSchedulerTest {
    @Test fun reminderIdsAreStableAndUniqueAcrossSlotsAndModes() {
        val notification = AlarmScheduler.reminderId(42, 3, 1, false)
        assertEquals(notification, AlarmScheduler.reminderId(42, 3, 1, false))
        assertNotEquals(notification, AlarmScheduler.reminderId(42, 3, 2, false))
        assertNotEquals(notification, AlarmScheduler.reminderId(42, 3, 1, true))
    }
}
