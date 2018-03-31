package nl.thomasvdbulk.failcounter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_fail.*
import nl.thomasvdbulk.failcounter.entity.Fail
import nl.thomasvdbulk.failcounter.entity.User
import java.util.*

class AddFailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_fail)

        User.onCurrentUser(this, {
            add_fail_author.text = it.name
        })

        val viewModel = ViewModelProviders.of(this).get(DateTimeViewModel::class.java)
        viewModel.liveData.observe(this, Observer { _ ->
            add_fail_date.text = viewModel.getDate()
            add_fail_time.text = viewModel.getTime()
        })

        add_fail_date.setOnClickListener {
            val calendar = viewModel.liveData.value!!
            DatePickerDialog(this,
                    { _, year, month, day -> viewModel.setDate(year, month, day) },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        add_fail_time.setOnClickListener {
            val calendar = viewModel.liveData.value!!
            TimePickerDialog(this,
                    { _, hourOfDay, minutes -> viewModel.setTime(hourOfDay, minutes) },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            ).show()
        }

        add_fail_submit.setOnClickListener { saveFail(add_fail_reason.text.toString(), viewModel.formatDate()) }
    }

    private fun saveFail(reason: String, date: String){
        if(reason.trim().isBlank()){
            Toast.makeText(this, R.string.add_fail_reason_is_empty, Toast.LENGTH_LONG).show()
            return
        }

        User.onCurrentUser(this){
            Fail(it.id, reason, date).addToFirebase(it)
        }
        finish()
    }

    class DateTimeViewModel : ViewModel() {

        val liveData = MutableLiveData<Calendar>()
                .also { it.value = Calendar.getInstance() }

        fun setDate(year: Int, month: Int, day: Int){
            liveData.value!!.let {
                it.set(Calendar.YEAR, year)
                it.set(Calendar.MONTH, month)
                it.set(Calendar.DAY_OF_MONTH, day)
            }
            liveData.value = liveData.value // We send an update to the observer(s)
        }

        fun setTime(hour: Int, minute: Int){
            liveData.value!!.let {
                it.set(Calendar.HOUR_OF_DAY, hour)
                it.set(Calendar.MINUTE, minute)
            }
            liveData.value = liveData.value // We send an update to the observer(s)
        }

        fun getDate(): String {
            val calendar = liveData.value!!
            return "%02d-%02d-%d".format(
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH)+1,
                    calendar.get(Calendar.YEAR))
        }

        fun getTime(): String {
            val calendar = liveData.value!!
            return "%02d:%02d".format(
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE))
        }

        fun formatDate() = "%s %s".format(getDate(), getTime())
    }


}
