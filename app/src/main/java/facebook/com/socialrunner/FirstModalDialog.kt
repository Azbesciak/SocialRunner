package facebook.com.socialrunner

import android.app.*
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import java.util.*
import android.app.TimePickerDialog
import android.content.Context
import android.text.Editable
import android.widget.EditText
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher


class NewRunDialog : DialogFragment() {
    private lateinit var postponeCallback: (Double) -> Unit
    private lateinit var startCallback: (Double) -> Unit
    private lateinit var appContext: Context
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val inputPace = EditText(appContext).apply {
            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
            hint = "Type your pace [min/km]"
            val padding = 30
            setPadding(padding, padding, padding, padding)
        }
        val dialog = AlertDialog.Builder(activity)
                .setTitle("Final set up")
                .setView(inputPace)
                .setMessage("Set your pace and select when you want to start the run")
                .setPositiveButton("Start now", { dialog, id ->
                    inputPace.ifNotEmpty { startCallback.invoke(it) }
                })
                .setNegativeButton("Postpone", { _, _ ->
                    inputPace.ifNotEmpty { postponeCallback.invoke(it) }
                })
                .create()
        dialog.setOnShowListener {
            dialog.changeButtonsState(false)
        }

        inputPace.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dialog.changeButtonsState(!TextUtils.isEmpty(s))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        return dialog
    }

    private inline fun EditText.ifNotEmpty(f: (Double) -> Unit) {
        if (!TextUtils.isEmpty(text))
            f(text.toString().replace(',', '.').toDouble())
    }

    private fun AlertDialog.changeButtonsState(isEnabled: Boolean) {
        getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isEnabled
        getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = isEnabled
    }

    fun setCallbacks(startCallback: (Double) -> Unit, postponeCallback: (Double) -> Unit): NewRunDialog {
        this.postponeCallback = postponeCallback
        this.startCallback = startCallback
        return this
    }

    fun setContext(context: Context): NewRunDialog {
        appContext = context
        return this
    }
}


class FirstModalDialog : DialogFragment() {
    private lateinit var yesCallback: () -> Unit
    private lateinit var noCallback: () -> Unit
    private lateinit var appContext: Context
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)

        builder.setMessage("Do you want to create a new path or to join an exisiting one?")
                .setPositiveButton("Create", { dialog, id ->
                    noCallback.invoke()
                })
                .setNegativeButton("Join", { dialog, id ->
                    yesCallback.invoke()
                })

        return builder.create()
    }

    fun setCallbacks(noCallback: () -> Unit, yesCallback: () -> Unit): FirstModalDialog {
        this.yesCallback = yesCallback
        this.noCallback = noCallback
        return this
    }

    fun setContext(context: Context): FirstModalDialog {
        appContext = context
        return this
    }
}

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    lateinit var timePickedCallback: (Int, Int) -> Unit
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

    fun setCallback(timePicked: (Int, Int) -> Unit): TimePickerFragment {
        this.timePickedCallback = timePicked
        return this
    }

}