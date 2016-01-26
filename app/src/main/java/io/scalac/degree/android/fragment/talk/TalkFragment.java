package io.scalac.degree.android.fragment.talk;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.scalac.degree.android.activity.BaseActivity;
import io.scalac.degree.android.fragment.common.BaseFragment;
import io.scalac.degree.android.view.talk.TalkDetailsHeader;
import io.scalac.degree.android.view.talk.TalkDetailsSectionItem;
import io.scalac.degree.android.view.talk.TalkDetailsSectionItem_;
import io.scalac.degree.connection.model.SlotApiModel;
import io.scalac.degree33.R;

@EFragment(R.layout.fragment_talk_new)
public class TalkFragment extends BaseFragment implements AppBarLayout.OnOffsetChangedListener {

    private static final String DATE_TEXT_FORMAT = "MMMM dd, yyyy"; // April 20, 2014
    private static final String TIME_TEXT_FORMAT = "HH:MM"; // 9:30

    @ViewById(R.id.talkDetailsScheduleBtn)
    View scheduleButton;

    @ViewById(R.id.main_toolbar)
    Toolbar toolbar;

    @ViewById(R.id.main_appbar)
    AppBarLayout appBarLayout;

    @ViewById(R.id.main_collapsing)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @ViewById(R.id.toolbar_header_view)
    TalkDetailsHeader toolbarHeaderView;

    @ViewById(R.id.float_header_view)
    TalkDetailsHeader floatHeaderView;

    @ViewById(R.id.talkDetailsContainer)
    LinearLayout sectionContainer;

    @ViewById(R.id.talkDetailsDescription)
    TextView description;

    private boolean isHideToolbarView = false;

    @AfterViews
    void afterViews() {
        setupMainLayout();
    }

    @Click(R.id.talkDetailsScheduleBtn)
    void onScheduleButtonClick() {
        // TODO Schedule unschedule talk!
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        if (percentage == 1f && isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.VISIBLE);
            isHideToolbarView = !isHideToolbarView;

        } else if (percentage < 1f && !isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.GONE);
            isHideToolbarView = !isHideToolbarView;
        }
    }

    public void setupFragment(SlotApiModel slotModel) {
        toolbarHeaderView.setupHeader(slotModel.talk.title, slotModel.talk.track);
        floatHeaderView.setupHeader(slotModel.talk.title, slotModel.talk.track);
        description.setText(Html.fromHtml(slotModel.talk.summaryAsHtml));

        fillSectionsContainer(slotModel);
    }

    private void fillSectionsContainer(SlotApiModel slotModel) {
        sectionContainer.addView(createDateTimeSection(slotModel));
        sectionContainer.addView(createPresenterSection(slotModel));
        sectionContainer.addView(createRoomSection(slotModel));
        sectionContainer.addView(createFormatSection(slotModel));
    }

    private View createFormatSection(SlotApiModel slotModel) {
        return createSection("icon_TBD", R.string.talk_details_section_format, slotModel.talk.talkType);
    }

    private View createRoomSection(SlotApiModel slotModel) {
        return createSection("icon_TBD", R.string.talk_details_section_room, slotModel.roomName);
    }

    private View createPresenterSection(SlotApiModel slotModel) {
        return createSection("icon_TBD", R.string.talk_details_section_presentor,
                slotModel.talk.getReadableSpeakers());
    }

    private View createDateTimeSection(SlotApiModel slotModel) {
        final DateTime startDate = new DateTime(slotModel.fromTimeMillis);
        final DateTime endDate = new DateTime(slotModel.toTimeMillis);
        final String startDateString = startDate.toString(DateTimeFormat.forPattern(DATE_TEXT_FORMAT));
        final String startTimeString = startDate.toString(DateTimeFormat.forPattern(TIME_TEXT_FORMAT));
        final String endTimeString = endDate.toString(DateTimeFormat.forPattern(TIME_TEXT_FORMAT));
        return createSection("icon_TBD", R.string.talk_details_section_date_time,
                String.format("%s, %s to %s", startDateString, startTimeString, endTimeString));
    }

    private TalkDetailsSectionItem createSection(String iconUrl, @StringRes int title, String subtitle) {
        final TalkDetailsSectionItem result = TalkDetailsSectionItem_.build(getContext());
        result.setupView(iconUrl, title, subtitle);
        return result;
    }

    private void setupMainLayout() {
        collapsingToolbarLayout.setTitle(" ");
        final BaseActivity baseActivity = ((BaseActivity) getActivity());
        toolbar.setNavigationOnClickListener(v -> baseActivity.finish());
        baseActivity.setSupportActionBar(toolbar);
        baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        appBarLayout.addOnOffsetChangedListener(this);
    }
}