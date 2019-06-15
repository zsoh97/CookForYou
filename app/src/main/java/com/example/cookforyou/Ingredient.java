package com.example.cookforyou;

public class Ingredient implements Comparable<Ingredient>{
    private String mText;
    private boolean isSelected;

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public Ingredient(String mText){
        this.mText=mText;
        this.isSelected = false;
    }

    public boolean isSelected(){
        return isSelected;
    }

    public void setSelected(boolean selected){
        isSelected = selected;
    }

    @Override
    public int compareTo(Ingredient o) {
        return this.mText.compareTo(o.getmText());
    }
}
