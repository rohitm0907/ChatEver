package com.rohit.chatever.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.rohit.chatever.Models.ContactModel
import com.rohit.chatever.Models.Users
import com.rohit.chatever.MyConstants
import com.rohit.chatever.MyUtils
import com.rohit.chatever.MyUtils.listAllUsers
import com.rohit.chatever.adapters.ContactsAdapter
import com.rohit.chatever.databinding.FragmentContactsBinding
import java.util.regex.Pattern


class ContactsFragment : Fragment() {
    var firebaseUsers = FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
        .getReference(MyConstants.NODE_USERS)
    var listContacts: ArrayList<ContactModel> = ArrayList()
    var filterContacts: ArrayList<ContactModel> = ArrayList()

    var binding: FragmentContactsBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkContactPermission()
        handleSearching()

    }

    private fun handleSearching() {
        binding!!.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searching(s)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun searching(searchText: CharSequence?) {

        filterContacts = listContacts.filter { contactModel ->
            contactModel.name!!.toLowerCase()!!.contains(
                searchText.toString().toLowerCase()
            ) || contactModel.mobileNumber!!.toLowerCase()
                .contains(searchText.toString().toLowerCase())
        } as ArrayList<ContactModel>

        setAdapter()


    }


    fun checkContactPermission() {
        var alertType = ""
        lateinit var perms: Array<String>

        alertType = "This app needs access to your contacts"
        perms = arrayOf<String>(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_CONTACTS
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
                    if (MyUtils.listAllUsersNumbers.size > 0) {
                        try {
                            getContacts()
                        } catch (e: Exception) {

                        }
                    } else {
                        fetchAllUsers()
                    }
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String?>?
                ) {
                    // permission denied, block the feature.
                }
            })


    }

    fun fetchAllUsers() {
        firebaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                MyUtils.stopProgress(requireActivity())
                if (snapshot.exists()) {
                    MyUtils.listAllUsersNumbers.clear()
                    for (postSnapshot in snapshot.children) {
                        val user: Users? =
                            postSnapshot.getValue(Users::class.java)
                        if (!user!!.phone.equals(
                                MyUtils.getStringValue(
                                    requireActivity(),
                                    MyConstants.USER_PHONE
                                )
                            )
                        ) {
                            listAllUsers.add(user!!)
                            MyUtils.listAllUsersNumbers.add(
                                user!!.phone.toString().replace("+", "")
                            )

                            MyUtils.listAllUsersNumbersWithoutCode.add(PhoneNumberWithoutCountryCode(user!!.phone.toString().replace("+", "")).toString())
                        }
                    }

                    try {
                        getContacts()
                    } catch (e: Exception) {

                    }
                } else {
                    try {
                        getContacts()
                    } catch (e: Exception) {

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong, Please try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    fun PhoneNumberWithoutCountryCode(phoneNumberWithCountryCode: String): String? { //+91 7698989898
        val compile: Pattern = Pattern.compile(
            "\\+(?:998|996|995|994|993|992|977|976|975|974|973|972|971|970|968|967|966|965|964|963|962|961|960|886|880|856|855|853|852|850|692|691|690|689|688|687|686|685|683|682|681|680|679|678|677|676|675|674|673|672|670|599|598|597|595|593|592|591|590|509|508|507|506|505|504|503|502|501|500|423|421|420|389|387|386|385|383|382|381|380|379|378|377|376|375|374|373|372|371|370|359|358|357|356|355|354|353|352|351|350|299|298|297|291|290|269|268|267|266|265|264|263|262|261|260|258|257|256|255|254|253|252|251|250|249|248|246|245|244|243|242|241|240|239|238|237|236|235|234|233|232|231|230|229|228|227|226|225|224|223|222|221|220|218|216|213|212|211|98|95|94|93|92|91|90|86|84|82|81|66|65|64|63|62|61|60|58|57|56|55|54|53|52|51|49|48|47|46|45|44\\D?1624|44\\D?1534|44\\D?1481|44|43|41|40|39|36|34|33|32|31|30|27|20|7|1\\D?939|1\\D?876|1\\D?869|1\\D?868|1\\D?849|1\\D?829|1\\D?809|1\\D?787|1\\D?784|1\\D?767|1\\D?758|1\\D?721|1\\D?684|1\\D?671|1\\D?670|1\\D?664|1\\D?649|1\\D?473|1\\D?441|1\\D?345|1\\D?340|1\\D?284|1\\D?268|1\\D?264|1\\D?246|1\\D?242|1)\\D?"
        )
        //Log.e(tag, "number::_>" +  number);//OutPut::7698989898
        return phoneNumberWithCountryCode.replace(compile.pattern().toRegex(), "")
    }

    private fun setAdapter() {
        println("users: ${MyUtils.listAllUsersNumbers.toString()}")
        binding!!.recyclerChatList.adapter =
            ContactsAdapter(requireActivity(), filterContacts)
        binding!!.recyclerChatList.setItemViewCacheSize(listContacts.size)
    }

    @SuppressLint("Range")
    fun getContacts() {
        listContacts.clear()
        val phones: Cursor? = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        var i = 0;
        while (phones!!.moveToNext()) {
            val name: String =
                phones!!.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber: String =
                phones!!.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            var contact = ContactModel()
            contact.name = name
            contact.mobileNumber = phoneNumber.replace(" ", "").replace("+", "")
            var list = listContacts.filter { contactModel ->
                contactModel.mobileNumber == contact.mobileNumber
            }
            if (list.size == 0) {
                listContacts.add(contact)
            }
        }
        listContacts.sortBy { contactModel ->
            contactModel.name
        }

        filterContacts = listContacts
        setAdapter()
        phones.close()
    }
}