package com.tarannelavelli.Pokedex;

public class Pokemon {
    private String name;
    private String url;
    private String descriptionURL;

    public Pokemon(String name, String url, String descriptionURL) {
        this.name = name;
        this.url = url;
        this.descriptionURL = descriptionURL;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getDescriptionURL() {
        return descriptionURL;
    }
}
