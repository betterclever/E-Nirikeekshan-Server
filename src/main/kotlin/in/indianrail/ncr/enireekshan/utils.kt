package `in`.indianrail.ncr.enireekshan

val currentTimeStamp get() = (System.currentTimeMillis()/1000)

fun createStringFromCollection(collection: Collection<Any>) : String{
    var returnString = ""
    collection.forEach {
        returnString+=it.toString()
    }
    return returnString
}