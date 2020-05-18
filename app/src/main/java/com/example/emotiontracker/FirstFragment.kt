package com.example.emotiontracker

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.fragment_first.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*
import kotlin.collections.HashMap

class EmotionAdapter : RecyclerView.Adapter<EmotionAdapter.EmotionViewHolder>() {

    val emotions = mutableListOf<Emotion>()
    private val formatter = DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")
    val emotionToEmoticon = hashMapOf(
            "anger" to "😡",
            "disgust" to "🤢",
            "fear" to "😨",
            "happiness" to "😊",
            "neutral" to "😐",
            "sadness" to "😢",
            "surprise" to "😮")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmotionViewHolder {
        return EmotionViewHolder(parent.inflate(R.layout.emotion_item_view))
    }

    override fun getItemCount(): Int = emotions.size

    override fun onBindViewHolder(holder: EmotionViewHolder, position: Int) {
        holder.bind(emotions[position])
    }

    inner class EmotionViewHolder(override val containerView: View):
            RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(emotion: Emotion) {
            val itemEmotionDateText = containerView.findViewById<TextView>(R.id.itemEmotionDateText)
            itemEmotionDateText.text = formatter.format(emotion.time)
            itemEmotionDateText.setBackgroundResource(emotion.color)

            val itemEmotionText = containerView.findViewById<TextView>(R.id.itemEmotionText)
            itemEmotionText.text = emotion.emotionCat

            val itemEmoticonText = containerView.findViewById<TextView>(R.id.emoticon)
            itemEmoticonText.text = emotionToEmoticon[emotion.emotionCat]
        }
    }
}

class FirstFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    private var selectedDate: LocalDate? = LocalDate.now()

    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    private val emotionsAdapter = EmotionAdapter()
    private var emotions = getEmotionsFromJsonString(readFile(activity)).groupBy { it.time.toLocalDate() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("fragment1", "onviewcreated")
        view.findViewById<View>(R.id.button_first).setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(activity!!.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
        val test = getEmotionsFromJsonString(readFile(activity))
        Log.d("fragment1 size", test.size.toString())
        emotions = getEmotionsFromJsonString(readFile(activity)).groupBy { it.time.toLocalDate() }
        emotionRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        emotionRv.adapter = emotionsAdapter
        emotionRv.addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        emotionsAdapter.notifyDataSetChanged()

        val daysOfWeek = daysOfWeekFromLocale()

        val currentMonth = YearMonth.now()
        emotionCalendar.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
        emotionCalendar.scrollToMonth(currentMonth)

        initAdapter()

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val textView = view.dayText
            val layout = view.dayLayout

            val emotionTopView = view.dayEmotionTop
            val emotionBottomView = view.dayEmotionBottom

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            emotionCalendar.notifyDateChanged(day.date)
                            oldDate?.let { emotionCalendar.notifyDateChanged(it) }
                            updateAdapterForDate(day.date)
                        }
                    }
                }
            }
        }

        emotionCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                val layout = container.layout
                textView.text = day.date.dayOfMonth.toString()

                val emotionTopView = container.emotionTopView
                val emotionBottomView = container.emotionBottomView

                emotionTopView.background = null
                emotionBottomView.background = null

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.setTextColorRes(R.color.example_5_text_grey)
                    layout.setBackgroundResource(if (selectedDate == day.date) R.drawable.example_5_selected_bg else 0)

                    val emotions = emotions[day.date]
                    if (emotions != null) {
                        if (emotions.count() == 1) {
                            emotionBottomView.setBackgroundColor(view.context.getColorCompat(emotions[0].color))
                        } else {
                            emotionTopView.setBackgroundColor(view.context.getColorCompat(emotions[0].color))
                            emotionBottomView.setBackgroundColor(view.context.getColorCompat(emotions[1].color))
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.example_5_text_grey_light)
                    layout.background = null
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = view.legendLayout
        }

        emotionCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                                .toUpperCase(Locale.ENGLISH)
                        tv.setTextColorRes(R.color.example_5_text_grey)
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    }
                    month.yearMonth
                }
            }
        }

        emotionCalendar.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            monthYearText.text = title
        }

        nextMonthImage.setOnClickListener {
            emotionCalendar.findFirstVisibleMonth()?.let {
                emotionCalendar.smoothScrollToMonth(it.yearMonth.next)
            }
        }

        previousMonthImage.setOnClickListener {
            emotionCalendar.findFirstVisibleMonth()?.let {
                emotionCalendar.smoothScrollToMonth(it.yearMonth.previous)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data!!.extras
            val imageBitmap = extras!!["data"] as Bitmap?
            val bundle = Bundle()
            bundle.putParcelable("image", imageBitmap)
            NavHostFragment.findNavController(this@FirstFragment)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
        }
    }

    private fun initAdapter() {
        emotionsAdapter.emotions.clear()
        emotionsAdapter.emotions.addAll(emotions[LocalDate.now()].orEmpty())
        emotionsAdapter.notifyDataSetChanged()
    }

    private fun updateAdapterForDate(date: LocalDate?) {
        emotionsAdapter.emotions.clear()
        emotionsAdapter.emotions.addAll(emotions[date].orEmpty())
        emotionsAdapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.example_5_toolbar_color)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.colorPrimaryDark)
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}