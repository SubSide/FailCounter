package nl.thomasvdbulk.failcounter.entity

import com.google.firebase.database.FirebaseDatabase
import java.io.Serializable
import java.util.*

/**
 * Created by SubSide on 25-3-2018.
 */
data class Fail(var user: Int, var reason: String, var `when`: String): Serializable {
    @JvmField var id: Int? = null

    constructor(): this(0, "", "01-01-1970 00:00:00")

    fun addToFirebase(user: User){
        FirebaseDatabase.getInstance().getReference(FB_REFERENCE).push().setValue(this)
        user.addToCounter()
    }

    companion object {
        const val FB_REFERENCE = "/fails"
    }
}