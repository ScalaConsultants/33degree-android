package io.scalac.degree.android.adapter.schedule;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.scalac.degree.android.fragment.schedule.ScheduleDayLinupFragment_;
import io.scalac.degree.data.conference.model.ConferenceDay;

public class SchedulePagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAB_LAYOUT_TEXT_FORMAT = "EEE dd"; // WED 04
    private static final SimpleDateFormat TAB_LAYOUT_FORMAT = new SimpleDateFormat(
            TAB_LAYOUT_TEXT_FORMAT, Locale.getDefault());

    private static final Date TMP_DATE = new Date();

    private final List<ConferenceDay> conferenceDays;

    public SchedulePagerAdapter(FragmentManager fm, List<ConferenceDay> slots) {
        super(fm);
        conferenceDays = new ArrayList<>(slots);
    }

    @Override
    public Fragment getItem(int position) {
        return ScheduleDayLinupFragment_.builder()
                .lineupDayMs(conferenceDays.get(position).getDayMs())
                .build();
    }

    @Override
    public int getCount() {
        return conferenceDays.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TMP_DATE.setTime(conferenceDays.get(position).getDayMs());
        return TAB_LAYOUT_FORMAT.format(TMP_DATE);
    }
}
