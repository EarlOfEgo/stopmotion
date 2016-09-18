package com.sthagios.stopmotion.purchases

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.android.vending.billing.IInAppBillingService
import rx.Observable
import java.util.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   17.09.16
 */

fun Context.bindService() {
    val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
    serviceIntent.`package` = "com.android.vending"

    bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE)
}

var mService: IInAppBillingService? = null

val mServiceConn = object : ServiceConnection {

    override fun onServiceDisconnected(name: ComponentName?) {
        mService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        mService = IInAppBillingService.Stub.asInterface(service)
    }

}

fun Context.getItems(): Observable<Bundle> {
    val skuList = ArrayList<String>()
    skuList.add("premiumUpgrade")
    val querySkus = Bundle()
    querySkus.putStringArrayList("ITEM_ID_LIST", skuList)
    return Observable.just(mService?.getSkuDetails(3, packageName, "inapp", querySkus))
}

val BILLING_RESPONSE_RESULT_OK = 0                  //Success
val BILLING_RESPONSE_RESULT_USER_CANCELED = 1       //User pressed back or canceled a dialog
val BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2 //Network connection is down
val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3 //Billing API version is not supported for the type requested
val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4    //Requested product is not available for purchase
val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5     //Invalid arguments provided to the API. This error can also indicate that the application was not correctly signed or properly set up for In-app Billing in Google Play, or does not have the necessary permissions in its manifest
val BILLING_RESPONSE_RESULT_ERROR = 6               //Fatal error during the API action
val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7  //Failure to purchase since item is already owned
val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8      //Failure to consume since item is not owned
