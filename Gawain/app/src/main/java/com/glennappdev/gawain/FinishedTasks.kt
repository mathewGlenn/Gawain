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
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.glennappdev.gawain.databinding.ActivityFinishedTasksBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class FinishedTasks : AppCompatActivity() {
    lateinit var binding: ActivityFinishedTasksBinding

    lateinit var finishedTaskList: RecyclerView
    lateinit var firestore: FirebaseFirestore
    lateinit var entryAdapter: FirestoreRecyclerAdapter<Task, ViewHolder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinishedTasksBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        finishedTaskList = binding.finishedTaskList
        firestore = FirebaseFirestore.getInstance()
        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        val query =
            firestore.collection("allTasks")
                .document(user.uid)
                .collection("userTasks")
                .whereEqualTo("done", true)
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
                    viewHolder.finishTask.isChecked = true
                    viewHolder.deleteTask.visibility = View.VISIBLE

                    val taskID = entryAdapter.snapshots.getSnapshot(i).id
                    val documentReference: DocumentReference =
                        firestore.collection("allTasks")
                            .document(user.uid)
                            .collection("userTasks").document(taskID)

                    // finish a task
                    viewHolder.finishTask.setOnClickListener {
                        val taskDone: MutableMap<String, Any> = HashMap()
                        taskDone["done"] = false
                        documentReference.update(taskDone)
                            .addOnFailureListener {
                                Toast.makeText(applicationContext,
                                    "Something went wrong",
                                    Toast.LENGTH_SHORT).show()
                            }

                        Toast.makeText(applicationContext, "Task restored", Toast.LENGTH_SHORT)
                            .show()
                    }

                    viewHolder.view.setOnClickListener{
                            v: View ->
                        val intent = Intent(v.context, ViewTask::class.java)
                        intent.putExtra("taskTitle", taskTitle)
                        intent.putExtra("taskDescription", task.note)
                        intent.putExtra("taskSubject", taskSubject)
                        intent.putExtra("taskDueDate", dueDate)
                        intent.putExtra("taskIsDone", task.isDone)
                        startActivity(intent)
                    }

                    // get id of task
                    val docID: String = entryAdapter.snapshots.getSnapshot(i).id
                    viewHolder.deleteTask.setOnClickListener{
                        val reference = firestore.collection("allTasks")
                            .document(user.uid)
                            .collection("userTasks")
                            .document(docID)
                        reference.delete()
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
                        binding.emptyView.visibility = View.VISIBLE
                    } else
                        binding.emptyView.visibility = View.INVISIBLE
                }
            }

        finishedTaskList.layoutManager = LinearLayoutManager(this)
        finishedTaskList.adapter = entryAdapter

        binding.refreshLayout.setOnRefreshListener {
            entryAdapter.notifyDataSetChanged()
            binding.refreshLayout.isRefreshing = false
        }

        binding.btnBack.setOnClickListener{finish()}
    }

    fun newTask(view: View) {
        startActivity(Intent(this, AddTask::class.java))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        var taskSubject: TextView = itemView.findViewById(R.id.taskSubject)
        var taskDue: TextView = itemView.findViewById(R.id.taskDue)
        var deleteTask: CardView = itemView.findViewById(R.id.deleteTask)
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