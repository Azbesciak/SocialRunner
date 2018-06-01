package facebook.com.socialrunner

import android.content.Context
import android.widget.Toast
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.module.SimpleModule
import org.joda.time.DateTime

fun showToast(message: String, applicationContext: Context) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}

class DateTimeSerializerModule: SimpleModule("date-time") {
    init {
        addSerializer(DateTime::class.java, DateTimeSerializer())
        addDeserializer(DateTime::class.java, DateTimeDeserializer())
    }
}
@JsonDeserialize
class DateTimeDeserializer: JsonDeserializer<DateTime>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DateTime {
        val value = p.text
        return DateTime.parse(value)
    }
}

@JsonSerialize
class DateTimeSerializer: JsonSerializer<DateTime>() {
    override fun serialize(value: DateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}