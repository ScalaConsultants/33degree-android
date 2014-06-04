package io.scalac.degree.fragments;

import io.scalac.degree.MainActivity;
import io.scalac.degree.items.RoomItem;
import io.scalac.degree.items.TimeslotItem;
import io.scalac.degree33.R;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * A placeholder fragment containing a simple view.
 */
public class TabsFragment extends Fragment implements ActionBar.TabListener {
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory. If this becomes too
	 * memory intensive, it may be best to switch to a {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter				mSectionsPagerAdapter;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager							mViewPager;
	
	ArrayList<RoomItem>				roomItemsList;
	ArrayList<TimeslotItem>			timeslotItemsList;
	ArrayList<Date>					datesList;
	ArrayList<String>					datesNamesList;
	boolean								isCreated;
	TabType								tabType					= TabType.ROOM;
	
	int									currentDatePosition	= 0;
	
	private ArrayAdapter<String>	spinnerAbAdapter;
	
	private static final String	ARG_TAB_TYPE			= "tab_type";
	private static final String	ARG_DATE_POSITION		= "date_position";
	
	public enum TabType {
		ROOM, TIME
	}
	
	public static TabsFragment newInstance(TabType tabType) {
		return newInstance(tabType, 0);
	}
	
	public static TabsFragment newInstance(TabType tabType, int datePosition) {
		TabsFragment fragment = new TabsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TAB_TYPE, tabType.ordinal());
		args.putInt(ARG_DATE_POSITION, datePosition);
		fragment.setArguments(args);
		return fragment;
	}
	
	public TabsFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		if (getActivity() != null) {
			init();
			isCreated = true;
		} else
			isCreated = false;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (!isCreated) {
			init();
			isCreated = true;
		}
		
		// Set up the action bar.
		final ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayShowTitleEnabled(getResources().getBoolean(R.bool.show_title));
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowCustomEnabled(true);
		if (actionBar.getCustomView() == null)
			actionBar.setCustomView(R.layout.date_ab_spinner);
		
		Spinner spinnerAB = (Spinner) actionBar.getCustomView().findViewById(R.id.date_ab_spinner);
		spinnerAB.setAdapter(spinnerAbAdapter);
		
		spinnerAB.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (currentDatePosition != position) {
					currentDatePosition = position;
					getMainActivity().getSupportFragmentManager().popBackStack();
					getMainActivity().replaceFragment(TabsFragment.newInstance(tabType, position));
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		spinnerAB.setSelection(currentDatePosition);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) getActivity().findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});
		
		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
		}
	}
	
	private void init() {
		tabType = TabType.values()[getArguments().getInt(ARG_TAB_TYPE)];
		currentDatePosition = getArguments().getInt(ARG_DATE_POSITION);
		roomItemsList = getMainActivity().getRoomItemsList();
		datesList = TimeslotItem.getDatesList(getMainActivity().getTimeslotItemsList());
		timeslotItemsList = TimeslotItem.getTimeslotItemsList(getMainActivity().getTimeslotItemsList(),
				datesList.get(currentDatePosition));
		datesNamesList = new ArrayList<String>();
		DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getActivity().getApplicationContext());
		for (Date date : datesList) {
			datesNamesList.add(dateFormat.format(date));
		}
		
		spinnerAbAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, datesNamesList);
		spinnerAbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getMainActivity().setDrawerIndicatorEnabled(true);
		View rootView = inflater.inflate(R.layout.fragment_rooms, container, false);
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		final ActionBar actionBar = getActivity().getActionBar();
		actionBar.removeAllTabs();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setCustomView(null);
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		try {
			Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);
			
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a TalksFragment (defined as a static inner class below).
			switch (tabType) {
				case TIME:
					return TalksFragment.newInstanceTime(timeslotItemsList.get(position).getId());
				default:
					return TalksFragment.newInstanceRoom(roomItemsList.get(position).getId(),
							datesList.get(currentDatePosition).getTime());
			}
		}
		
		@Override
		public int getCount() {
			// Show total pages.
			switch (tabType) {
				case TIME:
					return timeslotItemsList.size();
				default:
					return roomItemsList.size();
			}
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (tabType) {
				case TIME:
					TimeslotItem timeslotItem = timeslotItemsList.get(position);
					DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity().getApplicationContext());
					String time = timeFormat.format(timeslotItem.getStartTime()).replaceAll(" AM", "").replaceAll(" PM", "") + " � "
							+ timeFormat.format(timeslotItem.getEndTime()).replaceAll(" ", "&nbsp;");
					return Html.fromHtml(time);
				default:
					return roomItemsList.get(position).getName().toUpperCase(l);
			}
		}
	}
	
	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}
	
	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {}
	
	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {}
	
	private MainActivity getMainActivity() {
		return (MainActivity) getActivity();
	}
}
