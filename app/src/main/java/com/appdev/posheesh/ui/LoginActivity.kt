package com.appdev.posheesh.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import com.appdev.posheesh.DatabaseHandler
import com.appdev.posheesh.MainActivity
import com.appdev.posheesh.R
import com.appdev.posheesh.databinding.ActivityLogin2Binding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogin2Binding
    private var loginButtonPressCount = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginButton: Button = findViewById(R.id.buttonSubmit)
        val secretButton: Button = findViewById(R.id.buttonAccessAdmin)
        val editTextCashierCode: EditText = findViewById(R.id.editTextCashierCode)

        val handler = Handler()

        loginButton.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start a handler to show the secret button after 5 seconds
                    handler.postDelayed({
                        secretButton.visibility = View.VISIBLE
                    }, 5000) // 5000 milliseconds = 5 seconds
                }
                MotionEvent.ACTION_UP -> {
                    // Cancel the handler if the user releases the button before 5 seconds
                    view.performClick() // Ensure the onClick event is triggered
                    handler.removeCallbacksAndMessages(null)
                }
            }
            true
        }

        loginButton.setOnClickListener {
            // Add your existing logic here
            val enteredCode = editTextCashierCode.text.toString().trim()

            if (enteredCode.isNotEmpty()) {
                val db = DatabaseHandler(this)
                // If the entered code is not empty, check if it exists in the database
                if (db.isUserCodeExists(enteredCode)) {
                    // If the user code exists, open the main activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // If the user code does not exist, show a dialog indicating it
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Invalid User Code")
                        .setMessage("The entered user code does not exist.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val dialog = builder.create()
                    dialog.show()
                }
            } else {
                if(secretButton.visibility != View.VISIBLE) {
                    // If the entered code is empty, show a dialog indicating it
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Empty User Code")
                        .setMessage("Please enter a user code.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }


        secretButton.setOnClickListener {
            Toast.makeText(this, "You've unlocked the admin access", Toast.LENGTH_SHORT).show()
            showPasswordDialog()
        }
    }

    private fun showPasswordDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder.setTitle("Enter Admin Password")

        // Create a parent layout to encase the EditText
        val parentLayout = LinearLayout(this)
        parentLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        parentLayout.gravity = Gravity.CENTER
        parentLayout.setPadding(40, 16, 40, 0) // Add margin to the parent layout

        // Create EditText and add it to the parent layout
        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        input.layoutParams = lp
        input.gravity = Gravity.CENTER
        input.background = resources.getDrawable(R.drawable.edit_text_bg, null)
        parentLayout.addView(input)

        builder.setView(parentLayout)

        builder.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            val password = input.text.toString()
            if (password == "admin") {
                showEmployeeIdDialog()
            } else {
                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }

    private fun showEmployeeIdDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder.setTitle("Enter the employee id of the user you want to add")

        // Create a parent layout to encase the EditText
        val parentLayout = LinearLayout(this)
        parentLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        parentLayout.gravity = Gravity.CENTER
        parentLayout.setPadding(40, 16, 40, 0) // Add margin to the parent layout

        // Create EditText and add it to the parent layout
        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        input.layoutParams = lp
        input.gravity = Gravity.CENTER
        input.background = resources.getDrawable(R.drawable.edit_text_bg, null)
        parentLayout.addView(input)

        builder.setView(parentLayout)

        builder.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            val employeeId = input.text.toString()
            val result = addUserToDatabase(employeeId)
            if (result != null) {
                showResultDialog(result)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }

    private fun showResultDialog(result: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add User Result")
        builder.setMessage("User Code: $result\nPlease save this code to login as the user.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun addUserToDatabase(employeeId: String): String? {
        return DatabaseHandler(this).addUser(role = "user",employeeId = employeeId)
    }
}
