package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Idea Matrix", appName)
  }

  @Test
  fun creatorAndAppointedExecutors_canChangeExecutionStatus() {
    val ownerUid = "creator-123"
    val primaryExecutorUid = "executor-primary-456"
    val secondExecutorUid = "executor-second-789"
    val regularContributorUid = "contributor-001"
    val externalUserUid = "stranger-999"

    val members = listOf(
      com.example.data.model.IdeaMember(
        uid = primaryExecutorUid,
        displayName = "Alex",
        role = com.example.data.model.IdeaRole.EXECUTOR
      ),
      com.example.data.model.IdeaMember(
        uid = secondExecutorUid,
        displayName = "Sam",
        role = com.example.data.model.IdeaRole.EXECUTOR
      ),
      com.example.data.model.IdeaMember(
        uid = regularContributorUid,
        displayName = "Charlie",
        role = com.example.data.model.IdeaRole.CONTRIBUTOR
      )
    )

    val idea = com.example.data.local.IdeaEntity(
      id = 1L,
      title = "Autonomous Drone Fleet",
      description = "Fleet management software",
      category = "Tech",
      neededRequirements = "Skills,Time",
      ownerId = ownerUid,
      executorId = primaryExecutorUid,
      membersJson = com.example.data.model.IdeaMember.listToJson(members),
      executionStatus = "Started"
    )

    // Creator can change status
    org.junit.Assert.assertTrue("Creator must be able to change status", idea.canChangeExecutionStatus(ownerUid))

    // Primary executor can change status
    org.junit.Assert.assertTrue("Primary executor must be able to change status", idea.canChangeExecutionStatus(primaryExecutorUid))

    // Secondary appointed executor can change status
    org.junit.Assert.assertTrue("Secondary appointed executor must be able to change status", idea.canChangeExecutionStatus(secondExecutorUid))

    // Regular contributor cannot change status
    org.junit.Assert.assertFalse("Contributor cannot change status", idea.canChangeExecutionStatus(regularContributorUid))

    // External user cannot change status
    org.junit.Assert.assertFalse("External user cannot change status", idea.canChangeExecutionStatus(externalUserUid))
  }

  @Test
  fun executorIds_areExtractedCorrectly() {
    val members = listOf(
      com.example.data.model.IdeaMember(uid = "u1", role = com.example.data.model.IdeaRole.EXECUTOR),
      com.example.data.model.IdeaMember(uid = "u2", role = com.example.data.model.IdeaRole.CONTRIBUTOR),
      com.example.data.model.IdeaMember(uid = "u3", role = com.example.data.model.IdeaRole.EXECUTOR)
    )
    val idea = com.example.data.local.IdeaEntity(
      id = 3L,
      title = "Test Appointed Executors",
      description = "Testing executor UIDs",
      category = "Tech",
      neededRequirements = "Tools",
      ownerId = "owner-99",
      executorId = "u1",
      membersJson = com.example.data.model.IdeaMember.listToJson(members)
    )

    val executorUids = idea.getExecutorUids()
    assertEquals(2, executorUids.size)
    org.junit.Assert.assertTrue(executorUids.contains("u1"))
    org.junit.Assert.assertTrue(executorUids.contains("u3"))
    org.junit.Assert.assertFalse(executorUids.contains("u2"))

    // Test Firestore mapping includes executorIds list
    val map = idea.toFirestoreMap()
    @Suppress("UNCHECKED_CAST")
    val firestoreExecutorIds = map["executorIds"] as? List<String>
    org.junit.Assert.assertNotNull(firestoreExecutorIds)
    assertEquals(2, firestoreExecutorIds?.size)
    org.junit.Assert.assertTrue(firestoreExecutorIds?.contains("u1") == true)
    org.junit.Assert.assertTrue(firestoreExecutorIds?.contains("u3") == true)
  }

  @Test
  fun timingClock_calculatesActiveElapsedAndDaysRemaining() {
    val now = System.currentTimeMillis()
    val dayMillis = 24L * 60L * 60L * 1000L

    val activeIdea = com.example.data.local.IdeaEntity(
      id = 10L,
      title = "AI Code Generator",
      description = "Smart code helper",
      category = "Dev Tools",
      neededRequirements = "Skills,Time",
      timeBoxDays = 60,
      timingStatus = "Active",
      startedAtMillis = now - (15L * dayMillis),
      lastClockStartedAtMillis = now - (3600 * 1000), // 1 hour ago
      accumulatedActiveDurationMillis = 15L * dayMillis
    )

    // Elapsed should be 15 days + 1 hour
    val totalElapsed = activeIdea.getTotalActiveElapsedMillis(now)
    org.junit.Assert.assertTrue(totalElapsed >= (15L * dayMillis + 3600 * 1000))

    // Days remaining should be 60 - 15 = 45 days
    val daysRemaining = activeIdea.getDaysRemaining(now)
    org.junit.Assert.assertNotNull(daysRemaining)
    assertEquals(45, daysRemaining)

    // Not expired yet
    org.junit.Assert.assertFalse(activeIdea.isTimeExpired(now))

    // Test expired idea
    val expiredIdea = activeIdea.copy(
      accumulatedActiveDurationMillis = 61L * dayMillis
    )
    org.junit.Assert.assertTrue("Expired idea should return true", expiredIdea.isTimeExpired(now))
  }

  @Test
  fun reminder_largestGapWeightedByPriority_selectedCorrectly() {
    // Money weight: 1.2, score: 70 -> gap: 30 * 1.2 = 36
    // Skills weight: 1.3, score: 60 -> gap: 40 * 1.3 = 52  <-- Highest weighted gap
    // Time weight: 1.1, score: 60 -> gap: 40 * 1.1 = 44
    val idea = com.example.data.local.IdeaEntity(
      id = 20L,
      title = "Robotics Lab",
      description = "Hardware automation",
      category = "Hardware",
      neededRequirements = "Money,Skills,Time",
      moneyScore = 70,
      skillsScore = 60,
      timeScore = 60
    )

    val largestGap = idea.getLargestGapRequirement()
    org.junit.Assert.assertNotNull(largestGap)
    assertEquals(com.example.data.model.RequirementType.SKILLS, largestGap?.first)
    assertEquals(60, largestGap?.second)
  }

  @Test
  fun sessionLog_serialization_roundtrip() {
    val session1 = com.example.data.model.WorkSessionLog(
      startTimeMillis = 1000L,
      endTimeMillis = 5000L,
      durationMillis = 4000L,
      notes = "Built prototype database schemas"
    )
    val session2 = com.example.data.model.WorkSessionLog(
      startTimeMillis = 6000L,
      endTimeMillis = 12000L,
      durationMillis = 6000L,
      notes = "Implemented reminder scheduling"
    )

    val json = com.example.data.model.WorkSessionLog.listToJson(listOf(session1, session2))
    val decoded = com.example.data.model.WorkSessionLog.listFromJson(json)

    assertEquals(2, decoded.size)
    assertEquals(session1.id, decoded[0].id)
    assertEquals("Built prototype database schemas", decoded[0].notes)
    assertEquals(4000L, decoded[0].durationMillis)
    assertEquals(session2.id, decoded[1].id)
  }

  @Test
  fun auditEntity_jsonListParsers_operateCorrectly() {
    val blockers = listOf(
      "Skills readiness low at 45%",
      "Time box expired — action needed (Extend, Pause, or Shelve)"
    )
    val json = com.example.data.local.AuditEntity.stringListToJson(blockers)
    val parsed = com.example.data.local.AuditEntity.parseStringList(json)

    assertEquals(2, parsed.size)
    assertEquals(blockers[0], parsed[0])
    assertEquals(blockers[1], parsed[1])
  }
}
