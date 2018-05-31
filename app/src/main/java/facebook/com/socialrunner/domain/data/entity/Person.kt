package facebook.com.socialrunner.domain.data.entity

data class Person
    (
            val name : String = "",
            val pace : Double = 0.0,
            val longitude : Double = 0.0,
            val latitude : Double = 0.0,
            val routeID : String = "",
            val isRunning : Boolean = false
    )