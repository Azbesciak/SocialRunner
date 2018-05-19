package facebook.com.socialrunner

import android.app.*
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import java.util.*
import android.app.TimePickerDialog


class NewRunDialog() : DialogFragment() {
    private lateinit var postponeCallback : () -> Unit
    private lateinit var startCallback : () -> Unit

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Set your pace and select when you want to start the run")
                .setPositiveButton("Start now", { dialog, id ->
                    startCallback.invoke()
                })
                .setNegativeButton("Postpone", { dialog, id ->
                    postponeCallback.invoke()
                })

        return builder.create()
    }

    fun setCallbacks(startCallback : () -> Unit, postponeCallback: () -> Unit) : NewRunDialog{
        this.postponeCallback = postponeCallback
        this.startCallback = startCallback
        return this
    }
}

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener{
    lateinit var timePickedCallback : (Int, Int) -> Unit
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        timePickedCallback.invoke(hourOfDay, minute)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
    }

    fun setCallback(timePicked : (Int, Int) -> Unit) : TimePickerFragment
    {
        this.timePickedCallback = timePicked
        return this
    }

}