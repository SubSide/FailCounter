package nl.thomasvdbulk.failcounter.entity

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import nl.thomasvdbulk.failcounter.BaseActivity
import java.io.Serializable

/**
 * Created by SubSide on 25-3-2018.
 */
data class User(val name: String, val email: String, val count: Int) : Serializable {
    constructor(): this("", "", 0)

    @JvmField
    var id: Int = 0

    fun addToCounter(){
        FirebaseDatabase.getInstance().getReference(FB_REFERENCE).child(id.toString() + "/count").runTransaction(object: Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Int::class.java)
                if (currentValue == null) {
                    mutableData.value = 1
                } else {
                    mutableData.value = currentValue + 1
                }

                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {}
        })
    }

    companion object {
        const val FB_REFERENCE = "/users"

        fun onCurrentUser(activity: BaseActivity, block: (User) -> Unit){
            if(FirebaseAuth.getInstance().currentUser == null) return

            GlobalViewModel.observeOnce(activity, User::class.java){ list ->
                list.forEach {
                    if(it.email == FirebaseAuth.getInstance().currentUser!!.email) block.invoke(it)
                }
            }
        }
    }
}