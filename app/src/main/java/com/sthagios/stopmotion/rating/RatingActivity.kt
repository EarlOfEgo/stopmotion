package com.sthagios.stopmotion.rating

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.tracking.logRatingEvent

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   20.06.16
 */
class RatingActivity : AppCompatActivity(), RatingDialog.Callback {
    override fun giveFeedback(feedback: Boolean) {
        logRatingEvent("feedback", feedback)
        if (feedback) {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.type = "message/rfc822"
            emailIntent.data = Uri.parse("mailto:" + "stopmotion@sthagios.com")
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.rating_feedback_email_title))

            try {
                startActivity(Intent.createChooser(emailIntent,
                        getString(R.string.rating_email_choose_text)))
            } catch (ex: android.content.ActivityNotFoundException) {
                //TODO might add toast
            }
        } else {
            Toast.makeText(this, "Thanks anyway", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun rateTheApp(rate: Boolean) {
        logRatingEvent("rate", rate)
        if (rate) {
            val uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket);
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
            }
        } else {
            Toast.makeText(this, "Thanks anyway", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun enjoyTheApp(enjoy: Boolean) {
        logRatingEvent("enjoyed", enjoy)
        if (enjoy) {
            mRatingRate.show(fragmentManager, "RatingDialogRate")
        } else {
            mRatingFeedback.show(fragmentManager, "RatingDialogFeedback")
        }
    }

    val mRatingFirst = RatingDialogFirst()
    val mRatingFeedback = RatingDialogFeedback()
    val mRatingRate = RatingDialogRate()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRatingFirst.show(fragmentManager, "RatingDialogFirst")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        logRatingEvent("back_pressed", true)
        finish()
    }
}