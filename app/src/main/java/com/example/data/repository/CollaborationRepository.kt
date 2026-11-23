package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.IdeaEntity
import com.example.data.model.AppNotification
import com.example.data.model.CollaboratorConnection
import com.example.data.model.IdeaActivity
import com.example.data.model.IdeaMember
import com.example.data.model.IdeaRole
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CollaborationRepository(
    private val db: FirebaseFirestore
) {
    companion object {
        private const val TAG = "CollaborationRepo"

        fun create(context: Context): CollaborationRepository {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)
            return CollaborationRepository(db)
        }
    }

    // ==========================================
    // CONNECTIONS & CODE SHARING
    // ==========================================

    suspend fun findUserByCode(code: String): UserProfile? {
        val cleanCode = code.trim().uppercase()
        return try {
            val query = db.collection("users")
                .whereEqualTo("shareCode", cleanCode)
                .limit(1)
                .get()
                .await()
            val doc = query.documents.firstOrNull()
            doc?.data?.let { UserProfile.fromMap(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up user by code: ${e.message}")
            null
        }
    }

    suspend fun sendConnectionRequest(sender: UserProfile, targetCode: String): Result<String> {
        val receiver = findUserByCode(targetCode)
            ?: return Result.failure(Exception("No user found with code $targetCode. Double-check and try again."))

        if (receiver.uid == sender.uid) {
            return Result.failure(Exception("You cannot connect with your own code."))
        }

        return try {
            val connectionId = UUID.randomUUID().toString()
            val connection = CollaboratorConnection(
                id = connectionId,
                senderId = sender.uid,
                senderName = sender.displayName,
                senderPhoto = sender.photoUrl,
                senderEmail = sender.email,
                receiverId = receiver.uid,
                receiverName = receiver.displayName,
                receiverPhoto = receiver.photoUrl,
                receiverEmail = receiver.email,
                receiverCode = targetCode.trim().uppercase(),
                status = "pending",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            db.collection("connections").document(connectionId).set(connection.toMap()).await()

            // Push notification to receiver
            val notifId = UUID.randomUUID().toString()
            val notification = AppNotification(
                id = notifId,
                recipientId = receiver.uid,
                senderId = sender.uid,
                senderName = sender.displayName,
                title = "New Connection Request",
                body = "${sender.displayName} wants to connect with you via code $targetCode",
                type = "connection_request",
                relatedId = connectionId,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            db.collection("users").document(receiver.uid)
                .collection("notifications").document(notifId).set(notification.toMap()).await()

            Result.success("Connection request sent to ${receiver.displayName}!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send connection request", e)
            Result.failure(e)
        }
    }

    suspend fun acceptConnection(connection: CollaboratorConnection, currentUserId: String): Result<Unit> {
        return try {
            db.collection("connections").document(connection.id).update(
                mapOf(
                    "status" to "accepted",
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            // Notify the sender that it was accepted
            val otherUserId = if (connection.senderId == currentUserId) connection.receiverId else connection.senderId
            val notifId = UUID.randomUUID().toString()
            val notification = AppNotification(
                id = notifId,
                recipientId = otherUserId,
                senderId = currentUserId,
                senderName = if (connection.senderId == currentUserId) connection.senderName else connection.receiverName,
                title = "Connection Accepted",
                body = "You are now connected with ${if (connection.senderId == currentUserId) connection.receiverName else connection.senderName}",
                type = "connection_accepted",
                relatedId = connection.id,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            db.collection("users").document(otherUserId)
                .collection("notifications").document(notifId).set(notification.toMap()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to accept connection", e)
            Result.failure(e)
        }
    }

    suspend fun declineConnection(connectionId: String): Result<Unit> {
        return try {
            db.collection("connections").document(connectionId).update(
                mapOf(
                    "status" to "declined",
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decline connection", e)
            Result.failure(e)
        }
    }

    fun observeConnections(userId: String): Flow<List<CollaboratorConnection>> {
        val sentFlow = callbackFlow<List<CollaboratorConnection>> {
            val listener = db.collection("connections")
                .whereEqualTo("senderId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to sent connections", error)
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents?.mapNotNull { doc ->
                        doc.data?.let { CollaboratorConnection.fromMap(doc.id, it) }
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }

        val receivedFlow = callbackFlow<List<CollaboratorConnection>> {
            val listener = db.collection("connections")
                .whereEqualTo("receiverId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to received connections", error)
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents?.mapNotNull { doc ->
                        doc.data?.let { CollaboratorConnection.fromMap(doc.id, it) }
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }

        return combine(sentFlow, receivedFlow) { sent, received ->
            (sent + received).distinctBy { it.id }.sortedByDescending { it.updatedAt }
        }
    }

    // ==========================================
    // COLLABORATIVE IDEAS
    // ==========================================

    fun observeIdeas(userId: String): Flow<List<IdeaEntity>> = callbackFlow {
        val listener = db.collection("ideas")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user ideas", error)
                    return@addSnapshotListener
                }
                val ideas = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { IdeaEntity.fromFirestoreMap(doc.id, it) }
                }?.sortedByDescending { it.updatedAt } ?: emptyList()
                trySend(ideas)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveIdea(idea: IdeaEntity, currentUser: UserProfile): Result<String> {
        return try {
            val docId = if (idea.firestoreId.isNotBlank()) idea.firestoreId else db.collection("ideas").document().id
            val isNew = idea.firestoreId.isBlank()

            val members = idea.getMembersList().toMutableList()
            if (members.none { it.uid == currentUser.uid }) {
                members.add(
                    IdeaMember(
                        uid = currentUser.uid,
                        displayName = currentUser.displayName,
                        email = currentUser.email,
                        photoUrl = currentUser.photoUrl,
                        role = IdeaRole.OWNER
                    )
                )
            }

            val memberIds = members.map { it.uid }.distinct()

            val toSave = idea.copy(
                firestoreId = docId,
                ownerId = if (idea.ownerId.isNotBlank()) idea.ownerId else currentUser.uid,
                ownerName = if (idea.ownerName.isNotBlank()) idea.ownerName else currentUser.displayName,
                ownerPhoto = if (idea.ownerPhoto.isNotBlank()) idea.ownerPhoto else currentUser.photoUrl,
                memberIds = memberIds.joinToString(","),
                membersJson = IdeaMember.listToJson(members),
                updatedAt = System.currentTimeMillis()
            )

            db.collection("ideas").document(docId).set(toSave.toFirestoreMap()).await()

            if (isNew) {
                // Log creation activity
                logActivity(
                    ideaId = docId,
                    user = currentUser,
                    type = "created",
                    message = "${currentUser.displayName} created this idea bet"
                )
            }

            Result.success(docId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save idea", e)
            Result.failure(e)
        }
    }

    suspend fun changeExecutionStatus(
        idea: IdeaEntity,
        newStatus: String,
        actor: UserProfile
    ): Result<Unit> {
        if (!idea.canChangeExecutionStatus(actor.uid)) {
            return Result.failure(Exception("Only the Owner and the Executor can change execution status."))
        }

        return try {
            var docId = idea.firestoreId
            if (docId.isBlank()) {
                val saveRes = saveIdea(idea, actor)
                docId = saveRes.getOrNull() ?: ""
                if (docId.isBlank()) return Result.failure(Exception("Failed to sync idea before updating status"))
            }

            db.collection("ideas").document(docId).update(
                mapOf(
                    "executionStatus" to newStatus,
                    "status" to if (newStatus == "Live") "Live in Market" else if (newStatus == "Done") "Ready to Push" else "In Progress",
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            // Activity Log
            logActivity(
                ideaId = docId,
                user = actor,
                type = "status_change",
                message = "${actor.displayName} updated execution status to '$newStatus'"
            )

            // Notify other members
            notifyIdeaMembers(
                idea = idea,
                sender = actor,
                title = "Execution Status: $newStatus",
                body = "${actor.displayName} set ${idea.title} to '$newStatus'"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to change execution status", e)
            Result.failure(e)
        }
    }

    suspend fun inviteCollaboratorToIdea(
        idea: IdeaEntity,
        collaborator: UserProfile,
        asExecutor: Boolean,
        owner: UserProfile
    ): Result<Unit> {
        if (!idea.isOwner(owner.uid)) {
            return Result.failure(Exception("Only the Owner can invite members."))
        }

        return try {
            val docId = idea.firestoreId
            val currentMembers = idea.getMembersList().toMutableList()

            var newExecutorId = idea.executorId
            var newExecutorName = idea.executorName

            if (asExecutor) {
                newExecutorId = collaborator.uid
                newExecutorName = collaborator.displayName
            }

            // Remove if already present, then add with new role
            currentMembers.removeAll { it.uid == collaborator.uid }
            currentMembers.add(
                IdeaMember(
                    uid = collaborator.uid,
                    displayName = collaborator.displayName,
                    email = collaborator.email,
                    photoUrl = collaborator.photoUrl,
                    role = if (asExecutor) IdeaRole.EXECUTOR else IdeaRole.CONTRIBUTOR
                )
            )

            val memberIds = currentMembers.map { it.uid }.distinct()
            val executorUids = currentMembers.filter { it.role == IdeaRole.EXECUTOR }.map { it.uid }

            db.collection("ideas").document(docId).update(
                mapOf(
                    "memberIds" to memberIds,
                    "membersJson" to IdeaMember.listToJson(currentMembers),
                    "executorId" to newExecutorId,
                    "executorName" to newExecutorName,
                    "executorIds" to executorUids,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            // Activity
            val roleName = if (asExecutor) "Executor" else "Contributor"
            logActivity(
                ideaId = docId,
                user = owner,
                type = "member_joined",
                message = "${owner.displayName} added ${collaborator.displayName} as $roleName"
            )

            // Notification to invited collaborator
            val notifId = UUID.randomUUID().toString()
            val notification = AppNotification(
                id = notifId,
                recipientId = collaborator.uid,
                senderId = owner.uid,
                senderName = owner.displayName,
                title = "Added to Idea: ${idea.title}",
                body = "You were added as $roleName on '${idea.title}' by ${owner.displayName}",
                type = "idea_invite",
                relatedId = docId,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            db.collection("users").document(collaborator.uid)
                .collection("notifications").document(notifId).set(notification.toMap()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invite collaborator", e)
            Result.failure(e)
        }
    }

    suspend fun setExecutor(
        idea: IdeaEntity,
        newExecutorUid: String,
        owner: UserProfile
    ): Result<Unit> {
        if (!idea.isOwner(owner.uid)) {
            return Result.failure(Exception("Only the Creator can appoint Executors."))
        }

        return try {
            val docId = idea.firestoreId
            val currentMembers = idea.getMembersList().toMutableList()
            var executorName = ""

            for (i in 0 until currentMembers.size) {
                if (currentMembers[i].uid == newExecutorUid) {
                    currentMembers[i] = currentMembers[i].copy(role = IdeaRole.EXECUTOR)
                    executorName = currentMembers[i].displayName
                }
            }

            val executorUids = currentMembers.filter { it.role == IdeaRole.EXECUTOR }.map { it.uid }

            db.collection("ideas").document(docId).update(
                mapOf(
                    "executorId" to newExecutorUid,
                    "executorName" to executorName,
                    "executorIds" to executorUids,
                    "membersJson" to IdeaMember.listToJson(currentMembers),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            logActivity(
                ideaId = docId,
                user = owner,
                type = "executor_assigned",
                message = "${owner.displayName} appointed $executorName as Executor"
            )

            // Notify new executor
            val notifId = UUID.randomUUID().toString()
            val notification = AppNotification(
                id = notifId,
                recipientId = newExecutorUid,
                senderId = owner.uid,
                senderName = owner.displayName,
                title = "Appointed as Executor!",
                body = "You are now an appointed Executor for '${idea.title}'. You can advance its execution lifecycle.",
                type = "role_changed",
                relatedId = docId,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            db.collection("users").document(newExecutorUid)
                .collection("notifications").document(notifId).set(notification.toMap()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to appoint executor", e)
            Result.failure(e)
        }
    }

    suspend fun demoteExecutor(
        idea: IdeaEntity,
        memberUid: String,
        owner: UserProfile
    ): Result<Unit> {
        if (!idea.isOwner(owner.uid)) {
            return Result.failure(Exception("Only the Creator can change appointed Executors."))
        }

        return try {
            val docId = idea.firestoreId
            val currentMembers = idea.getMembersList().toMutableList()
            var demotedName = ""

            for (i in 0 until currentMembers.size) {
                if (currentMembers[i].uid == memberUid) {
                    currentMembers[i] = currentMembers[i].copy(role = IdeaRole.CONTRIBUTOR)
                    demotedName = currentMembers[i].displayName
                }
            }

            val remainingExecutors = currentMembers.filter { it.role == IdeaRole.EXECUTOR }
            val newPrimaryExecutor = remainingExecutors.firstOrNull()
            val executorUids = remainingExecutors.map { it.uid }

            db.collection("ideas").document(docId).update(
                mapOf(
                    "executorId" to (newPrimaryExecutor?.uid ?: ""),
                    "executorName" to (newPrimaryExecutor?.displayName ?: ""),
                    "executorIds" to executorUids,
                    "membersJson" to IdeaMember.listToJson(currentMembers),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            logActivity(
                ideaId = docId,
                user = owner,
                type = "role_changed",
                message = "${owner.displayName} changed $demotedName to Contributor"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to demote executor", e)
            Result.failure(e)
        }
    }

    suspend fun removeMember(
        idea: IdeaEntity,
        memberUid: String,
        owner: UserProfile
    ): Result<Unit> {
        if (!idea.isOwner(owner.uid)) {
            return Result.failure(Exception("Only the Owner can remove members."))
        }
        if (memberUid == owner.uid) {
            return Result.failure(Exception("Owner cannot be removed from the idea."))
        }

        return try {
            val docId = idea.firestoreId
            val currentMembers = idea.getMembersList().toMutableList()
            val removedMember = currentMembers.firstOrNull { it.uid == memberUid }
            currentMembers.removeAll { it.uid == memberUid }

            val memberIds = currentMembers.map { it.uid }.distinct()
            val newExecutorId = if (idea.executorId == memberUid) "" else idea.executorId
            val newExecutorName = if (idea.executorId == memberUid) "" else idea.executorName

            db.collection("ideas").document(docId).update(
                mapOf(
                    "memberIds" to memberIds,
                    "membersJson" to IdeaMember.listToJson(currentMembers),
                    "executorId" to newExecutorId,
                    "executorName" to newExecutorName,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            logActivity(
                ideaId = docId,
                user = owner,
                type = "member_removed",
                message = "${owner.displayName} removed ${removedMember?.displayName ?: "member"} from the idea"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove member", e)
            Result.failure(e)
        }
    }

    suspend fun deleteIdea(idea: IdeaEntity, currentUserId: String): Result<Unit> {
        if (!idea.isOwner(currentUserId)) {
            return Result.failure(Exception("Only the Owner can delete this idea."))
        }

        return try {
            if (idea.firestoreId.isNotBlank()) {
                db.collection("ideas").document(idea.firestoreId).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete idea", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // ACTIVITY FEED & COMMENTS
    // ==========================================

    fun observeActivities(ideaId: String): Flow<List<IdeaActivity>> = callbackFlow {
        if (ideaId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("ideas").document(ideaId)
            .collection("activities")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to activities", error)
                    return@addSnapshotListener
                }
                val activities = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { IdeaActivity.fromMap(doc.id, it) }
                } ?: emptyList()
                trySend(activities)
            }
        awaitClose { listener.remove() }
    }

    suspend fun postComment(
        ideaId: String,
        commentText: String,
        author: UserProfile
    ): Result<Unit> {
        val clean = commentText.trim()
        if (clean.isBlank()) return Result.failure(Exception("Comment cannot be empty"))

        return try {
            logActivity(
                ideaId = ideaId,
                user = author,
                type = "comment",
                message = clean
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post comment", e)
            Result.failure(e)
        }
    }

    private suspend fun logActivity(
        ideaId: String,
        user: UserProfile,
        type: String,
        message: String
    ) {
        try {
            val actId = UUID.randomUUID().toString()
            val activity = IdeaActivity(
                id = actId,
                ideaId = ideaId,
                userId = user.uid,
                userName = user.displayName,
                userPhoto = user.photoUrl,
                type = type,
                message = message,
                timestamp = System.currentTimeMillis()
            )
            db.collection("ideas").document(ideaId)
                .collection("activities").document(actId)
                .set(activity.toMap()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log activity: ${e.message}")
        }
    }

    private suspend fun notifyIdeaMembers(
        idea: IdeaEntity,
        sender: UserProfile,
        title: String,
        body: String
    ) {
        val membersToNotify = idea.getMemberIdsList().filter { it != sender.uid }
        membersToNotify.forEach { recipientUid ->
            try {
                val notifId = UUID.randomUUID().toString()
                val notification = AppNotification(
                    id = notifId,
                    recipientId = recipientUid,
                    senderId = sender.uid,
                    senderName = sender.displayName,
                    title = title,
                    body = body,
                    type = "status_changed",
                    relatedId = idea.firestoreId,
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                db.collection("users").document(recipientUid)
                    .collection("notifications").document(notifId)
                    .set(notification.toMap()).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to notify member $recipientUid: ${e.message}")
            }
        }
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================

    fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to notifications", error)
                    return@addSnapshotListener
                }
                val notifs = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { AppNotification.fromMap(doc.id, it) }
                } ?: emptyList()
                trySend(notifs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markNotificationRead(userId: String, notificationId: String) {
        try {
            db.collection("users").document(userId)
                .collection("notifications").document(notificationId)
                .update("isRead", true).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark notification read", e)
        }
    }
}
