package com.glennappdev.gawain

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SimpleCursorAdapter
import com.glennappdev.gawain.databinding.ActivityViewTaskBinding
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore


class ViewTask : AppCompatActivity() {

    lateinit var firestore: FirebaseFirestore
    lateinit var binding: ActivityViewTaskBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTaskBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        val taskTitle = intent.getStringExtra("taskTitle")
        val taskDescription = intent.getStringExtra("taskDescription")
        val taskSubject = intent.getStringExtra("taskSubject")
        val taskIsDone = intent.getBooleanExtra("taskIsDone", false)
        val taskDueDate = intent.getStringExtra("taskDueDate")

        binding.editTaskTitle.setText(taskTitle)
        binding.editNote.setText(taskDescription)
        binding.topBarText.text = taskTitle

        firestore = FirebaseFirestore.getInstance()
        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        if (taskIsDone)
            binding.checkTaskFinished.isChecked = true

        val subjects = resources.getStringArray(R.array.subjects)
        binding.chooseSubj.setSelection(subjects.indexOf(taskSubject))

        val taskID = intent.getStringExtra("taskID").toString()
        val documentReference: DocumentReference =
            firestore.collection("allTasks")
                .document(user.uid)
                .collection("userTasks").document(taskID)

        binding.checkTaskFinished.setOnClickListener{
            if (!binding.checkTaskFinished.isChecked){
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

            else{
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
        }

    }
}