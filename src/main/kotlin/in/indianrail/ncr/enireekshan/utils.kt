package `in`.indianrail.ncr.enireekshan

val currentTimeStamp get() = (System.currentTimeMillis()/1000)

fun createStringFromCollection(collection: Collection<Any>) : String{
    var returnString = ""
    var collectionList = collection.toMutableList()
    for(i in 1 until collectionList.size - 1) {
        returnString+=collectionList[i].toString() + ","
    }
    returnString += collectionList[collectionList.size -  1]
    return returnString
}