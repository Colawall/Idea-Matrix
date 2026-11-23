package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.util.UUID

data class ExecutionStep(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("isCompleted", isCompleted)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): ExecutionStep {
            return ExecutionStep(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", ""),
                isCompleted = obj.optBoolean("isCompleted", false)
            )
        }

        fun listToJson(steps: List<ExecutionStep>): String {
            val array = JSONArray()
            steps.forEach { array.put(it.toJsonObject()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String): List<ExecutionStep> {
            if (jsonStr.isBlank()) return emptyList()
            val list = mutableListOf<ExecutionStep>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(fromJsonObject(array.getJSONObject(i)))
                }
            } catch (_: Exception) {}
            return list
        }
    }
}

data class IdeaResource(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val urlOrNote: String = "",
    val type: String = "Link" // "URL", "Doc", "PDF", "Tool", "Note"
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("urlOrNote", urlOrNote)
            put("type", type)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): IdeaResource {
            return IdeaResource(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", ""),
                urlOrNote = obj.optString("urlOrNote", ""),
                type = obj.optString("type", "Link")
            )
        }

        fun listToJson(resources: List<IdeaResource>): String {
            val array = JSONArray()
            resources.forEach { array.put(it.toJsonObject()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String): List<IdeaResource> {
            if (jsonStr.isBlank()) return emptyList()
            val list = mutableListOf<IdeaResource>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(fromJsonObject(array.getJSONObject(i)))
                }
            } catch (_: Exception) {}
            return list
        }
    }
}

enum class IdeaRole(val label: String) {
    OWNER("Owner"),
    EXECUTOR("Executor"),
    CONTRIBUTOR("Contributor"),
    NONE("Viewer");

    companion object {
        fun fromString(value: String): IdeaRole {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } ?: CONTRIBUTOR
        }
    }
}

data class IdeaMember(
    val uid: String,
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: IdeaRole = IdeaRole.CONTRIBUTOR
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("uid", uid)
            put("displayName", displayName)
            put("email", email)
            put("photoUrl", photoUrl)
            put("role", role.name)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): IdeaMember {
            return IdeaMember(
                uid = obj.optString("uid", ""),
                displayName = obj.optString("displayName", ""),
                email = obj.optString("email", ""),
                photoUrl = obj.optString("photoUrl", ""),
                role = IdeaRole.fromString(obj.optString("role", "CONTRIBUTOR"))
            )
        }

        fun listToJson(members: List<IdeaMember>): String {
            val array = JSONArray()
            members.forEach { array.put(it.toJsonObject()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String): List<IdeaMember> {
            if (jsonStr.isBlank()) return emptyList()
            val list = mutableListOf<IdeaMember>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(fromJsonObject(array.getJSONObject(i)))
                }
            } catch (_: Exception) {}
            return list
        }
    }
}

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val shareCode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl,
            "shareCode" to shareCode,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(data: Map<String, Any?>): UserProfile {
            return UserProfile(
                uid = data["uid"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String ?: "",
                shareCode = data["shareCode"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class CollaboratorConnection(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val senderEmail: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverPhoto: String = "",
    val receiverEmail: String = "",
    val receiverCode: String = "",
    val status: String = "pending", // "pending", "accepted", "declined"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderPhoto" to senderPhoto,
            "senderEmail" to senderEmail,
            "receiverId" to receiverId,
            "receiverName" to receiverName,
            "receiverPhoto" to receiverPhoto,
            "receiverEmail" to receiverEmail,
            "receiverCode" to receiverCode,
            "status" to status,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any?>): CollaboratorConnection {
            return CollaboratorConnection(
                id = id,
                senderId = data["senderId"] as? String ?: "",
                senderName = data["senderName"] as? String ?: "",
                senderPhoto = data["senderPhoto"] as? String ?: "",
                senderEmail = data["senderEmail"] as? String ?: "",
                receiverId = data["receiverId"] as? String ?: "",
                receiverName = data["receiverName"] as? String ?: "",
                receiverPhoto = data["receiverPhoto"] as? String ?: "",
                receiverEmail = data["receiverEmail"] as? String ?: "",
                receiverCode = data["receiverCode"] as? String ?: "",
                status = data["status"] as? String ?: "pending",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class IdeaActivity(
    val id: String = UUID.randomUUID().toString(),
    val ideaId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val type: String = "comment", // "comment", "status_change", "member_joined", "executor_assigned", "step_update", "resource_update"
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "ideaId" to ideaId,
            "userId" to userId,
            "userName" to userName,
            "userPhoto" to userPhoto,
            "type" to type,
            "message" to message,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any?>): IdeaActivity {
            return IdeaActivity(
                id = id,
                ideaId = data["ideaId"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                userName = data["userName"] as? String ?: "",
                userPhoto = data["userPhoto"] as? String ?: "",
                type = data["type"] as? String ?: "comment",
                message = data["message"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "general", // "connection_request", "connection_accepted", "idea_invite", "role_changed", "status_changed"
    val relatedId: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "recipientId" to recipientId,
            "senderId" to senderId,
            "senderName" to senderName,
            "title" to title,
            "body" to body,
            "type" to type,
            "relatedId" to relatedId,
            "isRead" to isRead,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any?>): AppNotification {
            return AppNotification(
                id = id,
                recipientId = data["recipientId"] as? String ?: "",
                senderId = data["senderId"] as? String ?: "",
                senderName = data["senderName"] as? String ?: "",
                title = data["title"] as? String ?: "",
                body = data["body"] as? String ?: "",
                type = data["type"] as? String ?: "general",
                relatedId = data["relatedId"] as? String ?: "",
                isRead = data["isRead"] as? Boolean ?: false,
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

object ShareCodeUtils {
    // Unambiguous characters: exclude 0, O, 1, I, L to avoid user confusion
    private const val ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
    private val random = SecureRandom()

    fun generateCode(): String {
        val sb = StringBuilder("IM-")
        repeat(6) {
            val idx = random.nextInt(ALPHABET.length)
            sb.append(ALPHABET[idx])
        }
        return sb.toString()
    }

    fun isValid(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        if (!trimmed.startsWith("IM-") || trimmed.length != 9) return false
        val suffix = trimmed.substring(3)
        return suffix.all { it in ALPHABET }
    }
}
