package com.devoxx.android.view.listholder.track;

import com.devoxx.android.view.list.schedule.TalkItemView;
import com.devoxx.connection.model.SlotApiModel;

public class TalkTrackHolder extends BaseTrackHolder {

    private TalkItemView talkItemView;

    public TalkTrackHolder(TalkItemView itemView) {
        super(itemView);
        talkItemView = itemView;
    }

    @Override
    public void setupView(SlotApiModel slotModel, boolean isRunning) {
        talkItemView.setupTalk(slotModel, isRunning)
                .withoutTrackName()
                .withTime();
    }
}
