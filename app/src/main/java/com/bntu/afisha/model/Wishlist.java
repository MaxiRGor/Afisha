package com.bntu.afisha.model;

public class Wishlist {
    public Wishlist() {
    }

    private String uid;
    private String eventId;
    private int count;


    private boolean isPurchased;

    //not firestore, but local field
    private String documentId;


    public Wishlist(String uid, String eventId, int count, boolean isPurchased, String documentId) {
        this.uid = uid;
        this.eventId = eventId;
        this.count = count;
        this.isPurchased = isPurchased;
        this.documentId = documentId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }

}
