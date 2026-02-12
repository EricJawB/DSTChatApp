package com.dst4010.dstchatapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dst4010.dstchatapp.databinding.ActivityChatListBinding
import com.dst4010.dstchatapp.firebase.FirebaseRepository
import com.dst4010.dstchatapp.ui.adapter.RoomAdapter
import com.dst4010.dstchatapp.viewmodel.RoomsViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding
    private val vm: RoomsViewModel by viewModels()
    private val repo = FirebaseRepository()

    private lateinit var adapter: RoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = "Rooms"
        setSupportActionBar(binding.toolbar)

        adapter = RoomAdapter { room ->
            val i = Intent(this, ChatRoomActivity::class.java)
            i.putExtra(ChatRoomActivity.EXTRA_ROOM_ID, room.id)
            i.putExtra(ChatRoomActivity.EXTRA_ROOM_NAME, room.name)
            startActivity(i)
        }

        binding.roomsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.roomsRecyclerView.adapter = adapter

        binding.addRoomFab.setOnClickListener { showCreateRoomDialog() }

        vm.rooms.observe(this) { rooms -> adapter.submitList(rooms) }
        vm.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Sign out")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> {
                MainScope().launch { repo.signOut() }
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showCreateRoomDialog() {
        val input = EditText(this)
        input.hint = "Room name"
        AlertDialog.Builder(this)
            .setTitle("Create room")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString()
                vm.createRoom(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
