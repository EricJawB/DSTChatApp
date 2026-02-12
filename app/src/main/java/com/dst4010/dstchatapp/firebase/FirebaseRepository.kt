package com.dst4010.dstchatapp.firebase

import android.net.Uri
import com.dst4010.dstchatapp.data.ChatMessage
import com.dst4010.dstchatapp.data.ChatRoom
import com.dst4010.dstchatapp.data.UserProfile
import com.dst4010.dstchatapp.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

class FirebaseRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val storage = FirebaseStorage.getInstance().reference

    private val nameCache = ConcurrentHashMap<String, String>()

    fun currentUid(): String? = auth.currentUser?.uid
    fun isSignedIn(): Boolean = auth.currentUser != null

    suspend fun signIn(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Login failed", e)
    }

    suspend fun register(displayName: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = result.user?.uid ?: return Result.Error("Registration failed: missing uid")

            val profile = hashMapOf(
                "displayName" to displayName.trim(),
                "email" to email.trim(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.collection("users").document(uid).set(profile, SetOptions.merge()).await()
            nameCache[uid] = displayName.trim()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Registration failed", e)
        }
    }
    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun getCurrentUserProfile(): UserProfile? {
        val uid = currentUid() ?: return null
        val snap = db.collection("users").document(uid).get().await()
        val displayName = snap.getString("displayName") ?: (auth.currentUser?.email ?: "User")
        nameCache[uid] = displayName
        return UserProfile(
            uid = uid,
            displayName = displayName,
            email = snap.getString("email") ?: (auth.currentUser?.email ?: ""),
            createdAt = snap.getTimestamp("createdAt")
        )
    }

    fun listenRooms(onUpdate: (List<ChatRoom>) -> Unit, onError: (String) -> Unit): () -> Unit {
        val reg = db.collection("rooms")
            .orderBy("createdAt")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Room listener error")
                    return@addSnapshotListener
                }
                val rooms = snap?.documents?.mapNotNull { it.toRoom() } ?: emptyList()
                onUpdate(rooms)
            }
        return { reg.remove() }
    }

    suspend fun createRoom(name: String): Result<Unit> {
        return try {
            val uid = currentUid() ?: return Result.Error("Not signed in")

            val data = hashMapOf(
                "name" to name.trim(),
                "createdAt" to FieldValue.serverTimestamp(),
                "createdBy" to uid,
                "typing" to hashMapOf<String, Boolean>()
            )

            db.collection("rooms").add(data).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create room", e)
        }
    }

    fun listenMessages(roomId: String, onUpdate: (List<ChatMessage>) -> Unit, onError: (String) -> Unit): () -> Unit {
        val reg = db.collection("rooms").document(roomId)
            .collection("messages")
            .orderBy("sentAt")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Message listener error")
                    return@addSnapshotListener
                }
                val msgs = snap?.documents?.mapNotNull { it.toMessage() } ?: emptyList()
                onUpdate(msgs)
            }
        return { reg.remove() }
    }

    suspend fun sendText(roomId: String, text: String): Result<Unit> {
        return try {
            val uid = currentUid() ?: return Result.Error("Not signed in")
            val senderName = getOrFetchDisplayName(uid)

            val msg = hashMapOf(
                "senderId" to uid,
                "senderName" to senderName,
                "type" to ChatMessage.TYPE_TEXT,
                "text" to text.trim(),
                "imageUrl" to null,
                "sentAt" to FieldValue.serverTimestamp()
            )

            db.collection("rooms").document(roomId).collection("messages").add(msg).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send message", e)
        }
    }

    suspend fun sendImage(roomId: String, imageUri: Uri): Result<Unit> {
        return try {
            val uid = currentUid() ?: return Result.Error("Not signed in")
            val senderName = getOrFetchDisplayName(uid)

            val messageRef = db.collection("rooms").document(roomId).collection("messages").document()
            val messageId = messageRef.id

            val imageRef = storage.child("rooms/$roomId/images/$messageId.jpg")
            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            val msg = hashMapOf(
                "senderId" to uid,
                "senderName" to senderName,
                "type" to ChatMessage.TYPE_IMAGE,
                "text" to null,
                "imageUrl" to downloadUrl,
                "sentAt" to FieldValue.serverTimestamp()
            )

            messageRef.set(msg).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send image", e)
        }
    }


    suspend fun setTyping(roomId: String, isTyping: Boolean) {
        val uid = currentUid() ?: return
        // Update only this user's typing flag using dot notation.
        db.collection("rooms").document(roomId)
            .update("typing.$uid", isTyping)
            .await()
    }

    fun listenTyping(roomId: String, onTyping: (List<String>) -> Unit, onError: (String) -> Unit): () -> Unit {
        val uid = currentUid()
        val reg = db.collection("rooms").document(roomId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Typing listener error")
                    return@addSnapshotListener
                }
                val typingMap = snap?.get("typing") as? Map<*, *> ?: emptyMap<Any, Any>()
                val active = typingMap
                    .filter { (_, v) -> v == true }
                    .mapNotNull { (k, _) -> k as? String }
                    .filter { it != uid }
                onTyping(active)
            }
        return { reg.remove() }
    }

    suspend fun getOrFetchDisplayName(uid: String): String {
        nameCache[uid]?.let { return it }
        val snap = db.collection("users").document(uid).get().await()
        val name = snap.getString("displayName") ?: "User"
        nameCache[uid] = name
        return name
    }

    private fun DocumentSnapshot.toRoom(): ChatRoom? {
        val name = getString("name") ?: return null
        return ChatRoom(
            id = id,
            name = name,
            createdAt = getTimestamp("createdAt")?.let { Timestamp(it.seconds, it.nanoseconds) },
            createdBy = getString("createdBy") ?: ""
        )
    }

    private fun DocumentSnapshot.toMessage(): ChatMessage? {
        val senderId = getString("senderId") ?: return null
        val senderName = getString("senderName") ?: "User"
        val type = getString("type") ?: ChatMessage.TYPE_TEXT
        val text = getString("text")
        val imageUrl = getString("imageUrl")
        val ts = getTimestamp("sentAt")
        return ChatMessage(
            id = id,
            senderId = senderId,
            senderName = senderName,
            type = type,
            text = text,
            imageUrl = imageUrl,
            sentAt = ts?.let { Timestamp(it.seconds, it.nanoseconds) }
        )
    }
}
