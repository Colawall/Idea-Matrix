package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ExecutionStep
import com.example.data.model.IdeaMember
import com.example.data.model.IdeaResource
import com.example.data.model.IdeaRole
import com.example.data.model.ReminderConfig
import com.example.data.model.RequirementType
import com.example.data.model.TimingStatus
import com.example.data.model.WorkSessionLog
import kotlin.math.roundToInt

@Entity(tableName = "ideas")
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firestoreId: String = "",
    val title: String,
    val description: String,
    val category: String,
    val neededRequirements: String, // Comma separated: e.g. "Money,Skills,Time"
    val moneyScore: Int = 80,
    val skillsScore: Int = 80,
    val timeScore: Int = 80,
    val toolsScore: Int = 80,
    val peopleScore: Int = 80,
    val knowledgeScore: Int = 80,
    val readinessScore: Int = 80,
    val status: String = "Ready to Push",
    val executionStatus: String = "Not Started", // "Not Started", "Started", "Done", "Live"
    val isFeatured: Boolean = false,
    val nextAction: String = "Draft first prototype",
    val stepsJson: String = "[]",
    val resourcesJson: String = "[]",
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerPhoto: String = "",
    val executorId: String = "",
    val executorName: String = "",
    val memberIds: String = "", // Comma separated user IDs
    val membersJson: String = "[]",
    val timingStatus: String = "Planning", // "Planning", "Active", "Paused", "Done"
    val timeBoxDays: Int? = null,
    val targetDateMillis: Long? = null,
    val startedAtMillis: Long? = null,
    val lastClockStartedAtMillis: Long? = null,
    val accumulatedActiveDurationMillis: Long = 0L,
    val reminderConfigJson: String = "{}",
    val sessionLogsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getExecutionSteps(): List<ExecutionStep> {
        return ExecutionStep.listFromJson(stepsJson)
    }

    fun getResourcesList(): List<IdeaResource> {
        return IdeaResource.listFromJson(resourcesJson)
    }

    fun getMembersList(): List<IdeaMember> {
        return IdeaMember.listFromJson(membersJson)
    }

    fun getMemberIdsList(): List<String> {
        return if (memberIds.isBlank()) emptyList() else memberIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun getExecutorUids(): List<String> {
        val list = mutableListOf<String>()
        if (executorId.isNotBlank()) {
            list.add(executorId)
        }
        getMembersList().filter { it.role == IdeaRole.EXECUTOR }.forEach {
            if (it.uid.isNotBlank() && !list.contains(it.uid)) {
                list.add(it.uid)
            }
        }
        return list
    }

    fun isOwner(uid: String?): Boolean =
        ownerId.isBlank() || (!uid.isNullOrBlank() && uid == ownerId)

    fun isExecutor(uid: String?): Boolean =
        !uid.isNullOrBlank() && (uid == executorId || getExecutorUids().contains(uid))

    fun isMember(uid: String?): Boolean =
        ownerId.isBlank() || (!uid.isNullOrBlank() && (isOwner(uid) || isExecutor(uid) || getMemberIdsList().contains(uid)))

    fun canChangeExecutionStatus(uid: String?): Boolean =
        ownerId.isBlank() || isOwner(uid) || isExecutor(uid)

    fun getUserRole(uid: String?): IdeaRole {
        return when {
            isOwner(uid) -> IdeaRole.OWNER
            isExecutor(uid) -> IdeaRole.EXECUTOR
            isMember(uid) -> IdeaRole.CONTRIBUTOR
            else -> if (ownerId.isBlank()) IdeaRole.OWNER else IdeaRole.NONE
        }
    }

    fun getRequirementsSet(): Set<RequirementType> {
        return RequirementType.parseCommaSeparated(neededRequirements)
    }

    fun getScoreFor(type: RequirementType): Int {
        return when (type) {
            RequirementType.MONEY -> moneyScore
            RequirementType.SKILLS -> skillsScore
            RequirementType.TIME -> timeScore
            RequirementType.TOOLS -> toolsScore
            RequirementType.PEOPLE -> peopleScore
            RequirementType.KNOWLEDGE -> knowledgeScore
        }
    }

    fun getTimingStatusEnum(): TimingStatus = TimingStatus.fromString(timingStatus)

    fun getTotalActiveElapsedMillis(now: Long = System.currentTimeMillis()): Long {
        return if (getTimingStatusEnum() == TimingStatus.ACTIVE && lastClockStartedAtMillis != null && lastClockStartedAtMillis > 0) {
            accumulatedActiveDurationMillis + (now - lastClockStartedAtMillis).coerceAtLeast(0L)
        } else {
            accumulatedActiveDurationMillis
        }
    }

    fun getPlannedDurationMillis(): Long? {
        return if (targetDateMillis != null && targetDateMillis > 0) {
            val base = startedAtMillis ?: createdAt
            (targetDateMillis - base).coerceAtLeast(24L * 60L * 60L * 1000L)
        } else if (timeBoxDays != null && timeBoxDays > 0) {
            timeBoxDays * 24L * 60L * 60L * 1000L
        } else {
            null
        }
    }

    fun getDaysRemaining(now: Long = System.currentTimeMillis()): Int? {
        return if (targetDateMillis != null && targetDateMillis > 0) {
            val diff = targetDateMillis - now
            (diff / (24L * 60L * 60L * 1000L)).toInt()
        } else if (timeBoxDays != null && timeBoxDays > 0) {
            val elapsedMillis = getTotalActiveElapsedMillis(now)
            val elapsedDays = (elapsedMillis / (24L * 60L * 60L * 1000L)).toInt()
            timeBoxDays - elapsedDays
        } else {
            null
        }
    }

    fun isTimeExpired(now: Long = System.currentTimeMillis()): Boolean {
        if (getTimingStatusEnum() == TimingStatus.DONE) return false
        val days = getDaysRemaining(now)
        if (days != null && days <= 0 && getTimingStatusEnum() != TimingStatus.PLANNING) return true
        val planned = getPlannedDurationMillis()
        if (planned != null && planned > 0 && getTimingStatusEnum() != TimingStatus.PLANNING) {
            return getTotalActiveElapsedMillis(now) >= planned
        }
        return false
    }

    fun getElapsedVsPlannedProgress(now: Long = System.currentTimeMillis()): Float {
        val planned = getPlannedDurationMillis() ?: return 0f
        if (planned <= 0) return 0f
        val elapsed = getTotalActiveElapsedMillis(now)
        return (elapsed.toFloat() / planned.toFloat()).coerceIn(0f, 1f)
    }

    fun getReminderConfig(): ReminderConfig = ReminderConfig.fromJson(reminderConfigJson)

    fun getSessionLogs(): List<WorkSessionLog> = WorkSessionLog.listFromJson(sessionLogsJson)

    fun getLargestGapRequirement(): Pair<RequirementType, Int>? {
        val neededSet = getRequirementsSet()
        val typesToEvaluate = if (neededSet.isNotEmpty()) neededSet else RequirementType.entries.toSet()
        var maxGap = -1f
        var bestType: RequirementType? = null
        var bestScore = 0

        for (type in typesToEvaluate) {
            val score = getScoreFor(type)
            val gap = (100 - score) * type.defaultWeight
            if (gap > maxGap) {
                maxGap = gap
                bestType = type
                bestScore = score
            }
        }
        return if (bestType != null) Pair(bestType, bestScore) else null
    }

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "description" to description,
            "category" to category,
            "neededRequirements" to neededRequirements,
            "moneyScore" to moneyScore,
            "skillsScore" to skillsScore,
            "timeScore" to timeScore,
            "toolsScore" to toolsScore,
            "peopleScore" to peopleScore,
            "knowledgeScore" to knowledgeScore,
            "readinessScore" to readinessScore,
            "status" to status,
            "executionStatus" to executionStatus,
            "isFeatured" to isFeatured,
            "nextAction" to nextAction,
            "stepsJson" to stepsJson,
            "resourcesJson" to resourcesJson,
            "ownerId" to ownerId,
            "ownerName" to ownerName,
            "ownerPhoto" to ownerPhoto,
            "executorId" to executorId,
            "executorName" to executorName,
            "executorIds" to getExecutorUids(),
            "memberIds" to getMemberIdsList(),
            "membersJson" to membersJson,
            "timingStatus" to timingStatus,
            "timeBoxDays" to timeBoxDays,
            "targetDateMillis" to targetDateMillis,
            "startedAtMillis" to startedAtMillis,
            "lastClockStartedAtMillis" to lastClockStartedAtMillis,
            "accumulatedActiveDurationMillis" to accumulatedActiveDurationMillis,
            "reminderConfigJson" to reminderConfigJson,
            "sessionLogsJson" to sessionLogsJson,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromFirestoreMap(id: String, data: Map<String, Any?>): IdeaEntity {
            val memberIdsRaw = data["memberIds"]
            val memberIdsString = when (memberIdsRaw) {
                is List<*> -> memberIdsRaw.filterIsInstance<String>().joinToString(",")
                is String -> memberIdsRaw
                else -> ""
            }

            val executorIdsRaw = (data["executorIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val primaryExecutorId = (data["executorId"] as? String)?.ifBlank { null } ?: executorIdsRaw.firstOrNull() ?: ""

            return IdeaEntity(
                firestoreId = id,
                title = data["title"] as? String ?: "Untitled",
                description = data["description"] as? String ?: "",
                category = data["category"] as? String ?: "General",
                neededRequirements = data["neededRequirements"] as? String ?: "",
                moneyScore = (data["moneyScore"] as? Number)?.toInt() ?: 80,
                skillsScore = (data["skillsScore"] as? Number)?.toInt() ?: 80,
                timeScore = (data["timeScore"] as? Number)?.toInt() ?: 80,
                toolsScore = (data["toolsScore"] as? Number)?.toInt() ?: 80,
                peopleScore = (data["peopleScore"] as? Number)?.toInt() ?: 80,
                knowledgeScore = (data["knowledgeScore"] as? Number)?.toInt() ?: 80,
                readinessScore = (data["readinessScore"] as? Number)?.toInt() ?: 80,
                status = data["status"] as? String ?: "Ready to Push",
                executionStatus = data["executionStatus"] as? String ?: "Not Started",
                isFeatured = data["isFeatured"] as? Boolean ?: false,
                nextAction = data["nextAction"] as? String ?: "",
                stepsJson = data["stepsJson"] as? String ?: "[]",
                resourcesJson = data["resourcesJson"] as? String ?: "[]",
                ownerId = data["ownerId"] as? String ?: "",
                ownerName = data["ownerName"] as? String ?: "",
                ownerPhoto = data["ownerPhoto"] as? String ?: "",
                executorId = primaryExecutorId,
                executorName = data["executorName"] as? String ?: "",
                memberIds = memberIdsString,
                membersJson = data["membersJson"] as? String ?: "[]",
                timingStatus = data["timingStatus"] as? String ?: "Planning",
                timeBoxDays = (data["timeBoxDays"] as? Number)?.toInt(),
                targetDateMillis = (data["targetDateMillis"] as? Number)?.toLong(),
                startedAtMillis = (data["startedAtMillis"] as? Number)?.toLong(),
                lastClockStartedAtMillis = (data["lastClockStartedAtMillis"] as? Number)?.toLong(),
                accumulatedActiveDurationMillis = (data["accumulatedActiveDurationMillis"] as? Number)?.toLong() ?: 0L,
                reminderConfigJson = data["reminderConfigJson"] as? String ?: "{}",
                sessionLogsJson = data["sessionLogsJson"] as? String ?: "[]",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }

        fun calculateWeightedReadiness(
            needed: Set<RequirementType>,
            money: Int,
            skills: Int,
            time: Int,
            tools: Int,
            people: Int,
            knowledge: Int
        ): Int {
            if (needed.isEmpty()) {
                val sum = money + skills + time + tools + people + knowledge
                return (sum / 6.0).roundToInt().coerceIn(0, 100)
            }
            var weightedSum = 0f
            var weightSum = 0f

            for (type in needed) {
                val score = when (type) {
                    RequirementType.MONEY -> money
                    RequirementType.SKILLS -> skills
                    RequirementType.TIME -> time
                    RequirementType.TOOLS -> tools
                    RequirementType.PEOPLE -> people
                    RequirementType.KNOWLEDGE -> knowledge
                }
                weightedSum += score * type.defaultWeight
                weightSum += type.defaultWeight
            }

            return if (weightSum > 0f) {
                (weightedSum / weightSum).roundToInt().coerceIn(0, 100)
            } else {
                50
            }
        }
    }
}
