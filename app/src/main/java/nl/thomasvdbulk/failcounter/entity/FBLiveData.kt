package nl.thomasvdbulk.failcounter.entity

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.database.*


/**
 * Created by SubSide on 25-3-2018.
 */
class FBLiveData : LiveData<DataSnapshot> {

    private val query: Query
    private val listener = MyValueEventListener()

    constructor(query: Query) {
        this.query = query
    }

    constructor(ref: DatabaseReference) {
        this.query = ref
    }

    override fun onActive() {
        query.addValueEventListener(listener)
    }

    override fun onInactive() {
        query.removeEventListener(listener)
    }

    private inner class MyValueEventListener : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            value = dataSnapshot
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e(LOG_TAG, "Can't listen to query " + query, databaseError.toException())
        }
    }

    companion object {
        private val LOG_TAG = "FirebaseQueryLiveData"
    }
}