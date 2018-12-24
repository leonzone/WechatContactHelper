package com.reiser.wxcontacthelper;

/**
 * Created by reiserx on 2018/12/24.
 * desc :
 */
public class Setting {

    private int currentIndex = -1;
    private String currentPhone = "";
    private int length;

    private static volatile Setting instance = null;

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

    private boolean start;

    private String des;

    public String[] getPhones() {
        return phones;
    }

    public void setPhones(String[] phones) {
        this.phones = phones;
        if (phones != null && phones.length > 0) {
            length = phones.length;
            currentPhone = phones[phones.length - 1];
        }
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public String getPhone() {
        return currentPhone;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void next() {
        currentIndex += 1;
        if (currentIndex >= length) {
            start = false;
            currentPhone = "";
        } else {
            currentPhone = phones[currentIndex];
        }
    }
}
