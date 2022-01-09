package com.glennappdev.gawain

import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.glennappdev.gawain.databinding.ActivityManageSubjectsBinding
import com.google.android.material.chip.Chip
import petrov.kristiyan.colorpicker.ColorPicker
import petrov.kristiyan.colorpicker.ColorPicker.OnChooseColorListener
import java.util.*
import kotlin.collections.ArrayList

class ManageSubjects : AppCompatActivity() {
    lateinit var colorHex: String
    lateinit var databaseHelper: DatabaseHelper
    lateinit var binding: ActivityManageSubjectsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageSubjectsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        databaseHelper = DatabaseHelper(this)

        val subjectsList = listOf(
            "NSTP", "Eng", "Math", "IT 111", "PE 3", "Calculus"
        )

        //get subjects from database as an array
        val arrSubjects = getSubjectsToArray()

        //show subjects as chips
        showSubjects(arrSubjects)

        val colors: ArrayList<String> = ArrayList()
        colors.add("#C8E6C9")
        colors.add("#FFFFF9C4")
        colors.add("#FFDCEDC8")
        colors.add("#FFE1BEE7")
        colors.add("#FFF8BBD0")
        colors.add("#FFC5CAE9")
        colors.add("#FFBBDEFB")
        colors.add("#FFB2EBF2")

        val random = Random()
        val random_color = random.nextInt(7)
        colorHex = colors[random_color]
        binding.cardColor.setCardBackgroundColor(Color.parseColor(colors[random_color]))

        binding.cardColor.setOnClickListener {
            val colorPicker = ColorPicker(this)

            colorPicker
                .setDefaultColorButton(Color.parseColor("#C8E6C9"))
                .setColors(colors)
                .setColumns(5)
                .setRoundColorButton(true)
            colorPicker.setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    binding.cardColor.setCardBackgroundColor(Color.parseColor(colors[position]))
                    colorHex = colors[position]
                }

                override fun onCancel() {
                    // put code
                }
            }).show()
        }

        binding.addSubject.setOnClickListener {
            if (binding.editSubject.text.isNullOrEmpty()) {
                Toast.makeText(this, "You have to name the subject", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.chipGroupSubjects.removeAllViews()
            arrSubjects.add(binding.editSubject.text.toString())
            showSubjects(arrSubjects)

            addSubject(binding.editSubject.text.toString(), colorHex)
            binding.editSubject.text.clear()

            val anotherRandomColor = random.nextInt(7)
            colorHex = colors[anotherRandomColor]
            binding.cardColor.setCardBackgroundColor(Color.parseColor(colors[anotherRandomColor]))

        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        readInfo()
    }

    fun showSubjects(list: List<String>) {


        val chipGroup = binding.chipGroupSubjects
        for (i in list.indices) {
            val subject = list[i]
            val chip = Chip(this)
            val paddingDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f,
                resources.displayMetrics
            ).toInt()
            chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
            chip.text = subject
            chip.setTextColor(resources.getColor(R.color.white))
            chip.setBackgroundColor(resources.getColor(R.color.black))
            chip.setCloseIconResource(R.drawable.ic_baseline_close_24)
            chip.setTextAppearance(this, android.R.style.TextAppearance_Small)
//
//            val states = arrayOf(intArrayOf(android.R.attr.state_enabled),
//                intArrayOf(-android.R.attr.state_enabled),
//                intArrayOf(-android.R.attr.state_checked),
//                intArrayOf(android.R.attr.state_pressed))
//
//            val colors = intArrayOf(
//                Color.parseColor(getSubjectColor(subject)),
//                Color.parseColor(getSubjectColor(subject)),
//                Color.parseColor(getSubjectColor(subject)),
//                Color.parseColor(getSubjectColor(subject))
//            )
//
//            val myList = ColorStateList(states, colors)
//            chip.chipBackgroundColor = myList
            chip.isCloseIconVisible = true

            chip.setOnCloseIconClickListener {
                chipGroup.removeView(chip)
                deleteSubject(subject)
            }

            chipGroup.addView(chip)
        }
    }

    fun addSubject(subj: String, color: String) {
        val subjTitle = binding.editSubject.text.toString()
        val resultSuccessful = databaseHelper.insertInfo(subj, color)

        if (resultSuccessful) {
            Toast.makeText(this, "Subject added", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    fun readInfo() {
        val cursor: Cursor = databaseHelper.info
        val stringBuilder = StringBuilder()
        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                stringBuilder.append("ID: ").append(cursor.getString(0)).append("\n")
                stringBuilder.append("subject: ").append(cursor.getString(1)).append("\n")
                stringBuilder.append("color: ").append(cursor.getString(2)).append("\n\n")
            }
            //binding.txtData.text = stringBuilder.toString()
        }
    }

    fun getSubjectsToArray(): ArrayList<String> {
        val cursor: Cursor = databaseHelper.info
        val arrSubjects: ArrayList<String> = ArrayList()

        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                arrSubjects.add(cursor.getString(1))
            }
        }
        return arrSubjects
    }

    fun deleteSubject(subjTitle: String) {
        val result = databaseHelper.deleteSubj(subjTitle)
        Toast.makeText(this, "Subject removed", Toast.LENGTH_SHORT).show()
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