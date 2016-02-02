package com.devoxx.android.fragment.talk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devoxx.R;
import com.devoxx.android.activity.TalkDetailsHostActivity;
import com.devoxx.android.fragment.common.BaseFragment;
import com.devoxx.android.fragment.schedule.ScheduleLineupFragment;
import com.devoxx.android.view.talk.TalkDetailsHeader;
import com.devoxx.android.view.talk.TalkDetailsSectionClickableItem;
import com.devoxx.android.view.talk.TalkDetailsSectionClickableItem_;
import com.devoxx.android.view.talk.TalkDetailsSectionItem;
import com.devoxx.android.view.talk.TalkDetailsSectionItem_;
import com.devoxx.connection.model.SlotApiModel;
import com.devoxx.connection.model.TalkSpeakerApiModel;
import com.devoxx.connection.vote.VoteConnection;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.data.manager.NotificationsManager;
import com.devoxx.data.model.RealmConference;
import com.devoxx.data.vote.interfaces.IOnVoteForTalkListener;
import com.devoxx.data.vote.interfaces.ITalkVoter;
import com.devoxx.data.vote.voters.FakeVoter;
import com.devoxx.data.vote.voters.TalkVoter;
import com.devoxx.navigation.Navigator;
import com.devoxx.utils.DeviceUtil;
import com.devoxx.utils.InfoUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

@EFragment(R.layout.fragment_talk)
public class TalkFragment extends BaseFragment implements AppBarLayout.OnOffsetChangedListener {

    public static final String DATE_TEXT_FORMAT = "MMMM dd, yyyy"; // April 20, 2014
    public static final String TIME_TEXT_FORMAT = "HH:mm"; // 9:30
    private static final float FULL_FACTOR = 1f;

    @Bean
    DeviceUtil deviceUtil;

    @Bean
    NotificationsManager notificationsManager;

    @Bean
    Navigator navigator;

    @Bean
    ConferenceManager conferenceManager;

    @Bean
    InfoUtil infoUtil;

    @Bean(FakeVoter.class)
    ITalkVoter talkVoter;

    @SystemService
    LayoutInflater li;

    @FragmentArg
    SlotApiModel slotApiModel;

    @FragmentArg
    boolean notifyAboutChange;

    @ViewById(R.id.talkDetailsScheduleBtn)
    FloatingActionButton scheduleButton;

    @ViewById(R.id.fragment_talk_toolbar)
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

    private boolean shouldHideToolbarHeader = false;
    private SlotApiModel slotModel;

    @AfterViews
    void afterViews() {
        setHasOptionsMenu(!deviceUtil.isTablet());
        setupMainLayout();

        if (deviceUtil.isLandscapeTablet() && slotApiModel != null) {
            setupFragment(slotApiModel, notifyAboutChange);
        }
    }

    @Click(R.id.talkDetailsScheduleBtn)
    void onScheduleButtonClick() {
        if (notificationsManager.isNotificationScheduled(slotModel.slotId)) {
            notificationsManager.removeNotification(slotModel.slotId);
        } else {
            notificationsManager.scheduleNotification(slotModel, true);
        }

        if (deviceUtil.isLandscapeTablet()) {
            // Notify ScheduleLineupFragment about change.
            getActivity().sendBroadcast(ScheduleLineupFragment.getRefreshIntent());
        } else {
            notifyHostActivityAboutChangeOccured();
        }

        setupScheduleButton();
    }

    @Click(R.id.talkDetailsNotesBtn)
    void onNotesClick() {
        infoUtil.showToast("Notes TBD.");
    }

