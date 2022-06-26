package com.rohit.chitForChat.fragments

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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.rohit.chitForChat.Models.Users
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.MyUtils
import com.rohit.chitForChat.MyUtils.applyFilterType
import com.rohit.chitForChat.MyUtils.chatNearbyList
import com.rohit.chitForChat.R
import com.rohit.chitForChat.adapters.NearbyChatAdapter
import com.rohit.chitForChat.databinding.BottomSheetChangeDistanceBinding
import com.rohit.chitForChat.databinding.FragmentNearbyBinding
import kotlinx.android.synthetic.main.bottom_sheet_filter.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class NearbyFragment : Fragment() {
    var fetchNearbyList = false
    var filterList = ArrayList<Users>()
    private var locationCallback: LocationCallback? = null
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var binding: FragmentNearbyBinding? = null
    var myLat: String = "0.0"
    var myLong: String = "0.0"
    var searchDistance = 1
    var firebaseUsers = FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
        .getReference(MyConstants.NODE_USERS)
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
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)

        binding!!.imgSearch.setOnClickListener {
            searchNearby()
        }

        binding!!.imgFilter.setOnClickListener {
            showBottomSheetFilter()
        }

        binding!!.btnChangeDistance.setOnClickListener {
            showBottomSheetDistanceChange()
        }

        setWithInText()
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
        if (fusedLocationProviderClient != null && locationCallback != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fusedLocationProviderClient != null && locationCallback != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    override fun onDetach() {
        super.onDetach()
        if (fusedLocationProviderClient != null && locationCallback != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    fun showBottomSheetDistanceChange() {
        var bottomDistance =
            BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        var mBottomSheetBinding =
            BottomSheetChangeDistanceBinding.inflate(layoutInflater, null, false)

        bottomDistance.setContentView(mBottomSheetBinding!!.root)
        bottomDistance.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mBottomSheetBinding.sliderDistance.value = searchDistance.toFloat()
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
            if (MyUtils.getStringValue(requireContext(), MyConstants.SEARCH_DISTANCE).equals("")) 1
            else MyUtils.getStringValue(requireContext(), MyConstants.SEARCH_DISTANCE).toInt()
        binding!!.txtWithIn.text = "Within $searchDistance KM"
    }
}