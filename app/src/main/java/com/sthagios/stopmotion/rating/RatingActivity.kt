package com.sthagios.stopmotion.rating

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   20.06.16
 */
class RatingActivity : AppCompatActivity(), RatingDialog.Callback {
    override fun giveFeedback(feedback: Boolean) {
        if (feedback) {
            Snackbar.make(findViewById(android.R.id.content)!!, "oke", Snackbar.LENGTH_LONG).show()
        } else
            Snackbar.make(findViewById(android.R.id.content)!!, "meh", Snackbar.LENGTH_LONG).show()

    }

    override fun rateTheApp(rate: Boolean) {
        if (rate) {
            Snackbar.make(findViewById(android.R.id.content)!!, "YEAH", Snackbar.LENGTH_LONG).show()
        } else
            Snackbar.make(findViewById(android.R.id.content)!!, "Nej", Snackbar.LENGTH_LONG).show()

    }

    override fun enjoyTheApp(enjoy: Boolean) {
        if (enjoy) {
            mRatingRate.show(fragmentManager, "rate")
        } else {
            mRatingFeedback.show(fragmentManager, "Show")

        }
    }

    val mRatingFirst = RatingDialogFirst()
    val mRatingFeedback = RatingDialogFeedback()
    val mRatingRate = RatingDialogRate()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRatingFirst.show(fragmentManager, "BLO")

    }
}