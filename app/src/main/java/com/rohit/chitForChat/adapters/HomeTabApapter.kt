package com.rohit.chitForChat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.rohit.chitForChat.fragments.ChatsFragment
import com.rohit.chitForChat.fragments.ProfileFragment
import com.rohit.chitForChat.fragments.Settings

class HomeTabApapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 3;
    }

    override fun getItem(position: Int): Fragment {
        if (position == 0) return ChatsFragment();
        if (position == 1) return ProfileFragment();
        if (position == 2) return Settings();
        else return ChatsFragment()
    }

    override fun getPageTitle(position: Int): CharSequence? {

        if (position == 0) return "Chats";
        if (position == 1) return "Profile"
        if (position == 2) return "Settings"
        else return "Chats"
        return super.getPageTitle(position)

    }
}