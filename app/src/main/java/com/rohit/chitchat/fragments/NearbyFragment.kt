package com.rohit.chitchat.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.rohit.chitchat.Models.FirebasePurchase
import com.rohit.chitchat.Models.PurchasingModel
import com.rohit.chitchat.Models.Users
import com.rohit.chitchat.MyConstants
import com.rohit.chitchat.MyConstants.PUR_199_20
import com.rohit.chitchat.MyConstants.PUR_299_30
import com.rohit.chitchat.MyConstants.PUR_399_40
import com.rohit.chitchat.MyConstants.PUR_499_50
import com.rohit.chitchat.MyConstants.PUR_99_10
import com.rohit.chitchat.MyUtils
import com.rohit.chitchat.MyUtils.applyFilterType
import com.rohit.chitchat.MyUtils.chatNearbyList
import com.rohit.chitchat.R
import com.rohit.chitchat.Security
import com.rohit.chitchat.adapters.NearbyChatAdapter
import com.rohit.chitchat.adapters.PurchasingAdapter
import com.rohit.chitchat.databinding.BottomSheetChangeDistanceBinding
import com.rohit.chitchat.databinding.BottomSheetProductPurchasingBinding
import com.rohit.chitchat.databinding.FragmentNearbyBinding
import kotlinx.android.synthetic.main.bottom_sheet_filter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


