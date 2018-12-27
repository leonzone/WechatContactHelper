package com.reiser.wxcontacthelper;

/**
 * Created by reiserx on 2018/12/24.
 * desc :
 */
public class Setting {

    private int currentIndex = -1;
    private int length;

    private static volatile Setting instance = null;


    public boolean iWantGoHome = false;

    public boolean adding = false;

    public String currentPhone = "";

    private Setting() {
    }

    public static Setting newInstance() {
        if (null == instance) {
            synchronized (Setting.class) {
                if (instance == null) {
                    instance = new Setting();
                }
            }
        }
        return instance;
    }


    private String[] phones;


    private String des;

    public String[] getPhones() {
        return phones;
    }

    public void setPhones(String[] phones) {
        this.phones = phones;
        if (phones != null && phones.length > 0) {
            length = phones.length;
            currentPhone = phones[0];
        }
    }


    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String nextPhone() {
        currentIndex += 1;
        if (currentIndex >= length) {
            adding = false;
            currentPhone = "";
        } else {
            currentPhone = phones[currentIndex];
        }

        return currentPhone;
    }
}
