package com.example.emotiontracker

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.emotiontracker.tflite.Classifier
import org.threeten.bp.LocalDateTime
import java.io.*

class SecondFragment : Fragment() {
    private var classifier: Classifier? = null
    private val numThreads = -1

    private val EMOTION_CAT_TO_RESPONSE = hashMapOf(
            "anger" to "You look angry now 😡",
            "disgust" to "You look disgusted now 🤢",
            "fear" to "You look fearful now 😨",
            "happiness" to "You look happy now 😊",
            "neutral" to "You look neutral now 😐",
            "sadness" to "You look sad now 😢",
            "surprise" to "You look surprised now 😮")

    private val EMOTION_CAT_TO_COLOR = hashMapOf(
            "anger" to R.color.anger,
            "disgust" to R.color.disgust,
            "fear" to R.color.fear,
            "happiness" to R.color.happiness,
            "neutral" to R.color.neutral,
            "sadness" to R.color.sadness,
            "surprise" to R.color.surprise)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        try {
            classifier = Classifier(activity, numThreads)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = getView()?.findViewById<ImageView>(R.id.image_view)
        val emotionCategoryTextView = getView()?.findViewById<TextView>(R.id.emotion_category)

        val image = arguments!!.getParcelable<Bitmap>("image")
        val emotionCategory = classifier!!.classifyImage(image)

        imageView?.setImageBitmap(image)
        emotionCategoryTextView?.setText(EMOTION_CAT_TO_RESPONSE[emotionCategory])

        view.findViewById<View>(R.id.button_second).setOnClickListener {
            var emotions = getEmotionsFromJsonString(readFile(activity)).toMutableList()
            val currentEmotion: Emotion? = EMOTION_CAT_TO_COLOR[emotionCategory]?.let { it1 -> Emotion(LocalDateTime.now(), emotionCategory, it1) }
            currentEmotion?.let { it1 -> emotions.add(it1) }

            Log.d("second fragment", emotions.size.toString())

            saveFile(getJsonStringFromEmotions(emotions), activity)

            NavHostFragment.findNavController(this@SecondFragment)
                    .navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        view.findViewById<View>(R.id.cancel).setOnClickListener {
            NavHostFragment.findNavController(this@SecondFragment)
                    .navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }


}