class NearbyFragment : Fragment(), PurchasesUpdatedListener {
    var selectedPosition = -1
    private var firstTime: Boolean = true
    private var purchaseToken: String? = null
    private var productId: String? = null
    private var time: String? = null
    private var amount: String? = null
    private var productList: MutableList<SkuDetails>? = null
    private var flowParams: BillingFlowParams? = null
    var PRODUCT_ID = MyConstants.PUR_199_20
    var fetchNearbyList = false
    var filterList = ArrayList<Users>()
    private var locationCallback: LocationCallback? = null
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var binding: FragmentNearbyBinding? = null
    var myLat: String = "0.0"
    var myLong: String = "0.0"
    var currentPurchase = ""
    var searchDistance = 5
    var firebaseUsers = FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
        .getReference(MyConstants.NODE_USERS)
    var firebasePurchases = FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
        .getReference(MyConstants.NODE_PURCHASES)
    var locationRequest: LocationRequest? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNearbyBinding.inflate(inflater, container, false);
        return binding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchNearbyList = false
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)

        binding!!.imgSearch.setOnClickListener {
            searchNearby()
        }

        binding!!.imgFilter.setOnClickListener {
            showBottomSheetFilter()
        }

        binding!!.btnChangeDistance.setOnClickListener {
//            showBottomSheetDistanceChange()
            currentPurchase =
                MyUtils.getStringValue(requireActivity(), MyConstants.CURRENT_SUBSCRIPTION)
            showBottomSheetDistanceChange(currentPurchase)

        }
        addItems()
        setWithInText()
        setUpBillingAccount()
        setUpPrices()

    }

    private fun addItems() {
        list = ArrayList()
        list!!.add(PurchasingModel(MyConstants.PUR_99_10, "10KM", "RS. 99", "1 Month"))
        list!!.add(PurchasingModel(PUR_199_20, "20KM", "RS. 199", "1 Month"))
        list!!.add(PurchasingModel(PUR_299_30, "30KM", "RS. 299", "1 Month"))
        list!!.add(PurchasingModel(PUR_399_40, "40KM", "RS. 399", "1 Month"))
        list!!.add(PurchasingModel(PUR_499_50, "50KM", "RS. 499", "1 Month"))
    }

    private fun showBottomSheetFilter() {
        var bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        bottomSheet.setContentView(R.layout.bottom_sheet_filter)

        if (applyFilterType.equals("Others")) {
            bottomSheet.btnOthers.setCardBackgroundColor(resources.getColor(R.color.app_color))
            bottomSheet.txtOthers.setTextColor(resources.getColor(R.color.white))
        } else if (applyFilterType.equals("Men")) {
            bottomSheet.btnMen.setCardBackgroundColor(resources.getColor(R.color.app_color))
            bottomSheet.txtMen.setTextColor(resources.getColor(R.color.white))


        } else if (applyFilterType.equals("Women")) {

            bottomSheet.btnWomen.setCardBackgroundColor(resources.getColor(R.color.app_color))
            bottomSheet.txtWomen.setTextColor(resources.getColor(R.color.white))

        } else if (applyFilterType.equals("No Filter")) {

            bottomSheet.btnNoFilter.setCardBackgroundColor(resources.getColor(R.color.app_color))
            bottomSheet.txtNoFilter.setTextColor(resources.getColor(R.color.white))
        }


        bottomSheet.btnMen.setOnClickListener {

            bottomSheet.btnMen.setCardBackgroundColor(resources.getColor(R.color.app_color))
            bottomSheet.txtMen.setTextColor(resources.getColor(R.color.white))

            bottomSheet.btnWomen.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtWomen.setTextColor(resources.getColor(R.color.black))

            bottomSheet.btnOthers.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtOthers.setTextColor(resources.getColor(R.color.black))

            bottomSheet.btnNoFilter.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtNoFilter.setTextColor(resources.getColor(R.color.black))

            applyFilterType = "Men"
            filterList =
                chatNearbyList.filter { users -> users.gender.equals("Male") } as ArrayList<Users>
            setAdapterFilterList()
            bottomSheet.cancel()
        }

        bottomSheet.btnWomen.setOnClickListener {

            bottomSheet.btnMen.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtMen.setTextColor(resources.getColor(R.color.black))

            bottomSheet.btnWomen.setCardBackgroundColor(resources.getColor(R.color.app_color))
            bottomSheet.txtWomen.setTextColor(resources.getColor(R.color.white))

            bottomSheet.btnOthers.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtOthers.setTextColor(resources.getColor(R.color.black))

            bottomSheet.btnNoFilter.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtNoFilter.setTextColor(resources.getColor(R.color.black))
            applyFilterType = "Women"
            filterList =
                chatNearbyList.filter { users -> users.gender.equals("Female") } as ArrayList<Users>
            setAdapterFilterList()
            bottomSheet.cancel()

        }


        bottomSheet.btnOthers.setOnClickListener {

            bottomSheet.btnWomen.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtWomen.setTextColor(resources.getColor(R.color.black))

            bottomSheet.btnMen.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtMen.setTextColor(resources.getColor(R.color.black))

            bottomSheet.btnOthers.setCardBackgroundColor(resources.getColor(R.color.app_color))
            bottomSheet.txtOthers.setTextColor(resources.getColor(R.color.white))

            bottomSheet.btnNoFilter.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtNoFilter.setTextColor(resources.getColor(R.color.black))

            applyFilterType = "Others"
            filterList =
                chatNearbyList.filter { users -> users.gender.equals("Others") } as ArrayList<Users>
            setAdapterFilterList()
            bottomSheet.cancel()

        }


        bottomSheet.btnNoFilter.setOnClickListener {

            bottomSheet.btnWomen.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtWomen.setTextColor(resources.getColor(R.color.black))

            bottomSheet.btnMen.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtMen.setTextColor(resources.getColor(R.color.black))

            bottomSheet.btnOthers.setCardBackgroundColor(resources.getColor(R.color.white))
            bottomSheet.txtOthers.setTextColor(resources.getColor(R.color.black))

            bottomSheet.btnNoFilter.setCardBackgroundColor(resources.getColor(R.color.app_color))
            bottomSheet.txtNoFilter.setTextColor(resources.getColor(R.color.white))

            applyFilterType = "No Filter"
            filterList = chatNearbyList
            setAdapterFilterList()
            bottomSheet.cancel()

        }
        bottomSheet.show()
    }

    private fun setAdapterFilterList() {
        binding!!.recyclerChatList.adapter =
            NearbyChatAdapter(requireActivity(), filterList!!)
    }

    private fun setAdapter() {
        var activity = getActivity();
        if (activity != null && isAdded()) {
            if (chatNearbyList.size == 0) {
                binding!!.txtNoOneFound.visibility = View.VISIBLE
            } else {
                binding!!.txtNoOneFound.visibility = View.GONE
            }
            binding!!.recyclerChatList.adapter =
                NearbyChatAdapter(activity, chatNearbyList!!)
        }
    }

    private fun searchNearby() {
        binding!!.txtNoOneFound.visibility = View.GONE
        binding!!.rippleEffect.startRippleAnimation()
        binding!!.rippleEffect.visibility = View.VISIBLE
        binding!!.recyclerChatList.visibility = View.INVISIBLE
        binding!!.imgSearch.visibility = View.INVISIBLE
        binding!!.imgFilter.visibility = View.INVISIBLE
//        MyUtils.showProgress(requireActivity())
        myLat = MyUtils.getStringValue(requireActivity(), MyConstants.USER_LATITUDE)
        myLong = MyUtils.getStringValue(requireActivity(), MyConstants.USER_LONGITUDE)
        if (!myLat.isNullOrEmpty() && !myLong.isNullOrEmpty()) {
            val queryRef: Query = firebaseUsers.orderByChild("ghostMode").equalTo("off")
            queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                MyUtils.stopProgress(requireActivity())
                    if (snapshot.exists()) {
                        chatNearbyList.clear()
                        for (postSnapshot in snapshot.children) {
                            val user: Users? =
                                postSnapshot.getValue(Users::class.java)

                            Log.d(
                                "mylog DISTANCE", (getKmFromLatLong(
                                    myLat.toFloat(),
                                    myLong.toFloat(),
                                    user!!.lat!!.toFloat(),
                                    user!!.long!!.toFloat()
                                )).toString()
                            )
                            if (!user!!.phone.equals(
                                    MyUtils.getStringValue(
                                        requireActivity(),
                                        MyConstants.USER_PHONE
                                    )
                                ) && !myLat.equals("") && (getKmFromLatLong(
                                    myLat.toFloat(),
                                    myLong.toFloat(),
                                    user!!.lat!!.toFloat(),
                                    user!!.long!!.toFloat()
                                )) <= searchDistance
                            ) {
                                chatNearbyList.add(user!!)
                            }


                            // sort by distance
                            chatNearbyList.sortBy {
                                getKmFromLatLong(
                                    myLat.toFloat(),
                                    myLong.toFloat(),
                                    it!!.lat!!.toFloat(),
                                    it!!.long!!.toFloat()
                                )
                            }
                            /** check filter **/
                            Handler().postDelayed({
                                binding!!.rippleEffect.stopRippleAnimation()
                                binding!!.rippleEffect.visibility = View.INVISIBLE
                                binding!!.recyclerChatList.visibility = View.VISIBLE
                                binding!!.imgSearch.visibility = View.VISIBLE
                                binding!!.imgFilter.visibility = View.VISIBLE
                                if (applyFilterType.equals("No Filter")) {
                                    setAdapter()
                                } else if (applyFilterType.equals("Men")) {
                                    filterList =
                                        chatNearbyList.filter { users -> users.gender.equals("Male") } as ArrayList<Users>
                                    setAdapterFilterList()
                                } else if (applyFilterType.equals("Women")) {
                                    filterList =
                                        chatNearbyList.filter { users -> users.gender.equals("Female") } as ArrayList<Users>
                                    setAdapterFilterList()
                                } else if (applyFilterType.equals("Others")) {
                                    filterList =
                                        chatNearbyList.filter { users -> users.gender.equals("Others") } as ArrayList<Users>
                                    setAdapterFilterList()
                                }

                            }, 3000)

//                        binding!!.rippleEffect.stopRippleAnimation()
//                        binding!!.rippleEffect.visibility = View.INVISIBLE
                            // here you can access to name property like university.name
                        }


                    } else {
                        binding!!.rippleEffect.visibility = View.INVISIBLE
                        binding!!.recyclerChatList.visibility = View.VISIBLE
                        binding!!.imgSearch.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding!!.rippleEffect.visibility = View.INVISIBLE
                    binding!!.recyclerChatList.visibility = View.VISIBLE
                    binding!!.imgSearch.visibility = View.VISIBLE
                }

            })
        }
    }

    fun getKmFromLatLong(lat1: Float, lng1: Float, lat2: Float, lng2: Float): Float {
        val loc1 = Location("")
        loc1.latitude = lat1.toDouble()
        loc1.longitude = lng1.toDouble()
        val loc2 = Location("")
        loc2.latitude = lat2.toDouble()
        loc2.longitude = lng2.toDouble()
        val distanceInMeters = loc1.distanceTo(loc2)
        return distanceInMeters / 1000
    }


    fun checkLocationPermission() {
        var alertType = ""
        lateinit var perms: Array<String>
        alertType = "This app needs access to your Location"
        perms = arrayOf<String>(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val rationale = alertType
        val options: Permissions.Options = Permissions.Options()
            .setRationaleDialogTitle("Info")
            .setSettingsDialogTitle("Warning")

        Permissions.check(
            requireActivity()/*context*/,
            perms,
            rationale,
            options,
            object : PermissionHandler() {
                override fun onGranted() {
                    when {
                        MyUtils.isLocationEnabled(requireContext()) -> {
                            try {
                                setUpLocationListener()
                            } catch (e: Exception) {

                            }
                        }
                        else -> {
                            MyUtils.showGPSNotEnabledDialog(requireContext())
                        }
                    }
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String?>?
                ) {
                    checkLocationPermission()
                }
            })


    }


    override fun onResume() {
        super.onResume()
        checkLocationPermission()
    }

    private fun setUpLocationListener() {
        // for getting the current location update after every 2 seconds with high accuracy
        locationRequest = LocationRequest().setInterval(10000).setFastestInterval(10000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                var lat = ""
                var longi = ""
                for (location in locationResult.locations) {
                    lat = location.latitude.toString()
                    longi = location.longitude.toString()
                }


                var users: Users = Users();
//                users.name = MyUtils.getStringValue(requireContext(), MyConstants.USER_NAME)
                users.phone = MyUtils.getStringValue(requireActivity(), MyConstants.USER_PHONE)
//                users.image = MyUtils.getStringValue(requireContext(), MyConstants.USER_IMAGE)
//                users.captions = MyUtils.getStringValue(requireContext(), MyConstants.USER_CAPTIONS)
//                users.ghostMode=MyUtils.getStringValue(requireContext(),MyConstants.GHOST_MODE)
//                users.token=MyUtils.getStringValue(requireContext(),MyConstants.OTHER_USER_TOKEN)
//                users.lat = lat!!
//                users.long = longi!!
                if (users.phone != null && !users.phone.equals("")) {
                    firebaseUsers.child(users.phone.toString()).child("lat").setValue(lat)
                    firebaseUsers.child(users.phone.toString()).child("long").setValue(longi)
                }
                MyUtils.saveStringValue(
                    requireContext(),
                    MyConstants.USER_LATITUDE,
                    lat
                )
                MyUtils.saveStringValue(
                    requireContext(),
                    MyConstants.USER_LONGITUDE,
                    longi
                )

                if (!fetchNearbyList) {
                    fetchNearbyList = true
                    searchNearby()
                }

                // Few more things we can do here:
                // For example: Update the location of user on server
            }
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
        if (fusedLocationProviderClient != null && locationCallback != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
        if (fusedLocationProviderClient != null && locationCallback != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    override fun onDetach() {
        super.onDetach()
        if (fusedLocationProviderClient != null && locationCallback != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    var list: ArrayList<PurchasingModel>? = null
    var purchaseBottomsheet: BottomSheetDialog? = null
    fun showPurchasingBottomsheet() {
        purchaseBottomsheet =
            BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        var mPurchasingBinding =
            BottomSheetProductPurchasingBinding.inflate(layoutInflater, null, false)


        purchaseBottomsheet!!.setContentView(mPurchasingBinding!!.root)
        purchaseBottomsheet!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)


        var adapter = PurchasingAdapter(requireContext(), list!!, object : PurchasingAdapter.Click {
            override fun onClickItem(position: Int) {
                selectedPosition = position
            }
        })
        mPurchasingBinding.rcPurchaseItems.adapter = adapter
        purchaseBottomsheet!!.show()

        mPurchasingBinding.btnPurchase.setOnClickListener {
            firstTime = true
            if (selectedPosition == -1) {
                MyUtils.showToast(requireContext(), "Please select a plan")
            } else {
                if (productList != null && productList!!.size > 0) {
                    PRODUCT_ID= list!![selectedPosition].PurchaseType!!
                        flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(productList!![selectedPosition])
                            .build()
                        billingClient!!.launchBillingFlow(requireActivity(), flowParams!!)
                } else {
                    MyUtils.showToast(
                        requireContext(),
                        "In App purchase is not ready yet, Try again later"
                    )
                }
            }
        }


//
//            if (productList != null && productList!!.size > 0) {
//                if (PRODUCT_ID.equals(MyConstants.PUR_99_10)) {
//                    flowParams = BillingFlowParams.newBuilder()
//                        .setSkuDetails(productList!![1])
//                        .build()
//                    billingClient!!.launchBillingFlow(requireActivity(), flowParams!!)
//                } else {
//                    flowParams = BillingFlowParams.newBuilder()
//                        .setSkuDetails(productList!![0])
//                        .build()
//                    billingClient!!.launchBillingFlow(requireActivity(), flowParams!!)
//                }
//            } else {
//                Toast.makeText(requireContext(), "Wait for fetching details", Toast.LENGTH_SHORT).show()
//            }
//        }

    }

    fun showBottomSheetDistanceChange(currentPurchase: String) {
        var bottomDistance =
            BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        var mBottomSheetBinding =
            BottomSheetChangeDistanceBinding.inflate(layoutInflater, null, false)

        bottomDistance.setContentView(mBottomSheetBinding!!.root)
        bottomDistance.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mBottomSheetBinding.sliderDistance.value = searchDistance.toFloat()
        var selectedDistance = if(MyUtils.getStringValue(
            requireContext(),
            MyConstants.SEARCH_DISTANCE
        ).equals("") ) 5 else MyUtils.getStringValue(
                requireContext(),
                MyConstants.SEARCH_DISTANCE
            ).toInt()


        mBottomSheetBinding.sliderDistance.valueFrom = 1F
        if (currentPurchase.equals(PUR_99_10)) {
            mBottomSheetBinding.sliderDistance.valueTo = 10F
            if (selectedDistance.toInt() > 10) {
                mBottomSheetBinding.sliderDistance.value = 10F
                MyUtils.saveStringValue(
                    requireContext(),
                    MyConstants.SEARCH_DISTANCE,
                    "10"
                )
            }
        } else if (currentPurchase.equals(PUR_199_20)) {
            mBottomSheetBinding.sliderDistance.valueTo = 20F
            if (selectedDistance.toInt() > 20) {
                mBottomSheetBinding.sliderDistance.value = 20F
                MyUtils.saveStringValue(
                    requireContext(),
                    MyConstants.SEARCH_DISTANCE,
                    "20"
                )
            }
        } else if (currentPurchase.equals(PUR_299_30)) {
            mBottomSheetBinding.sliderDistance.valueTo = 30F
            if (selectedDistance.toInt() > 30) {
                mBottomSheetBinding.sliderDistance.value = 30F
                MyUtils.saveStringValue(
                    requireContext(),
                    MyConstants.SEARCH_DISTANCE,
                    "30"
                )
            }
        } else if (currentPurchase.equals(PUR_399_40)) {
            mBottomSheetBinding.sliderDistance.valueTo = 40F
            if (selectedDistance.toInt() > 40) {
                mBottomSheetBinding.sliderDistance.value = 40F
                MyUtils.saveStringValue(
                    requireContext(),
                    MyConstants.SEARCH_DISTANCE,
                    "40"
                )
            }
        } else if (currentPurchase.equals(PUR_499_50)) {
            mBottomSheetBinding.sliderDistance.valueTo = 50F
            if (selectedDistance.toInt() > 50) {
                mBottomSheetBinding.sliderDistance.value = 50F
                MyUtils.saveStringValue(
                    requireContext(),
                    MyConstants.SEARCH_DISTANCE,
                    "50"
                )
            }
        } else {
            mBottomSheetBinding.sliderDistance.valueTo = 5F
            if (selectedDistance.toInt() > 5) {
                mBottomSheetBinding.sliderDistance.value = 5F
                MyUtils.saveStringValue(
                    requireContext(),
                    MyConstants.SEARCH_DISTANCE,
                    "5"
                )
            }
        }


//        mBottomSheetBinding.sliderDistance.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
//            override fun onStartTrackingTouch(slider: Slider) {
//
//            }
//
//            override fun onStopTrackingTouch(slider: Slider) {
//            }
//
//        })

//        mBottomSheetBinding.sliderDistance.addOnChangeListener { slider, value, fromUser ->
//           bottomDistance.cancel()
//
//        }


        mBottomSheetBinding.btnUpdatePlan.setOnClickListener {
            bottomDistance.dismiss()
            showPurchasingBottomsheet()
        }

        mBottomSheetBinding.btnChange.setOnClickListener {
            searchDistance = mBottomSheetBinding.sliderDistance.value.toInt()
            MyUtils.saveStringValue(
                requireContext(),
                MyConstants.SEARCH_DISTANCE,
                searchDistance.toString()
            )
            setWithInText()
            searchNearby()
            bottomDistance.cancel()
        }
        bottomDistance.show()
    }

    private fun setWithInText() {
        searchDistance =
            if (MyUtils.getStringValue(requireContext(), MyConstants.SEARCH_DISTANCE).equals("")) 5
            else MyUtils.getStringValue(requireContext(), MyConstants.SEARCH_DISTANCE).toInt()
        binding!!.txtWithIn.text = "Within $searchDistance KM"
    }


    private fun setUpPrices() {
        if (billingClient!!.isReady()) {
            initiatePurchase();
        }
        //else reconnect service
        else {
            billingClient = BillingClient.newBuilder(requireContext()).enablePendingPurchases()
                .setListener(this).build();
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    Toast.makeText(requireContext(), "Error ", Toast.LENGTH_SHORT)
                        .show();
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase();
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error " + billingResult.getDebugMessage(),
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            });
        }
    }

    private var billingClient: BillingClient? = null
    private fun setUpBillingAccount() {
        billingClient = BillingClient.newBuilder(requireContext())
            .enablePendingPurchases().setListener(this).build()
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val queryPurchase = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }




    fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            //if item is purchased
            if (PRODUCT_ID == purchase.skus.get(0) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                    Log.d("mySubscription", "billing response 4")
                    Toast.makeText(
                        requireContext(),
                        "Error : Invalid Purchase",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                productId = PRODUCT_ID
                purchaseToken = purchase.purchaseToken
                Log.d("mylog purchaseToken", purchaseToken.toString())
                time = purchase.purchaseTime.toString()


                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            // Update the appropriate tables/databases to grant user the items
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.MONTH, 1)
                                var purchaseData = FirebasePurchase(
                                    list!!.get(selectedPosition).PurchaseType.toString(),
                                    list!!.get(selectedPosition).itemPrice.toString(),
                                    Calendar.getInstance().timeInMillis.toString(),
                                    calendar.timeInMillis.toString(),
                                    purchaseToken.toString()
                                )
                                MyUtils.showProgress(requireContext())
                                firebasePurchases.child(
                                    MyUtils.getStringValue(
                                        requireActivity(),
                                        MyConstants.USER_PHONE
                                    )
                                ).setValue(purchaseData).addOnSuccessListener {
                                    MyUtils.stopProgress(requireContext())
                                    purchaseBottomsheet!!.dismiss()
                                    MyUtils.saveStringValue(
                                        requireContext(),
                                        MyConstants.CURRENT_SUBSCRIPTION,
                                        list!!.get(selectedPosition).PurchaseType.toString()
                                    )
                                    MyUtils.showToast(requireContext(), "Successfully purchase, Enjoy")
                                    showBottomSheetDistanceChange(list!!.get(selectedPosition).PurchaseType.toString())

                                    /// PURCHASING CALL HERE
                                }
                        }
                        else -> {
                            Log.w("TAG_INAPP", billingResult.debugMessage)
                        }
                    }
                }



//                }
            } else if (PRODUCT_ID == purchase.skus.get(0) && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                Toast.makeText(
                    requireContext(),
                    "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT
                ).show()
            } else if (PRODUCT_ID == purchase.skus.get(0) && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                Toast.makeText(
                    requireContext(),
                    "Purchase Status Unknown",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            val base64Key = MyConstants.BASE_64_KEY
            return Security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }


    private fun initiatePurchase() {
        val skuList: MutableList<String> = ArrayList()
        skuList.add(MyConstants.PUR_99_10)
        skuList.add(MyConstants.PUR_199_20)
        skuList.add(MyConstants.PUR_299_30)
        skuList.add(MyConstants.PUR_399_40)
        skuList.add(MyConstants.PUR_499_50)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient!!.querySkuDetailsAsync(
            params.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size > 0) {
                    productList = skuDetailsList
                    list!!.forEachIndexed { index, purchasingModel ->
                        list!!.get(index).itemPrice = productList!!.get(index).originalPrice
                    }
                } else {
                    //try to add item/product id "purchase" inside managed product in google play console
                    Toast.makeText(
                        requireContext(),
                        "Purchase Item not Found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setData(skuDetailsList: List<SkuDetails>) {
        Log.d("mylog", skuDetailsList.toString())
        Log.d("mylog price", skuDetailsList.get(1).originalPrice)
        Log.d("mylog price", skuDetailsList.get(0).originalPrice)
        requireActivity().runOnUiThread {
//            binding!!.txtMonthlyPrice.text = skuDetailsList.get(1).originalPrice.toString()
//            binding!!.txtAnuallyPrice.text = skuDetailsList.get(0).originalPrice.toString()
        }

    }


    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
            if (p1 != null && p1.size > 0) {
                handlePurchases(p1)
            } else {
//                        savePurchaseValueToPref(false)
            }
        }
    }


}