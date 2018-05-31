package facebook.com.socialrunner.domain.data.entity

import com.google.firebase.database.GenericTypeIndicator


data class Runner(
             val name: String = "",
             var routeIds: MutableList<String> = arrayListOf(),
             val pace : Double = 0.0,
             val longitude : Double = 0.0,
             val latitude : Double = 0.0,
             val routeID : String = "",
             val isRunning : Boolean = false
) : Entity()
{
    fun position() : Position = Position(longitude = longitude, latitude = latitude)
}
