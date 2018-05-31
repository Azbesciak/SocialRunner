package facebook.com.socialrunner.domain.data.entity


open class Entity()
{
    var id: String? = null
    fun <T>setID(id : String, obj : T) : T{
        this.id = id
        return obj
    }
}