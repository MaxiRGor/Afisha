package com.volovich.afisha.model;

import com.google.firebase.Timestamp;

public class Event {

    //this fields are the same as ones in firestore collection "events"
    //and their names should coincide
    private String title;
    private String description;
    private String imageURL;
    private Timestamp date;
    private long price;
    private String place;
    private String address;

    //all fields below aren't firestores, they changes locally

    //field documentId is not a value that belongs to firestore document, we fill it in Java code (in AfishaActivity)
    private String documentId;

    //this field also set ups only locally (in WishlistActivity) and needs for deleting wishlist in WishlistAdapter
    private String wishListDocumentId;

    //this field changes in Afisha Adapter. If mark = true, it means that user liked this event
    private boolean mark;

    //this field changes in Afisha Adapter. If mark = true, it fills with count of liked tickets
    private long count;

    //needs empty constructor for firebase
    public Event() {
    }

    public Event(String title, String description, String imageURL, Timestamp date, long price, String place, String address, String documentId, String wishListDocumentId, boolean mark, int count) {
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.date = date;
        this.price = price;
        this.place = place;
        this.address = address;
        this.documentId = documentId;
        this.wishListDocumentId = wishListDocumentId;
        this.mark = mark;
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public boolean isMark() {
        return mark;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public String getWishListDocumentId() {
        return wishListDocumentId;
    }

    public void setWishListDocumentId(String wishListDocumentId) {
        this.wishListDocumentId = wishListDocumentId;
    }


    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
