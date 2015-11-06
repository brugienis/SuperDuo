package barqsoft.footballscores;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {

    // FIXME: 6/11/2015 - below change back to 5
    public static final int NUM_PAGES = 7;
    public static final int TODAYS_PAGE = NUM_PAGES / 2;    /* number of pages or tabs */
    /* about ViewPager - http://developer.android.com/training/implementing-navigation/lateral.html */
    public ViewPager mPagerHandler;
    private MyPageAdapter mPagerAdapter;
    private MainScreenFragment[] mViewFragments = new MainScreenFragment[NUM_PAGES];

    private final static String LOG_TAG = PagerFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);

        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new MyPageAdapter(getChildFragmentManager());

        for (int i = 0; i < NUM_PAGES; i++) {
//            Date fragmentdate = new Date(System.currentTimeMillis() + ((i-2) * 86400000));
            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - TODAYS_PAGE) * 86400000));
            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
            mViewFragments[i] = new MainScreenFragment();
            mViewFragments[i].setFragmentDate(mformat.format(fragmentdate));
            Log.v(LOG_TAG, "onCreateView - date: " + mformat.format(fragmentdate));
        }

        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.currentFragment);
        return rootView;
    }

    private class MyPageAdapter extends FragmentStatePagerAdapter {

        @Override
        public Fragment getItem(int i) {
            return mViewFragments[i];
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
//            return getDayName(getActivity(), System.currentTimeMillis() + ((position - 2) * 86400000));
            return getDayName(getActivity(), System.currentTimeMillis() + ((position - TODAYS_PAGE) * 86400000));
        }

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
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                return dayFormat.format(dateInMillis);
            }
        }
    }
}
