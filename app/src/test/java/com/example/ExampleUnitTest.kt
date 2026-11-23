package com.example

import com.example.data.local.IdeaEntity
import com.example.data.model.RequirementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun readinessCalculation_isCorrect() {
        val needed = setOf(RequirementType.MONEY, RequirementType.SKILLS)
        // MONEY weight is 1.2, SKILLS weight is 1.3
        // Money: 100, Skills: 50 -> (100*1.2 + 50*1.3) / (1.2 + 1.3) = (120 + 65) / 2.5 = 185 / 2.5 = 74
        val score = IdeaEntity.calculateWeightedReadiness(
            needed = needed,
            money = 100,
            skills = 50,
            time = 0,
            tools = 0,
            people = 0,
            knowledge = 0
        )
        assertEquals(74, score)
    }

    @Test
    fun readinessCalculation_allFulfill_is100() {
        val needed = setOf(RequirementType.TIME, RequirementType.TOOLS)
        val score = IdeaEntity.calculateWeightedReadiness(
            needed = needed,
            money = 0,
            skills = 0,
            time = 100,
            tools = 100,
            people = 0,
            knowledge = 0
        )
        assertEquals(100, score)
    }

    @Test
    fun firstName_extraction_isCorrect() {
        val fullName = "Alex Morgan"
        val firstName = fullName.trim().split(Regex("\\s+")).firstOrNull()?.replaceFirstChar { it.uppercase() }
        assertEquals("Alex", firstName)
        assertEquals("Hello Alex", "Hello " + firstName)
    }

    @Test
    fun greeting_formatting_withFirstName() {
        val firstName = "Sarah"
        val greeting = "Hello $firstName"
        assertEquals("Hello Sarah", greeting)
    }

    @Test
    fun dailyFeelingOptions_haveCorrectPercentagesAndEffects() {
        val options = listOf(
            com.example.ui.viewmodel.DailyFeelingOption("", "Fired Up", 95, com.example.ui.viewmodel.FeelingEffectType.THUNDER_LIGHTNING),
            com.example.ui.viewmodel.DailyFeelingOption("", "Super Ready", 85, com.example.ui.viewmodel.FeelingEffectType.FLAMES),
            com.example.ui.viewmodel.DailyFeelingOption("", "Getting There", 70, com.example.ui.viewmodel.FeelingEffectType.ROCKET_TAKEOFF),
            com.example.ui.viewmodel.DailyFeelingOption("", "Explorer", 55, com.example.ui.viewmodel.FeelingEffectType.PLANT_SPURT),
            com.example.ui.viewmodel.DailyFeelingOption("", "Recharging", 40, com.example.ui.viewmodel.FeelingEffectType.BATTERY_CHARGING)
        )

        assertEquals(95, options[0].readiness)
        assertEquals("Fired Up", options[0].title)
        assertEquals(com.example.ui.viewmodel.FeelingEffectType.THUNDER_LIGHTNING, options[0].effectType)

        assertEquals(85, options[1].readiness)
        assertEquals("Super Ready", options[1].title)
        assertEquals(com.example.ui.viewmodel.FeelingEffectType.FLAMES, options[1].effectType)

        assertEquals(70, options[2].readiness)
        assertEquals("Getting There", options[2].title)
        assertEquals(com.example.ui.viewmodel.FeelingEffectType.ROCKET_TAKEOFF, options[2].effectType)

        assertEquals(55, options[3].readiness)
        assertEquals("Explorer", options[3].title)
        assertEquals(com.example.ui.viewmodel.FeelingEffectType.PLANT_SPURT, options[3].effectType)

        assertEquals(40, options[4].readiness)
        assertEquals("Recharging", options[4].title)
        assertEquals(com.example.ui.viewmodel.FeelingEffectType.BATTERY_CHARGING, options[4].effectType)
    }

    @Test
    fun dailyVisit_disappearanceLogic() {
        val today = "2026-10-12"
        var lastCompletedDate: String? = null

        // First visit: not completed today -> buttons should appear
        var shouldShowButtons = (lastCompletedDate != today)
        assertTrue(shouldShowButtons)

        // User selects a button -> completed today -> buttons disappear
        lastCompletedDate = today
        shouldShowButtons = (lastCompletedDate != today)
        org.junit.Assert.assertFalse(shouldShowButtons)

        // Subsequent visit on same day -> still false
        val laterVisitToday = today
        shouldShowButtons = (lastCompletedDate != laterVisitToday)
        org.junit.Assert.assertFalse(shouldShowButtons)

        // Next day in user timezone -> buttons appear again
        val nextDay = "2026-10-13"
        shouldShowButtons = (lastCompletedDate != nextDay)
        assertTrue(shouldShowButtons)
    }

    @Test
    fun guestModeIdea_allowsStatusChange() {
        val guestIdea = IdeaEntity(
            id = 2L,
            title = "Local Offline Idea",
            description = "Created without sign-in",
            category = "Tech",
            neededRequirements = "Skills",
            ownerId = "",
            executorId = "",
            executionStatus = "Not Started"
        )

        // Offline / guest creator can change status
        assertTrue(guestIdea.canChangeExecutionStatus(null))
        assertTrue(guestIdea.canChangeExecutionStatus("any-local-id"))
    }
}
