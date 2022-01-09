package com.glennappdev.gawain


import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var taskList: RecyclerView
    lateinit var firestore: FirebaseFirestore
    lateinit var entryAdapter: FirestoreRecyclerAdapter<Task, ViewHolder>

    lateinit var databaseHelper: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {

        databaseHelper = DatabaseHelper(this)
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
                    val dueDate = if (task.dueDate != null) {
                        task.dueDate.toString()
                    } else {
                        "no_date"
                    }

                    //format the date for display
                    val fireStoreFormat = DateTimeFormatter.ofPattern("")
                    val dateTimeFormat =
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.getDefault())
//
//                    try {
//                        var formattedDateTime = dueDate.for
//                        viewHolder.taskDue.text = dateTimeFormat.format(formattedDateTime)
//                    } catch (e: ParseException) {
//                        e.printStackTrace()
//                    }

                    viewHolder.taskTitle.text = taskTitle
                    viewHolder.taskSubject.text = taskSubject
                    viewHolder.finishTask.isChecked = false



                    val subjColor = getSubjectColor(task.subject)

                    if(subjColor != "no_color"){
                        viewHolder.subjectColor.setCardBackgroundColor(Color.parseColor(subjColor))
                    }

                    if (dueDate != "no_date") {
                        val dateTimeFormatter = DateTimeFormatter.ofPattern("E MMM d H:m:s O u")
                        val offSetDateTime = OffsetDateTime.parse(dueDate, dateTimeFormatter)
                        val monthValue: String

                        if (offSetDateTime.monthValue == 10 || offSetDateTime.monthValue == 11 || offSetDateTime.monthValue == 12) {
                            monthValue = offSetDateTime.monthValue.toString()
                        } else {
                            monthValue = "0" + offSetDateTime.monthValue.toString()
                        }

                        val dateString =
                            offSetDateTime.dayOfMonth.toString() + "/" + monthValue + "/" + offSetDateTime.year.toString()
                        val sdf1 = SimpleDateFormat("dd/MM/yyyy")
                        val sdf2 = SimpleDateFormat("E, MMM dd, ''yy")
                        val dDate = sdf1.parse(dateString)

                        // check dueDate if today or due
                        val dueDateMillis: Long = task.dueDate.time
                        val timeNow = System.currentTimeMillis()
                        val timeDifMilli: Long = dueDateMillis - timeNow

                        if (timeDifMilli in 0..43200000){
                            viewHolder.taskStatus.visibility = View.VISIBLE
                            viewHolder.taskStatus.setTextColor(Color.parseColor("#FF388E3C"))
                            viewHolder.taskStatus.text = "Due today"

                        }else if (timeDifMilli < 0){
                            viewHolder.taskStatus.visibility = View.VISIBLE
                            viewHolder.taskStatus.setTextColor(Color.parseColor("#FFF44336"))
                            viewHolder.taskStatus.text = "Past due"
                            viewHolder.deleteTask.visibility = View.VISIBLE
                        } else{
                            viewHolder.taskStatus.visibility = View.GONE
                            viewHolder.deleteTask.visibility = View.INVISIBLE
                        }

                        // delete task
                        val docID: String = entryAdapter.snapshots.getSnapshot(i).id
                        viewHolder.deleteTask.setOnClickListener {
                            val reference = firestore.collection("allTasks")
                                .document(user.uid)
                                .collection("userTasks")
                                .document(docID)
                            reference.delete()
                            Toast.makeText(applicationContext, "Task deleted", Toast.LENGTH_SHORT).show()
                        }



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
                            (sdf2.format(dDate).toString() + "  " + timeString).also {
                                viewHolder.taskDue.text = it
                            }
                    } else {
                        viewHolder.taskDue.text = "No date"
                    }


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

                    //viewHolder.taskTitle.text = getSubjectColor(taskSubject.toString())
                    //viewHolder.subjectColor.setCardBackgroundColor(Color.parseColor(getSubjectColor(taskSubject.toString())))


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
        var subjectColor: CardView = itemView.findViewById(R.id.cardTaskSubject)
        var finishTask: CheckBox = itemView.findViewById(R.id.finishTask)
        var deleteTask: CardView = itemView.findViewById(R.id.deleteTask)
        var taskStatus: TextView = itemView.findViewById(R.id.taskStatus)


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

    fun finishedTasks(view: android.view.View) {
        startActivity(Intent(this, FinishedTasks::class.java))
    }

    fun settings(view: android.view.View) {
        startActivity(Intent(this, Settings::class.java))
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

}