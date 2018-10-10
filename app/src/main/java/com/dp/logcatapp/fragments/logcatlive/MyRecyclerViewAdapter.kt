package com.dp.logcatapp.fragments.logcatlive

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dp.logcat.Log
import com.dp.logcat.LogPriority
import com.dp.logcat.Logcat
import com.dp.logcatapp.R
import com.dp.logcatapp.util.PreferenceKeys
import com.logcat.collections.FixedCircularArray

internal class MyRecyclerViewAdapter(context: Context, initialCapacity: Int) :
        RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder>(),
        View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private var data = FixedCircularArray<Log>(initialCapacity, Logcat.INITIAL_LOG_SIZE)
    private var onClickListener: ((View) -> Unit)? = null

    private val priorityColorAssert = ContextCompat.getColor(context, R.color.priority_assert)
    private val priorityColorDebug = ContextCompat.getColor(context, R.color.priority_debug)
    private val priorityColorError = ContextCompat.getColor(context, R.color.priority_error)
    private val priorityColorInfo = ContextCompat.getColor(context, R.color.priority_info)
    private val priorityColorVerbose = ContextCompat.getColor(context, R.color.priority_verbose)
    private val priorityColorWarning = ContextCompat.getColor(context, R.color.priority_warning)
    private val priorityColorFatal = ContextCompat.getColor(context, R.color.priority_fatal)
    private val priorityColorSilent = ContextCompat.getColor(context, R.color.priority_silent)

    private fun getPriorityColor(priority: String) = when (priority) {
        LogPriority.ASSERT -> priorityColorAssert
        LogPriority.DEBUG -> priorityColorDebug
        LogPriority.ERROR -> priorityColorError
        LogPriority.FATAL -> priorityColorFatal
        LogPriority.INFO -> priorityColorInfo
        LogPriority.VERBOSE -> priorityColorVerbose
        LogPriority.WARNING -> priorityColorWarning
        else -> priorityColorSilent
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val log = data[position]
        holder.date.text = log.date
        holder.time.text = log.time
        holder.pid.text = log.pid
        holder.tid.text = log.tid
        holder.priority.text = log.priority
        holder.tag.text = log.tag
        holder.message.text = log.msg

        holder.priority.setBackgroundColor(getPriorityColor(log.priority))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_logcat_live_list_item, parent, false)
        view.setOnClickListener(this)
        return MyViewHolder(view)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.list_item_root -> onClickListener?.invoke(v)
        }
    }

    override fun getItemCount() = data.size

    internal fun addItems(items: List<Log>) {
        val startPosition = data.size
        data.add(items)
        notifyItemRangeInserted(startPosition, items.size)
    }

    operator fun get(index: Int) = data[index]

    internal fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceKeys.Logcat.KEY_MAX_LOGS -> {
                val newCapacity = sharedPreferences.getString(PreferenceKeys.Logcat.KEY_MAX_LOGS,
                        PreferenceKeys.Logcat.Default.MAX_LOGS)!!.trim().toInt()
                val newData = FixedCircularArray<Log>(newCapacity, Logcat.INITIAL_LOG_SIZE)
                newData.add(data)
                data = newData
                notifyDataSetChanged()
            }
        }
    }

    internal fun setOnClickListener(onClickListener: (View) -> Unit) {
        this.onClickListener = onClickListener
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
        val time: TextView = itemView.findViewById(R.id.time)
        val pid: TextView = itemView.findViewById(R.id.pid)
        val tid: TextView = itemView.findViewById(R.id.tid)
        val priority: TextView = itemView.findViewById(R.id.priority)
        val tag: TextView = itemView.findViewById(R.id.tag)
        val message: TextView = itemView.findViewById(R.id.message)
    }
}