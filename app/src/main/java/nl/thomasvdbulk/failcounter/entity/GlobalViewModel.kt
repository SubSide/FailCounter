package nl.thomasvdbulk.failcounter.entity

import android.arch.lifecycle.*
import android.arch.lifecycle.Observer
import com.google.firebase.database.*
import nl.thomasvdbulk.failcounter.BaseActivity
import java.util.*


/**
 * Created by SubSide on 25-3-2018.
 */
class GlobalViewModel : ViewModel() {

    private lateinit var liveData: FBLiveData

    val dataSnapshotLiveData: LiveData<DataSnapshot>
        get() = liveData

    fun init(query: Query, limit: Int? = null, orderBy: String? = null){
        var reference = query
        if(limit != null && orderBy != null) reference = reference.limitToLast(limit).orderByChild(orderBy)
        liveData = FBLiveData(reference)
    }

    fun <T> observe(owner: LifecycleOwner, clazz: Class<T>, block: (List<T>) -> Unit){
        liveData.observe(owner, Observer<DataSnapshot>{ dataSnapshot ->
            if(dataSnapshot == null) return@Observer

            block.invoke(deserialize(dataSnapshot, clazz))
        })
    }

    fun <T> observeOnce(owner: LifecycleOwner, clazz: Class<T>, block: (List<T>) -> Unit){
        liveData.observe(owner, object: Observer<DataSnapshot>{
            override fun onChanged(dataSnapshot: DataSnapshot?) {
                if(dataSnapshot == null) return
                block.invoke(deserialize(dataSnapshot, clazz))
                liveData.removeObserver(this)
            }
        })
    }



    fun <T> deserialize(dataSnapshot: DataSnapshot, clazz: Class<T>): List<T> {
        val list = ArrayList<T>()
        dataSnapshot.children.forEach { child ->
            try {
                val item = child.getValue(clazz)!!
                try {
                    clazz.getDeclaredField("id").set(item, child.key.toString().toIntOrNull())
                } catch(e: NoSuchFieldException){}
                list.add(item)
            } catch(e: DatabaseException){}
        }
        return list
    }

    companion object {
        fun <T> observe(activity: BaseActivity, clazz: Class<T>, limit: Int? = null, orderBy: String? = null, block: (List<T>) -> Unit){
            val viewModel = ViewModelProviders.of(activity).get(GlobalViewModel::class.java)
            viewModel.init(getDatabaseReference(clazz), limit, orderBy)
            viewModel.observe(activity, clazz, block)
        }

        fun <T> observeOnce(activity: BaseActivity, clazz: Class<T>, limit: Int? = null, orderBy: String? = null, block: (List<T>) -> Unit)
        {
            val viewModel = ViewModelProviders.of(activity).get(GlobalViewModel::class.java)
            viewModel.init(getDatabaseReference(clazz), limit, orderBy)
            viewModel.observeOnce(activity, clazz, block)
        }

        private fun getDatabaseReference(clazz: Class<*>): DatabaseReference {
           return FirebaseDatabase.getInstance().getReference(clazz.getDeclaredField("FB_REFERENCE").get(null) as String)
        }
    }
}