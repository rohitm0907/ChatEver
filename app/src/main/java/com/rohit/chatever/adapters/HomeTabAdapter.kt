package com.rohit.chatever.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.rohit.chatever.fragments.ChatsFragment
import com.rohit.chatever.fragments.ContactsFragment
import com.rohit.chatever.fragments.NearbyFragment
import com.rohit.chatever.fragments.Settings

class HomeTabAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 4;
    }

    override fun getItem(position: Int): Fragment {
        if (position == 0) return ChatsFragment()
        if (position == 1) return NearbyFragment();
        if (position == 2) return Settings();
        if (position == 3) return ContactsFragment();
        else return ChatsFragment()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        if (position == 0) return "Chat"
        if (position == 1) return "Nearby";
        if (position == 2) return "Settings"
        if (position == 3) return "Contacts"
        else return "Chat"
        return super.getPageTitle(position)
    }
}