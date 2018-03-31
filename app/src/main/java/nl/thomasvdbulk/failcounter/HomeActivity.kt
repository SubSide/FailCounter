package nl.thomasvdbulk.failcounter

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.home_activity.*
import nl.thomasvdbulk.failcounter.entity.Fail
import nl.thomasvdbulk.failcounter.entity.GlobalViewModel
import nl.thomasvdbulk.failcounter.entity.User
import java.util.*


/**
 * Created by SubSide on 25-3-2018.
 */
class HomeActivity : BaseActivity() {
    private lateinit var failRecyclerView: RecyclerView
    private lateinit var failViewAdapter: RecyclerView.Adapter<*>
    private lateinit var failViewManager: RecyclerView.LayoutManager
    private var lastDate = ""
    private val mUsers: MutableList<User> = ArrayList()
    private val mFails: MutableList<Fail> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)


        failViewManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true).also {
            it.stackFromEnd = true
        }
        failViewAdapter = FailAdapter()

        failRecyclerView = findViewById<RecyclerView>(R.id.fail_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = failViewManager

            // specify an failViewAdapter (see also next example)
            adapter = failViewAdapter

        }

        GlobalViewModel.observe(this, User::class.java){ list ->
            mUsers.clear()
            mUsers.addAll(list)
            findViewById<LinearLayout>(R.id.users).removeAllViewsInLayout()
            list.forEach { user ->
                findViewById<LinearLayout>(R.id.users).let {
                    it.addView(
                        layoutInflater.inflate(R.layout.user_item, it, false).also { view ->
                            view.findViewById<TextView>(R.id.user_name).text = user.name
                            view.findViewById<TextView>(R.id.user_counter).text = user.count.toString()
                        }
                    )
                }
            }
        }

        GlobalViewModel.observe(this, Fail::class.java, 10, "when"){ list ->
            mFails.clear()
            mFails.addAll(list)
            failViewAdapter.notifyDataSetChanged()
            if(mFails.size > 0)
                failRecyclerView.smoothScrollToPosition(mFails.size-1)
        }

        fab_button.setOnClickListener {
            if(FirebaseAuth.getInstance().currentUser != null) {
                startActivity(Intent(this, AddFailActivity::class.java))
            } else {
                Toast.makeText(this, getString(R.string.error_not_logged_in), Toast.LENGTH_SHORT).show()
            }
        }
    }


    inner class FailAdapter: RecyclerView.Adapter<FailAdapter.ViewHolder>() {

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        inner class ViewHolder(val view: ConstraintLayout) : RecyclerView.ViewHolder(view)


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): FailAdapter.ViewHolder {
            // create a new view
            val textView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.fail_adapter_item, parent, false) as ConstraintLayout
            return ViewHolder(textView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.view.findViewById<TextView>(R.id.who).text = mUsers.find{ it.id == mFails[position].user }?.name
            holder.view.findViewById<TextView>(R.id.reason).text = mFails[position].reason
            holder.view.findViewById<TextView>(R.id.`when`).text = mFails[position].`when`

            setAnimation(holder.itemView, mFails[position].`when`)
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = mFails.size
    }

    /**
     * Here is the key method to apply the animation
     */
    fun setAnimation(viewToAnimate: View, date: String?) {
        if(date == null) return
        // If the bound view wasn't previously displayed on screen, it's animated
        if (date > lastDate) {
            val animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastDate = date
        }
    }
}