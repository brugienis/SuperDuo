/*
 * Copyright (C) 2013 The Android Open Source Project
 */

package barqsoft.footballscores.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.activities.MainActivity;
import barqsoft.footballscores.service.MyFetchService;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {

    /* ViewPager - see : http://developer.android.com/training/implementing-navigation/lateral.html */
    private ViewPager mPagerHandler;
    private MyPageAdapter mPagerAdapter;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat mDayFormat = new SimpleDateFormat("EEEE");
    private MainScreenFragment[] mViewFragments = new MainScreenFragment[MainActivity.getNumPages()];
    private Date mFragmentdate;
    private static final long TWENTY_FOUR_HOURS_IN_MILLIS = 86400000L;

    private final static String LOG_TAG = PagerFragment.class.getSimpleName();

    /**
     * Create one MainScreenFragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        updateScores();

        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);

        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new MyPageAdapter(getChildFragmentManager());

            for (int i = 0; i < MainActivity.getNumPages(); i++) {
                mFragmentdate = new Date(System.currentTimeMillis() + ((i - MainActivity.getTodaysPage()) * TWENTY_FOUR_HOURS_IN_MILLIS));
                mViewFragments[i] = new MainScreenFragment();
                mViewFragments[i].setFragmentDate(mFormat.format(mFragmentdate));
        }

        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.getCurrentFragment());
        return rootView;
    }

    /**
     * start service that will get the latest scores data from the Internet.
     */
    private void updateScores() {
        Intent intent = new Intent(getActivity(), MyFetchService.class);
        getActivity().startService(intent);
    }

    public ViewPager getPagerHandler() {
        return mPagerHandler;
    }

    private class MyPageAdapter extends FragmentStatePagerAdapter {

        @Override
        public Fragment getItem(int i) {
            return mViewFragments[i];
        }

        @Override
        public int getCount() {
            return MainActivity.getNumPages();
        }

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
//            return getDayName(getActivity(), System.currentTimeMillis() + ((position - 2) * 86400000));
            return getDayName(getActivity(), System.currentTimeMillis() + ((position - MainActivity.getTodaysPage()) * 86400000));
        }

        /**
         * Returns the localized version of "Today, Yesterday and Tomorrow" instead of the actual
         * day name.
         */
        public String getDayName(Context context, long dateInMillis) {
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.

            Time t = new Time();
            t.setToNow();
            int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
            int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
            if (julianDay == currentJulianDay) {
                return context.getString(R.string.today);
            } else if (julianDay == currentJulianDay + 1) {
                return context.getString(R.string.tomorrow);
            } else if (julianDay == currentJulianDay - 1) {
                return context.getString(R.string.yesterday);
            } else {
                Time time = new Time();
                time.setToNow();
                // Otherwise, the format is just the day of the week (e.g "Wednesday".
                return mDayFormat.format(dateInMillis);
            }
        }
    }
}
