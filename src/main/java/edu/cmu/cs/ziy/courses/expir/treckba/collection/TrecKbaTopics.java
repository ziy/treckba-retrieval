package edu.cmu.cs.ziy.courses.expir.treckba.collection;

import java.io.Reader;
import java.util.Arrays;
import java.util.Date;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class TrecKbaTopics {

  private KnowledgeBase kb;

  private String[] topicNames;

  private String topicSetId;

  public static TrecKbaTopics readTrecKbaTopics(Reader json) {
    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").create();
    return gson.fromJson(json, TrecKbaTopics.class);
  }

  public KnowledgeBase getKb() {
    return kb;
  }

  public String[] getTopicNames() {
    return topicNames;
  }

  public String getTopicSetId() {
    return topicSetId;
  }

  @Override
  public String toString() {
    return "TrecKbaTopics [kb=" + kb + ", topicNames=" + Arrays.toString(topicNames)
            + ", topicSetId=" + topicSetId + "]";
  }

}

class KnowledgeBase {

  @SerializedName("URL")
  private String url;

  private String description;

  private String name;

  private SnapshotTime snapshotTime;

  public String getUrl() {
    return url;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public SnapshotTime getSnapshotTime() {
    return snapshotTime;
  }

  @Override
  public String toString() {
    return "KnowledgeBase [URL=" + url + ", description=" + description + ", name=" + name
            + ", snapshotTime=" + snapshotTime + "]";
  }

}

class SnapshotTime {

  private int epochTicks;

  private Date zuluTimestamp;

  public int getEpochTicks() {
    return epochTicks;
  }

  public Date getZuluTimestamp() {
    return zuluTimestamp;
  }

  @Override
  public String toString() {
    return "SnapshotTime [epochTicks=" + epochTicks + ", zuluTimestamp=" + zuluTimestamp + "]";
  }

}