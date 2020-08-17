package com.glofora.whatstustoolbox.models;

public class Card {

    private String text;
    private int l_image,image,color;

    public Card(String text, int image,int l_image, int color) {
        this.text = text;
        this.image = image;
        this.color = color;
        this.l_image = l_image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getL_image() {
        return l_image;
    }

    public void setL_image(int l_image) {
        this.l_image = l_image;
    }
}
