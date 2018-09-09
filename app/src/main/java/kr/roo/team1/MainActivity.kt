package kr.roo.team1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.iid.InstanceID
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.text.DateFormat.getTimeInstance
import java.util.*
import java.util.concurrent.TimeUnit




class MainActivity : AppCompatActivity() {
    val LOG_TAG = "MainActivity"
    val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 2222

    private lateinit var mInstanceID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mInstanceID = InstanceID.getInstance(this).getId()

        mainWebView.loadUrl("https://bankco2.ga/main/")


        val fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build()

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions)
        } else {
            accessGoogleFitHistory()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFitHistory()
            }
        }
    }

    private fun accessGoogleFitHistory() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, -2)
        val startTime = cal.timeInMillis

        val readRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build()

        val googleAccount: GoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)!!

        Fitness.getHistoryClient(this, googleAccount)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener { dataReadResponse ->
                    Log.d(LOG_TAG, "onSuccess()")
                    reportGoogleFitHistory(dataReadResponse.dataPoints)
                    Log.d(LOG_TAG, dataReadResponse.toString())
                }
                .addOnFailureListener { e ->
                    Log.e(LOG_TAG, "onFailure()", e)
                }
                .addOnCompleteListener { p0 ->
                    Log.d(LOG_TAG, "onComplete()")
                    Log.d(LOG_TAG, p0.toString())
                }

//        Fitness.getHistoryClient(this, googleAccount)
//                .readDailyTotalFromLocalDevice(DataType.TYPE_STEP_COUNT_DELTA)
//                .addOnSuccessListener { dataReadResponse ->
//                    Log.d(LOG_TAG, "onSuccess()")
//                    reportGoogleFitHistory(dataReadResponse.dataPoints)
//                    Log.d(LOG_TAG, dataReadResponse.toString())
//                }
//                .addOnFailureListener { e ->
//                    Log.e(LOG_TAG, "onFailure()", e)
//                }
//                .addOnCompleteListener { p0 ->
//                    Log.d(LOG_TAG, "onComplete()")
//                    Log.d(LOG_TAG, p0.toString())
//                }

//        Fitness.getHistoryClient(this, googleAccount)
//                .readData(readRequest)
//                .addOnSuccessListener { dataReadResponse ->
//                    Log.d(LOG_TAG, "onSuccess()")
//                    reportGoogleFitHistory(dataReadResponse.buckets)
//                    Log.d(LOG_TAG, dataReadResponse.toString())
//                }
//                .addOnFailureListener { e ->
//                    Log.e(LOG_TAG, "onFailure()", e)
//                }
//                .addOnCompleteListener { p0 ->
//                    Log.d(LOG_TAG, "onComplete()")
//                    Log.d(LOG_TAG, p0.toString())
//                }
    }

    private fun reportGoogleFitHistory(dataPoints: MutableList<DataPoint>) {

        val dateFormat = getTimeInstance()

        for (dp in dataPoints) {
            Log.i(LOG_TAG, "Data point:")
            Log.i(LOG_TAG, "\tType: " + dp.dataType.name)
            Log.i(LOG_TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)))
            Log.i(LOG_TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)))
            for (field in dp.dataType.fields) {
                Log.i(LOG_TAG, "\tField: " + field.name + " Value: " + dp.getValue(field))
            }



        }

    }

    fun sendreport(step_date: String, step_count: Int) {
        "https://bankco2.ga/api/steps".httpPost(listOf(
                "step_date" to step_date,
                "step_count" to step_count,
                "instance_id" to mInstanceID))
                .responseString { request, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            Log.d("httpPost", "Failed to report %s,%s".format(step_date, step_count))
                        }
                        is Result.Success -> {
                            Log.d("httpPost", "Reported %s,%s".format(step_date, step_count))
                        }
                    }
                }
    }
}