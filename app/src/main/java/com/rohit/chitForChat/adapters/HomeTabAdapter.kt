package com.rohit.chitForChat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.rohit.chitForChat.fragments.ChatsFragment
import com.rohit.chitForChat.fragments.ContactsFragment
import com.rohit.chitForChat.fragments.NearbyFragment
import com.rohit.chitForChat.fragments.Settings

class HomeTabAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 4;

    }

    override fun getItem(position: Int): Fragment {
        if (position == 0) return NearbyFragment();
        if (position == 1) return ChatsFragment()
        if (position == 2) return Settings();
        if (position == 3) return ContactsFragment();
        else return NearbyFragment()
    }

    override fun getPageTitle(position: Int): CharSequence? {

        if (position == 0) return "Nearby";
        if (position == 1) return "Chat"
        if (position == 2) return "Settings"
        if (position == 3) return "Contacts"
        else return "Nearby"
        return super.getPageTitle(position)

    }


}