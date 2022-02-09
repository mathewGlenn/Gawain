package com.glennappdev.gawain

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.glennappdev.gawain.databinding.ActivityViewTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


class ViewTask : AppCompatActivity() {

    var dYear = 0
    var dMonth = 0
    var dDay = 0
    var dHour = 0
    var dMinute = 0


    val myCalendar = Calendar.getInstance()

    lateinit var databaseHelper: DatabaseHelper

    lateinit var firestore: FirebaseFirestore
    lateinit var binding: ActivityViewTaskBinding

    var dueDate: String? = null
    lateinit var timeString: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTaskBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        databaseHelper = DatabaseHelper(this)
        val subjects = getSubjectsToArray()
        // add subjects to spinner
        val arrayAdapter = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item, subjects)
        binding.chooseSubj.adapter = arrayAdapter


        val taskTitle = intent.getStringExtra("taskTitle")
        val taskDescription = intent.getStringExtra("taskDescription")
        val taskSubject = intent.getStringExtra("taskSubject")
        val taskIsDone = intent.getBooleanExtra("taskIsDone", false)
        val taskDueDate = intent.getStringExtra("taskDueDate")
        val taskID = intent.getStringExtra("taskID").toString()

        binding.editTaskTitle.setText(taskTitle)
        binding.editNote.setText(taskDescription)
        binding.topBarText.text = taskTitle

        // datetime picker
        val clearDueDate = binding.clearDueDate
        val clearDueTime = binding.clearDueTime
        val editDueDate = binding.editDueDate
        val editDueTime = binding.editDueTime
        var origDate = ""
        var origTime = "00:00"
        var isFinished: Boolean



        firestore = FirebaseFirestore.getInstance()
        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        if (taskIsDone)
            binding.checkTaskFinished.isChecked = true


        binding.chooseSubj.setSelection(subjects.indexOf(taskSubject))

        val documentReference: DocumentReference =
            firestore.collection("allTasks")
                .document(user.uid)
                .collection("userTasks").document(taskID)

        if (taskDueDate != "no_date" && !taskDueDate.isNullOrEmpty()) {
            //time
            val dateTimeFormatter = DateTimeFormatter.ofPattern("E MMM d H:m:s O u")
            val offSetDateTime = OffsetDateTime.parse(taskDueDate, dateTimeFormatter)
            val monthValue: String
            if (offSetDateTime.monthValue == 10 || offSetDateTime.monthValue == 11 || offSetDateTime.monthValue == 12) {
                monthValue = offSetDateTime.monthValue.toString()
            } else {
                monthValue = "0" + offSetDateTime.monthValue.toString()
            }


            val dateString =
                offSetDateTime.dayOfMonth.toString() + "/" + monthValue + "/" + offSetDateTime.year.toString()
            val sdf1 = SimpleDateFormat("dd/MM/yyyy")
            val sdf2 = SimpleDateFormat("E, MMM dd, yyyy")
            val dDate = sdf1.parse(dateString)
            dueDate = sdf2.format(dDate).toString()
            editDueDate.setText(dueDate)

            var hour = offSetDateTime.hour
            val minute = offSetDateTime.minute
            val noTime = (hour == 0 && minute == 0)

            origDate =
                offSetDateTime.dayOfMonth.toString() + "-" + offSetDateTime.monthValue + "-" + offSetDateTime.year
            origTime = "$hour:$minute"
            // val noTime = (hour == 0 && minute == 0)
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

            timeString = "$hour:$minute $format"
            if (!noTime) {
                editDueTime.setText(timeString)
            }
            clearDueDate.visibility = View.VISIBLE
            clearDueTime.visibility = View.VISIBLE

        }

        editDueDate.setOnClickListener {

            // get current date
            dYear = myCalendar.get(Calendar.YEAR)
            dMonth = myCalendar.get(Calendar.MONTH)
            dDay = myCalendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, year, monthOfYear, dayOfMonth -> //set text
                    //format date
                    val originalFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val dateFormat = SimpleDateFormat("E, MMM dd, yyyy", Locale.getDefault())
                    origDate = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                    var formattedDate: Date? = null
                    try {
                        formattedDate = originalFormat.parse(origDate)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    editDueDate.setText(dateFormat.format(formattedDate))
                    clearDueDate.visibility = View.VISIBLE
                }, dYear, dMonth, dDay)
            datePickerDialog.setOnCancelListener { clearDueDate.visibility = View.GONE }
            datePickerDialog.show()

        }

        editDueTime.setOnClickListener {
            // get current time
            dHour = myCalendar.get(Calendar.HOUR_OF_DAY)
            dMinute = myCalendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this,
                { _, hourOfDay: Int, minute: Int ->
                    val format: String
                    var hour = hourOfDay

                    origTime = "$hourOfDay:$minute"
                    if (hour == 0) {
                        hour += 12
                        format = "AM"
                    } else if (hour == 12) {
                        format = "PM"
                    } else if (hour > 12) {
                        hour -= 12
                        format = "PM"
                    } else {
                        format = "AM"
                    }
                    val chosenTime = "$hour:$minute $format"
                    editDueTime.setText(chosenTime)
                    clearDueTime.visibility = View.VISIBLE
                }, dHour, dMinute, false)
            timePickerDialog.setOnCancelListener {
                clearDueTime.visibility = View.GONE
                origTime = "00:00"
            }
            timePickerDialog.show()
        }

        clearDueDate.setOnClickListener {
            editDueDate.text.clear()
            clearDueDate.visibility = View.GONE
        }
        clearDueTime.setOnClickListener {
            editDueTime.text.clear()
            clearDueTime.visibility = View.GONE
        }


        binding.btnSaveChanges.setOnClickListener {
            if (binding.editTaskTitle.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please name this task", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val title = binding.editTaskTitle.text.toString()
            val note = binding.editNote.text.toString()
            val subj = binding.chooseSubj.selectedItem.toString()
            val due = getDateFromString("$origDate $origTime")
            val taskFinished = binding.checkTaskFinished.isChecked
            val task = Task(title, note, subj, due, false, taskFinished)
            documentReference.set(task)
                .addOnFailureListener {
                    Toast.makeText(applicationContext,
                        "Something went wrong",
                        Toast.LENGTH_SHORT).show()
                }

            Toast.makeText(applicationContext, "Changes are saved", Toast.LENGTH_SHORT)
                .show()

            // cancel and create new notification if duedate or task title is changed
            val chooseSubj = binding.chooseSubj

            if (dueDate != editDueDate.text.toString()
                || timeString != editDueTime.toString()
                || taskTitle != binding.editTaskTitle.text.toString()
                || taskSubject != chooseSubj.selectedItem.toString()) {
                if(!editDueDate.text.isNullOrEmpty()){
                    cancelNotification(this, "tag:$taskTitle")

                    val dueDateMillis = due!!.time
                    val timeNow = System.currentTimeMillis()
                    val advanceMilli = 43200000
                    val timeDifMilli: Long = dueDateMillis - timeNow - advanceMilli
                    val tag = "tag:" + binding.editTaskTitle.text.toString()
                    val notificationMessage = "${chooseSubj.selectedItem} - ${binding.editTaskTitle.text}"
                    val title = "Your task is nearly due"
                    scheduleNotification(this, timeDifMilli, tag, title, notificationMessage)
                }
            }

            finish()

        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        //delete task

        binding.btnDelete.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage("Delete this task?")
                .setCancelable(true)
                .setPositiveButton("Delete") { _, _ ->
                    val reference = firestore.collection("allTasks")
                        .document(user.uid)
                        .collection("userTasks")
                        .document(taskID)
                    reference.delete()
                    Toast.makeText(applicationContext, "Task deleted", Toast.LENGTH_SHORT)
                        .show()

                    cancelNotification(this, "tag:${binding.editTaskTitle.text}")

                    finish()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

    }

    val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
    private fun getDateFromString(date: String): Date? {
        return try {
            val date: Date = format.parse(date)
            date
        } catch (e: ParseException) {
            null
        }
    }

    private fun getSubjectsToArray(): ArrayList<String> {
        val cursor: Cursor = databaseHelper.info
        val arrSubjects: ArrayList<String> = ArrayList()

        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                arrSubjects.add(cursor.getString(1))
            }
        }
        return arrSubjects
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

    private fun cancelNotification(context: Context, tag: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tag)
    }
}