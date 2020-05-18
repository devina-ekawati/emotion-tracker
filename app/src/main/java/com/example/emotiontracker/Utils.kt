package com.example.emotiontracker

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

fun saveFile(data: String, context: FragmentActivity?) {
    val fileOutputStream: FileOutputStream

    try {
        fileOutputStream = context!!.openFileOutput("emotions.json", Context.MODE_PRIVATE)
        fileOutputStream.write(data.toByteArray())
        fileOutputStream.close()
    }catch (e: Exception){
        e.printStackTrace()
    }
}

fun readFile(context: FragmentActivity?): String {
    var fileInputStream: FileInputStream? = null
    try {
        fileInputStream = context!!.openFileInput("emotions.json")
        var inputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        val stringBuilder: StringBuilder = StringBuilder()
        var text: String? = null
        while ({ text = bufferedReader.readLine(); text }() != null) {
            stringBuilder.append(text)
        }
        fileInputStream.close()
        return stringBuilder.toString()
    } catch (e: Exception) {
        return "[]"
    }

}

fun getEmotionsFromJsonString(emotionsFromJson: String): List<Emotion> {
    val gson = Gson()
    val listType = object : TypeToken<List<Emotion>>() { }.type
    return gson.fromJson(emotionsFromJson, listType)
}

fun getJsonStringFromEmotions(emotions: List<Emotion>): String {
    val gson = Gson()
    return gson.toJson(emotions)
}