    @Click(R.id.talkDetailsTweetBtn)
    void onTweetClick() {
        final RealmConference conference = conferenceManager.getActiveConference();
        final String twitterMessage = String.format("%s\n%s %s", slotModel.talk.title,
                slotModel.talk.getReadableSpeakers(), conference.getHashtag());
        final String tweetUrl = "https://twitter.com/intent/tweet?text=" + Uri.encode(twitterMessage);
        final Uri uri = Uri.parse(tweetUrl);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    @Click(R.id.talkDetailsLikeBtn)
    void onLikeClick() {
        talkVoter.voteForTalk(slotModel.talk.id, new IOnVoteForTalkListener() {
            @Override
            public void onVoteForTalkSucceed() {
                infoUtil.showToast("View succeed, TBD.");
            }

            @Override
            public void onVoteForTalkFailed() {
                infoUtil.showToast("View failed, TBD.");
            }
        });
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float factor = (float) Math.abs(offset) / (float) maxScroll;

        if (factor == FULL_FACTOR && shouldHideToolbarHeader) {
            toolbarHeaderView.setVisibility(View.VISIBLE);
            shouldHideToolbarHeader = !shouldHideToolbarHeader;
        } else if (factor < FULL_FACTOR && !shouldHideToolbarHeader) {
            toolbarHeaderView.setVisibility(View.GONE);
            shouldHideToolbarHeader = !shouldHideToolbarHeader;
        }
    }

    public void setupFragment(SlotApiModel slot, boolean notifyParentAboutChange) {
        slotModel = slot;
        toolbarHeaderView.setupHeader(slot.talk.title, slot.talk.track);
        floatHeaderView.setupHeader(slot.talk.title, slot.talk.track);
        description.setText(Html.fromHtml(slot.talk.summaryAsHtml));
        setupScheduleButton();

        fillSectionsContainer(slot);

        if (notifyParentAboutChange) {
            notifyHostActivityAboutChangeOccured();
        }
    }

    private void notifyHostActivityAboutChangeOccured() {
        final Activity activity = getActivity();
        activity.setResult(TalkDetailsHostActivity.RESULT_CODE_SUCCESS);
    }

    private void setupScheduleButton() {
        if (notificationsManager.isNotificationScheduled(slotModel.slotId)) {
            scheduleButton.setImageResource(R.drawable.ic_star);
        } else {
            scheduleButton.setImageResource(R.drawable.ic_star_border);
        }
    }

    private void fillSectionsContainer(SlotApiModel slotModel) {
        sectionContainer.removeAllViews();
        sectionContainer.addView(createDateTimeSection(slotModel));
        sectionContainer.addView(createPresenterSection(slotModel));
        sectionContainer.addView(createRoomSection(slotModel));
        sectionContainer.addView(createFormatSection(slotModel));
    }

    private View createFormatSection(SlotApiModel slotModel) {
        return createSection(R.drawable.ic_star, R.string.talk_details_section_format, slotModel.talk.talkType);
    }

    private View createRoomSection(SlotApiModel slotModel) {
        return createSection(R.drawable.ic_place_big, R.string.talk_details_section_room, slotModel.roomName);
    }

    private View createPresenterSection(SlotApiModel slotModel) {
        final boolean manyGuys = slotModel.talk.speakers.size() > 1;
        return createClickableSection(R.drawable.ic_microphone_big,
                manyGuys ? R.string.talk_details_section_presentors : R.string.talk_details_section_presentor,
                slotModel.talk.speakers);
    }

    private View createClickableSection(
            @DrawableRes int icon, @StringRes int title, List<TalkSpeakerApiModel> readableSpeakers) {
        final TalkDetailsSectionClickableItem result = TalkDetailsSectionClickableItem_.build(getContext());
        result.setupView(icon, title);
        final ViewGroup container = result.getSpeakersContainer();
        for (TalkSpeakerApiModel speaker : readableSpeakers) {
            final TextView speakerView = (TextView) li.inflate(
                    R.layout.talk_details_section_speaker_item, container, false);
            SpannableString content = new SpannableString(speaker.name);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            speakerView.setText(content);
            final String speakeruuid = TalkSpeakerApiModel.getUuidFromLink(speaker.link);
            speakerView.setOnClickListener(v ->
                    navigator.openSpeakerDetails(getActivity(), speakeruuid));
            result.addSpeakerView(speakerView);
        }
        return result;
    }

    private View createDateTimeSection(SlotApiModel slotModel) {
        final DateTime startDate = new DateTime(slotModel.fromTimeMillis);
        final DateTime endDate = new DateTime(slotModel.toTimeMillis);
        final String startDateString = startDate.toString(DateTimeFormat.forPattern(DATE_TEXT_FORMAT));
        final String startTimeString = startDate.toString(DateTimeFormat.forPattern(TIME_TEXT_FORMAT));
        final String endTimeString = endDate.toString(DateTimeFormat.forPattern(TIME_TEXT_FORMAT));
        return createSection(R.drawable.ic_access_time_white_48dp, R.string.talk_details_section_date_time,
                String.format("%s, %s to %s", startDateString, startTimeString, endTimeString));
    }

    private TalkDetailsSectionItem createSection(@DrawableRes int icon, @StringRes int title, String subtitle) {
        final TalkDetailsSectionItem result = TalkDetailsSectionItem_.build(getContext());
        result.setupView(icon, title, subtitle);
        return result;
    }

    private void setupMainLayout() {
        collapsingToolbarLayout.setTitle(" ");
        final AppCompatActivity baseActivity = ((AppCompatActivity) getActivity());
        toolbar.setNavigationOnClickListener(v -> baseActivity.finish());

        if (!deviceUtil.isLandscapeTablet()) {
            baseActivity.setSupportActionBar(toolbar);
            baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        appBarLayout.addOnOffsetChangedListener(this);
    }
}