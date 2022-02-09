package com.glennappdev.gawain

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.glennappdev.gawain.databinding.ActivityFinishedTasksBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import petrov.kristiyan.colorpicker.ColorViewAdapter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class FinishedTasks : AppCompatActivity() {
    lateinit var binding: ActivityFinishedTasksBinding
    lateinit var databaseHelper: DatabaseHelper
    lateinit var finishedTaskList: RecyclerView
    lateinit var firestore: FirebaseFirestore
    lateinit var entryAdapter: FirestoreRecyclerAdapter<Task, ViewHolder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinishedTasksBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        databaseHelper = DatabaseHelper(this)
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
                    val dueDate = if (task.dueDate != null) {
                        task.dueDate.toString()
                    } else {
                        "no_date"
                    }

                    if (dueDate != "no_date") {
                        val dateTimeFormatter = DateTimeFormatter.ofPattern("E MMM d H:m:s O u")
                        val offSetDateTime = OffsetDateTime.parse(dueDate, dateTimeFormatter)

                        val monthValue: String =
                            if (offSetDateTime.monthValue == 10 || offSetDateTime.monthValue == 11 || offSetDateTime.monthValue == 12) {
                                offSetDateTime.monthValue.toString()
                            } else {
                                "0" + offSetDateTime.monthValue.toString()
                            }

                        val dateString =
                            offSetDateTime.dayOfMonth.toString() + "/" + monthValue + "/" + offSetDateTime.year.toString()
                        val sdf1 = SimpleDateFormat("dd/MM/yyyy")
                        val sdf2 = SimpleDateFormat("E, MMM dd, ''yy")
                        val dDate = sdf1.parse(dateString)

                        var hour = offSetDateTime.hour
                        val minute = offSetDateTime.minute

                        val noTime = (hour == 0 && minute == 0)
                        val format: String
                        when {
                            hour == 0 -> {
                                hour += 12
                                format = "AM"
                            }
                            hour == 12 -> {
                                format = "PM"
                            }
                            hour > 12 -> {
                                hour -= 12
                                format = "PM"
                            }
                            else -> {
                                format = "AM"
                            }
                        }

                        val timeString: String

                        val hourString: String = if (hour < 10) {
                            "0$hour"
                        } else
                            "$hour"

                        val minuteString: String = if (minute < 10) {
                            "0$minute"
                        } else
                            "$minute"

                        timeString = "$hourString:$minuteString $format"

                        if (noTime) {
                            viewHolder.taskDue.text = sdf2.format(dDate).toString()
                        } else
                            viewHolder.taskDue.text =
                                sdf2.format(dDate).toString() + "  " + timeString
                    } else {
                        viewHolder.taskDue.text = "No date"
                    }

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

                    val subjColor = getSubjectColor(task.subject)

                    if (subjColor != "no_color") {
                        viewHolder.subjectColor.setCardBackgroundColor(Color.parseColor(subjColor))
                    }

                    val taskID = entryAdapter.snapshots.getSnapshot(i).id
                    val documentReference: DocumentReference =
                        firestore.collection("allTasks")
                            .document(user.uid)
                            .collection("userTasks").document(taskID)

                    // restore finished task
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

                        // Task reminder set
                        val dueDateMillis = task.dueDate.time
                        val timeNow = System.currentTimeMillis()
                        val alertAdvanceMilli = 43200000
                        val timeDifMilli: Long = dueDateMillis - timeNow - alertAdvanceMilli
                        val tag = "tag:" + task.title
                        val notificationMessage = task.title.toString()
                        val title = "Your task is nearly due"
                        scheduleNotification(this@FinishedTasks, timeDifMilli, tag, title, notificationMessage)

                    }

                    viewHolder.view.setOnClickListener { v: View ->
                        val intent = Intent(v.context, ViewTask::class.java)
                        intent.putExtra("taskTitle", taskTitle)
                        intent.putExtra("taskDescription", task.note)
                        intent.putExtra("taskSubject", taskSubject)
                        intent.putExtra("taskDueDate", dueDate)
                        intent.putExtra("taskIsDone", task.isDone)
                        intent.putExtra("taskID", taskID)
                        startActivity(intent)
                    }

                    // get id of task
                    val docID: String = entryAdapter.snapshots.getSnapshot(i).id

                    //delete task
                    viewHolder.deleteTask.setOnClickListener {
                        val reference = firestore.collection("allTasks")
                            .document(user.uid)
                            .collection("userTasks")
                            .document(docID)
                        reference.delete()

                        //cancelNotification(this@FinishedTasks, "tag:"+task.title)
                    }

                    // delete all finished tasks. p.s. I thought of this code :P
                    binding.btnDeleteAllFinishedTasks.setOnClickListener {
                        val alertDialogBuilder = AlertDialog.Builder(this@FinishedTasks)
                        alertDialogBuilder.setMessage("Delete all finished tasks?")
                            .setCancelable(true)
                            .setPositiveButton("Delete all") { _, _ ->
                                for (ids in 0 until finishedTaskList.size) {
                                    val currentID = entryAdapter.snapshots.getSnapshot(ids).id
                                    val reference = firestore.collection("allTasks")
                                        .document(user.uid)
                                        .collection("userTasks")
                                        .document(currentID)
                                    reference.delete()
                                    notifyItemRemoved(ids)
                                    Toast.makeText(applicationContext,
                                        "all finished tasks deleted",
                                        Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            .setNegativeButton("Cancel"){dialog,_ ->
                                dialog.cancel()
                            }

                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
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

        binding.btnBack.setOnClickListener { finish() }
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

        var subjectColor: CardView = itemView.findViewById(R.id.cardTaskSubject)

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

    fun getSubjectColor(subject: String): String {
        val cursor: Cursor = databaseHelper.info

        var subjColor = "no_color"
        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == subject) {
                    subjColor = cursor.getString(2)
                }
            }
        }
        return subjColor
    }


    val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
    fun getDateFromString(date: String): Date? {
        try {
            val date: Date = format.parse(date)
            return date
        } catch (e: ParseException) {
            return null
        }
    }

    private fun scheduleNotification(
        context: Context,
        timeDelay: Long,
        tag: String,
        title: String,
        body: String,
    ) {

        val data = Data.Builder().putString("body", body).putString("title", title)


        val work = OneTimeWorkRequestBuilder<NotificationSchedule>()
            .setInitialDelay(timeDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder()
                .setTriggerContentMaxDelay(1000, TimeUnit.MILLISECONDS).build()) // API Level 24
            .setInputData(data.build())
            .addTag(tag)
            .build()

        WorkManager.getInstance(context).enqueue(work)
    }

}