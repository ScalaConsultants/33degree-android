package io.scalac.degree.connection.model;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.scalac.degree.data.model.SpeakerDbModel;
import io.scalac.degree.data.model.TalkDbModel;

/**
 * www.scalac.io
 * jacek.modrakowski@scalac.io
 * 26/10/2015
 */
public class SpeakerFullApiModel extends SpeakerBaseApiModel {
    public String bio;
    public String bioAsHtml;
    public String company;
    public String blog;
    public String twitter;
    public String lang;
    public List<LinkApiModel> links;
    public List<TalkShortApiModel> acceptedTalks;

    public static SpeakerFullApiModel fromDb(SpeakerDbModel dbModel) {
        final SpeakerFullApiModel result = new SpeakerFullApiModel();
        result.avatarURL = dbModel.getAvatarURL();
        result.firstName = dbModel.getFirstName();
        result.lastName = dbModel.getLastName();
        result.bio = dbModel.getBio();
        result.bioAsHtml = dbModel.getBioAsHtml();
        result.blog = dbModel.getBlog();
        result.company = dbModel.getCompany();
        result.lang = dbModel.getLang();
        result.links = new ArrayList<>();
        result.twitter = dbModel.getTwitter();

        final RealmList<TalkDbModel> talks = dbModel.getAcceptedTalks();
        result.acceptedTalks = new ArrayList<>(talks.size());

        for (TalkDbModel talkDbModel : talks) {
            result.acceptedTalks.add(TalkShortApiModel.fromDb(talkDbModel));
        }

        return result;
    }
}
