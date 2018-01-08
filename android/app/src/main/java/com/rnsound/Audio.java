package com.rnsound;

import java.io.Serializable;

/**
 * Created by iDontCare on 16.12.2017.
 */

public class Audio implements Serializable {
    private String DataSource;
    private String title;
    private String subtitle;
    private String imageUrl;

    public Audio(String url, String title, String subtitle) {
        this.DataSource = url;
        this.title = title;
        this.subtitle = subtitle;
    }

    public Audio(String url, String title, String subtitle, String imageUrl) {
        this.DataSource = url;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
    }

    public String getDataSource() {
        return DataSource;
    }

    public void setDataSource (String url) {
        this.DataSource = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
