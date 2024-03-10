package com.appdev.openaitest.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.appdev.posheesh.DatabaseHandler
import com.appdev.posheesh.R
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class AIChatBoxBalloon(private val context: Context): Activity() {
    private val balloon: Balloon
    private val chatLayout: View
    private val sendButton: ImageButton
    private val messageEditText: EditText
    private val chatMessagesLayout: LinearLayout
    private val client = OkHttpClient()
    private var chatScrollView: ScrollView
    private var dbHandler: DatabaseHandler = DatabaseHandler(context)
    init {
        // Inflate the custom chat layout
        chatLayout = LayoutInflater.from(context).inflate(R.layout.layout_floating_chatbox, null)
        sendButton = chatLayout.findViewById(R.id.send_message_button)
        messageEditText = chatLayout.findViewById(R.id.message_input_edittext)
        chatMessagesLayout = chatLayout.findViewById(R.id.chat_messages_layout)
        // Assuming chatScrollView is your ScrollView
        chatScrollView = chatLayout.findViewById<ScrollView>(R.id.chat_messages_container)


        // Create a Balloon instance and set the custom layout
        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.search_box_background)
        balloon = Balloon.Builder(context)
            .setWidthRatio(0.6f)
            .setHeight(400)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(10)
            .setArrowPosition(0.5f)
            .setPadding(3)
            .setBackgroundDrawable(backgroundDrawable)
            .setCornerRadius(8f)
            .setLayout(chatLayout)
            .setBalloonAnimation(BalloonAnimation.OVERSHOOT) // Apply the animation here
            .build()
        // Initialize chat UI elements and set up listeners
        initChatUI()
    }

    private fun initChatUI() {
        sendButton.setOnClickListener {
            // Handle message sending logic
            val message = messageEditText.text.toString().trim()
            sendMessage(message, true)
            val question=message.trim()
            if(question.isNotEmpty()){
                getResponse(question) { response ->
                    runOnUiThread {
                        sendMessage(response, false)
                    }
                }
            }
        }
    }
    private fun sendMessage(message: String,  isUserMessage: Boolean) {
        // Handle message sending logic
        // Get the message text from the EditText
        val messageText = message

        if (messageText.isNotEmpty()) {
            // Create a new LinearLayout to hold the message TextView
            val messageLayout = LinearLayout(context)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Set margins based on who sends the message
            if (isUserMessage) {
                layoutParams.setMargins(16, 16, 16, 16) // Add right margin for user's message
                layoutParams.gravity = Gravity.END // Align to the right if user's message
            } else {
                layoutParams.gravity = Gravity.START // Align to the left if other's message
                layoutParams.setMargins(16, 16, 16, 16) // Add left margin for other's message

            }
            messageLayout.layoutParams = layoutParams

            // Create a new TextView to display the message
            val newMessageTextView = TextView(context)
            newMessageTextView.text = messageText
            newMessageTextView.setTextColor(Color.BLACK)
            newMessageTextView.textSize = 16f
            newMessageTextView.setPadding(16, 8, 16, 8)

            if (isUserMessage) {
                // If the message is from the user, set background to blue and align right
                newMessageTextView.setBackgroundResource(R.drawable.user_message_background)
                newMessageTextView.setTextColor(Color.WHITE)
                // Add icon at the end of the message bubble
                //newMessageTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_menu_camera, 0)
                // Align the text to the end (right)
                newMessageTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            } else {
                // If the message is from the other, set background to gray and align left
                newMessageTextView.setBackgroundResource(R.drawable.ai_message_background)
                newMessageTextView.setTextColor(Color.BLACK)
                // Add icon at the start of the message bubble
                //newMessageTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_gallery, 0, 0, 0)
                // Align the text to the start (left)
                newMessageTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                newMessageTextView.gravity = View.TEXT_ALIGNMENT_TEXT_START
            }

            // Add the new message TextView to the messageLayout
            messageLayout.addView(newMessageTextView)

            // Add the messageLayout to the chat messages layout
            chatMessagesLayout.addView(messageLayout)

            // Clear the EditText after sending the message
            messageEditText.text.clear()
            //Update the height of the balloon to show the new message
        } else {
            // Show a toast or handle empty message case
            Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
        chatScrollView.post {
            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    fun show(anchorView: View) {
        balloon.showAlignBottom(anchorView) // Show the balloon at the bottom of an anchor view
    }

    fun getResponse(question: String, callback: (String) -> Unit){

        val apiKey="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"
        val url="https://api.openai.com/v1/chat/completions"
        val refTableString = dbHandler.generateDatabaseDescription()
        val content = "If my question does not relate to the data that I will be giving, don't reference it and just answer the question, but if it does, reference the data. This is the data "+refTableString + "         and this is the question  "+question

        val requestBody="""
            {
            "model": "gpt-3.5-turbo",
            "messages": [{"role": "user", "content": "$content"}],
            "temperature": 0.7
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error","API failed",e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body=response.body?.string()
                if (body != null) {
                    Log.v("data",body)
                }
                else{
                    Log.v("data","empty")
                }
                val jsonObject = JSONObject(body)
                val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                val messageObject = jsonArray.getJSONObject(0).getJSONObject("message")
                val content = messageObject.getString("content")
                val textResult = content
                callback(textResult)
            }
        })
    }

}
