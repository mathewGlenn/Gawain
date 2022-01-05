package com.glennappdev.gawain

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.glennappdev.gawain.databinding.ActivityAddTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class AddTask : AppCompatActivity() {

    var dYear = 0
    var dMonth = 0
    var dDay = 0
    var dHour = 0
    var dMinute = 0


    val myCalendar = Calendar.getInstance()
    lateinit var binding: ActivityAddTaskBinding
    lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        val clearDueDate = binding.clearDueDate
        val clearDueTime = binding.clearDueTime
        val editDueDate = binding.editDueDate
        val editDueTime = binding.editDueTime
        val createTask = binding.buttonCreateTask
        var origDate = ""
        var origTime = "00:00"

        firestore = FirebaseFirestore.getInstance()
        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

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

        val documentReference: DocumentReference =
            firestore.collection("allTasks")
                .document(user.uid)
                .collection("userTasks").document()

        createTask.setOnClickListener {

            if (binding.editTaskTitle.text.isEmpty()) {
                Toast.makeText(this, "Please name this task", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val taskTitle = binding.editTaskTitle.text.toString()
            val taskNote = binding.editNote.text.toString()
            val taskSubject = binding.chooseSubj.selectedItem.toString()
            val taskDueDate = getDateFromString(origDate + " " + origTime)
            val task = Task(taskTitle, taskNote, taskSubject, taskDueDate, false, false)
            documentReference.set(task).addOnFailureListener { e ->
                Toast.makeText(this, "Error: $e", Toast.LENGTH_LONG).show()
            }
            finish()

        }
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


}