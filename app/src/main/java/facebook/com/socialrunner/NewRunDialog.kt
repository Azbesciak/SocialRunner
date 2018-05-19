package facebook.com.socialrunner

import android.app.*
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import java.util.*
import android.app.TimePickerDialog
import android.content.Context
import android.widget.EditText
import android.text.InputType




class NewRunDialog() : DialogFragment() {
    private lateinit var postponeCallback : (Double) -> Unit
    private lateinit var startCallback : (Double) -> Unit
    private lateinit var appContext : Context
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        var pace = 0
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Final set up")
        var inputPace = EditText(appContext)
        inputPace.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        inputPace.setHint("Type your pace [min/km]")
        val padding = 30
        inputPace.setPadding(padding ,padding ,padding ,padding )
        builder.setView(inputPace)

        builder.setMessage("Set your pace and select when you want to start the run")
                .setPositiveButton("Start now", { dialog, id ->
                    startCallback.invoke(inputPace.text.toString().replace(',', '.').toDouble())
                })
                .setNegativeButton("Postpone", { dialog, id ->
                    postponeCallback.invoke(inputPace.text.toString().replace(',', '.').toDouble())
                })

        return builder.create()
    }

    fun setCallbacks(startCallback : (Double) -> Unit, postponeCallback: (Double) -> Unit) : NewRunDialog{
        this.postponeCallback = postponeCallback
        this.startCallback = startCallback
        return this
    }

    fun setContext(context: Context) : NewRunDialog{
        appContext = context
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