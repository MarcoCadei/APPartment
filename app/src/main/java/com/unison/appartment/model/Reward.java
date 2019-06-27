package com.unison.appartment.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.util.Objects;

/**
 * Classe che rappresenta un premio da reclamare
 */
public class Reward implements Parcelable {

    private final static String ATTRIBUTE_RESERVATION_NAME = "reservation-name";
    private final static String ATTRIBUTE_RESERVATION_ID = "reservation-id";

    @Exclude
    private String id;
    private String name;
    private String description;
    private int points;
    @Nullable
    @PropertyName(ATTRIBUTE_RESERVATION_ID)
    private String reservationId;
    @Nullable
    @PropertyName(ATTRIBUTE_RESERVATION_NAME)
    private String reservationName;

    public Reward() {}

    public Reward(String id, String name, String description, int points) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.points = points;
    }

    public Reward(String name, String description, int points) {
        this.name = name;
        this.description = description;
        this.points = points;
    }

    public Reward(String name, String description, int points, @Nullable String reservationId) {
        this(name, description, points);
        this.reservationId = reservationId;
    }

    @Nullable
    @PropertyName(ATTRIBUTE_RESERVATION_ID)
    public String getReservationId() {
        return reservationId;
    }

    @PropertyName(ATTRIBUTE_RESERVATION_ID)
    public void setReservationId(@Nullable String reservationId) {
        this.reservationId = reservationId;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Nullable
    @PropertyName(ATTRIBUTE_RESERVATION_NAME)
    public String getReservationName() {
        return reservationName;
    }

    @PropertyName(ATTRIBUTE_RESERVATION_NAME)
    public void setReservationName(@Nullable String reservationName) {
        this.reservationName = reservationName;
    }

    @Exclude
    public boolean isRequested() {
        return this.reservationId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reward reward = (Reward) o;
        return points == reward.points &&
                name.equals(reward.name) &&
                description.equals(reward.description) &&
                Objects.equals(reservationId, reward.reservationId) &&
                Objects.equals(reservationName, reward.reservationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, points, reservationId, reservationName);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeInt(this.points);
        dest.writeString(this.reservationId);
        dest.writeString(this.reservationName);
    }

    protected Reward(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.points = in.readInt();
        this.reservationId = in.readString();
        this.reservationName = in.readString();
    }

    public static final Parcelable.Creator<Reward> CREATOR = new Parcelable.Creator<Reward>() {
        @Override
        public Reward createFromParcel(Parcel source) {
            return new Reward(source);
        }

        @Override
        public Reward[] newArray(int size) {
            return new Reward[size];
        }
    };
}
