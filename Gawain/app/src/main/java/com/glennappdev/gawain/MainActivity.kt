package com.glennappdev.gawain

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.glennappdev.gawain.databinding.ActivityMainBinding


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var taskList: RecyclerView
    lateinit var firestore: FirebaseFirestore
    lateinit var entryAdapter: FirestoreRecyclerAdapter<Task, ViewHolder>


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        taskList = binding.taskList
        firestore = FirebaseFirestore.getInstance()
        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        val query =
            firestore.collection("allTasks")
                .document(user.uid)
                .collection("userTasks")
                .whereEqualTo("done", false)
                .orderBy("dueDate", Query.Direction.ASCENDING)

        val userTasks = FirestoreRecyclerOptions.Builder<Task>()
            .setQuery(query, Task::class.java)
            .build()

        entryAdapter =
            object :
                FirestoreRecyclerAdapter<Task, ViewHolder>(
                    userTasks) {
                override fun onBindViewHolder(viewHolder: ViewHolder, i: Int, task: Task) {
                    val taskTitle = task.title
                    val taskSubject = task.subject
                    val dueDate = task.dueDate.toString()

                    //format the date for display
                    val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

                    try {
                        val dateTime = format.parse(dueDate)
                        val dateTimeFormat =
                            SimpleDateFormat("MM dd, yyy   hh:mm", Locale.getDefault())
                        viewHolder.taskDue.text = dateTimeFormat.format(dateTime)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                    viewHolder.taskTitle.text = taskTitle
                    viewHolder.taskSubject.text = taskSubject

                    val taskID = entryAdapter.snapshots.getSnapshot(i).id
                    val documentReference: DocumentReference =
                        firestore.collection("allTasks")
                            .document(user.uid)
                            .collection("userTasks").document(taskID)

                    // finish a task
                    viewHolder.finishTask.setOnClickListener {
                        val taskDone: MutableMap<String, Any> = HashMap()
                        taskDone["done"] = true
                        documentReference.update(taskDone)
                            .addOnFailureListener {
                                Toast.makeText(applicationContext,
                                    "Something went wrong",
                                    Toast.LENGTH_SHORT).show()
                            }

                        Toast.makeText(applicationContext, "Task finished", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val taskView: View =
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.task_view, parent, false)
                    return ViewHolder(taskView)
                }

                override fun onDataChanged() {
                    super.onDataChanged()

                    if (itemCount == 0) {
                        binding.txtNoTaskYet.visibility = View.VISIBLE
                    } else
                        binding.txtNoTaskYet.visibility = View.INVISIBLE
                }
            }

        taskList.layoutManager = LinearLayoutManager(this)
        taskList.adapter = entryAdapter

        binding.refreshLayout.setOnRefreshListener {
            entryAdapter.notifyDataSetChanged()
            binding.refreshLayout.isRefreshing = false
        }
    }

    fun newTask(view: View) {
        startActivity(Intent(this, AddTask::class.java))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        var taskSubject: TextView = itemView.findViewById(R.id.taskSubject)
        var taskDue: TextView = itemView.findViewById(R.id.taskDue)

        var finishTask: CheckBox = itemView.findViewById(R.id.finishTask)
        var view: View = itemView

    }

    override fun onStart() {
        super.onStart()
        entryAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()

        entryAdapter.stopListening()
    }

}