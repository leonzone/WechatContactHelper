package com.reiser.wxcontacthelper.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.reiser.wxcontacthelper.Setting;

import java.util.List;

/**
 * Created by reiserx on 2018/12/21.
 * desc :
 */
public class ContactService extends AccessibilityService {
    private static final String TAG = "AccessibilityService";

    /**
     * 启动页
     */
    private static final String ACTIVITY_LAUNCHER = "LauncherUI";
    /**
     * 添加朋友
     */
    private static final String ACTIVITY_FTSMAIN = "FTSMainUI";

    /**
     * 搜索朋友
     */
    private static final String ACTIVITY_FTSFRIEND = "FTSAddFriendUI";
    private static final String ACTIVITY_CONTACT_INFO = "ContactInfoUI";
    private static final String ACTIVITY_SAY_HI = "SayHiWithSnsPermissionUI";

    private String currentActivityName;


    private static int tabcount = -1;
    private static StringBuilder sb;
    private Setting mSetting;

    private AccessibilityNodeInfo rootNodeInfo;
    private String phone;

    @Override
    public void onCreate() {
        super.onCreate();
        mSetting = Setting.newInstance();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        try {
            if (mSetting == null || mSetting.getPhones() == null) {
                return;
            }
            watchChat(event);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void watchChat(AccessibilityEvent event) {
        rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) {
            return;
        }
        setCurrentActivityName(event);

        if (currentActivityName.contains(ACTIVITY_LAUNCHER)) {
            phone = mSetting.currentPhone;
            if (TextUtils.isEmpty(phone)) {
                return;
            }
            inMainPage();
            mSetting.adding = true;
            mSetting.iWantGoHome = false;
            return;
        }

        if (mSetting.iWantGoHome) {
            if (!currentActivityName.contains(ACTIVITY_LAUNCHER)) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                return;
            }
        }

        if (!mSetting.adding) {
            return;
        }

        if (currentActivityName.contains(ACTIVITY_FTSMAIN)) {
            if (inSearchPage()) return;
        }

        if (currentActivityName.contains(ACTIVITY_CONTACT_INFO)) {
            if (inInfoPage()) return;
        }

        if (currentActivityName.contains(ACTIVITY_SAY_HI)) {
            inAddPage();
        }


    }

    private void inAddPage() {
        AccessibilityNodeInfo node4 = findAllChilden(rootNodeInfo, "android.widget.EditText", null);
        if (node4 != null) {
            Bundle arg = new Bundle();
            arg.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, mSetting.getDes());
            node4.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg);
        }
        AccessibilityNodeInfo node5 = findAllChilden(rootNodeInfo, "android.widget.TextView", "发送");
        if (node5 != null) {
            onClick(node5);


            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            resetStatusJumpMain();
                        }
                    },
                    1000);
//                    performGlobalAction(GLOBAL_ACTION_BACK);
        }
    }

    private boolean inInfoPage() {
        AccessibilityNodeInfo node3 = findAllChilden(rootNodeInfo, "android.widget.Button", "添加到通讯录");
        if (node3 != null) {
            onClick(node3);
            return true;
        }

        AccessibilityNodeInfo node2 = findAllChilden(rootNodeInfo, "android.widget.Button", "发消息");
        if (node2 != null) {
            resetStatusJumpMain();
        }
        return false;
    }

    private boolean inSearchPage() {
        printAllChilden(rootNodeInfo);
        Log.d(TAG, "--------------------------------");
        final AccessibilityNodeInfo node2 = findAllChilden(rootNodeInfo, "android.widget.TextView", "查找手机/QQ号:" + phone);
        if (node2 != null) {
            //点击为什么不生效？？
            onClick(node2);
            return true;
        }
        AccessibilityNodeInfo node = findAllChilden(rootNodeInfo, "android.widget.EditText", "搜索");
        if (node != null) {
            Bundle arg = new Bundle();
            arg.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, phone);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg);
        }

        List<AccessibilityNodeInfo> node3 = rootNodeInfo.findAccessibilityNodeInfosByText("用户不存在");
        if (node3 != null && node3.size() > 0) {
            resetStatusJumpMain();
        }
        return false;
    }

    private void inMainPage() {
        printAllChilden(rootNodeInfo);
        AccessibilityNodeInfo node = findAllChilden(rootNodeInfo, "android.widget.TextView", "搜索");
        boolean have = hasOneOfThoseNodes("搜索");
        AccessibilityNodeInfo node2 = getTheLastNode("搜索");
        if (node != null) {
            onClick(node);
        }
    }

    private void resetStatusJumpMain() {
        performGlobalAction(GLOBAL_ACTION_BACK);
        mSetting.adding = false;
        phone = mSetting.nextPhone();
//        performGlobalAction(GLOBAL_ACTION_BACK);
    }


    private void openPacket(AccessibilityNodeInfo node) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        Point position = new Point(rect.left + 10, rect.top + 10);
        float dpi = metrics.densityDpi;
        Log.d(TAG, "openPacket！" + dpi);
        if (android.os.Build.VERSION.SDK_INT <= 23) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            if (android.os.Build.VERSION.SDK_INT > 23) {
                Path path = new Path();
//                if (640 == dpi) { //1440
//                    path.moveTo(80, 380);
//                } else if(320 == dpi){//720p
//                    path.moveTo(80, 380);
//                }else if(480 == dpi){//1080p
//                    path.moveTo(196, 1026);
//                }
                path.moveTo(position.x, position.y);
                GestureDescription.Builder builder = new GestureDescription.Builder();
                GestureDescription gestureDescription = builder.addStroke(new GestureDescription.StrokeDescription(path, 450, 50)).build();
                dispatchGesture(gestureDescription, new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        Log.d(TAG, "onCompleted");
                        super.onCompleted(gestureDescription);
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        Log.d(TAG, "onCancelled");
                        super.onCancelled(gestureDescription);
                    }
                }, null);

            }
        }
    }


    private void setCurrentActivityName(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        try {
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
            );

            getPackageManager().getActivityInfo(componentName, 0);
            currentActivityName = componentName.flattenToShortString();
            Log.d(TAG, "setCurrentActivityName: " + currentActivityName);
        } catch (PackageManager.NameNotFoundException e) {
//            currentActivityName = WECHAT_LUCKMONEY_GENERAL_ACTIVITY;
        }
    }

    private void onClick(AccessibilityNodeInfo node) {
        String type = node.getClassName().toString();

        switch (type) {
            case "android.widget.Button":
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            case "android.widget.TextView":
                node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            default:
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

    }


    private AccessibilityNodeInfo findAllChilden(AccessibilityNodeInfo nodeInfo, String type, String text) {
        if (nodeInfo == null) {
            return null;
        }
        if (TextUtils.isEmpty(text) && TextUtils.equals(nodeInfo.getClassName(), type)) {
            return nodeInfo;
        }
        if (TextUtils.equals(nodeInfo.getClassName(), type) && TextUtils.equals(nodeInfo.getText(), text)) {
            return nodeInfo;
        }

        if (TextUtils.equals(nodeInfo.getClassName(), type) && TextUtils.equals(nodeInfo.getContentDescription(), text)) {
            return nodeInfo;
        }
        AccessibilityNodeInfo node = null;
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            node = findAllChilden(nodeInfo.getChild(i), type, text);
            if (node != null) {
                break;
            }
        }

        return node;
    }


    //打印节点
    private void printAllChilden(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        if (!TextUtils.isEmpty(nodeInfo.getText())) {
            Log.d(TAG, "find nodeInfo: " + nodeInfo.getText() + " ---- " + nodeInfo.getClassName());
        } else {
            Log.d(TAG, "find nodeInfo: no text getContentDescription is" + nodeInfo.getContentDescription() + " >>>>>> " + nodeInfo.getClassName());
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            printAllChilden(nodeInfo.getChild(i));
        }
    }

    private boolean hasOneOfThoseNodes(String... texts) {
        List<AccessibilityNodeInfo> nodes;
        for (String text : texts) {
            if (text == null) continue;

            nodes = this.rootNodeInfo.findAccessibilityNodeInfosByText(text);

            if (nodes != null && !nodes.isEmpty()) return true;
        }
        return false;
    }

    private AccessibilityNodeInfo getTheLastNode(String... texts) {
        int bottom = 0;
        AccessibilityNodeInfo lastNode = null, tempNode;
        List<AccessibilityNodeInfo> nodes;

        for (String text : texts) {
            if (text == null) continue;

            nodes = this.rootNodeInfo.findAccessibilityNodeInfosByText(text);

            if (nodes != null && !nodes.isEmpty()) {
                tempNode = nodes.get(nodes.size() - 1);
                if (tempNode == null) return null;
                Rect bounds = new Rect();
                tempNode.getBoundsInScreen(bounds);
                if (bounds.bottom > bottom) {
                    bottom = bounds.bottom;
                    lastNode = tempNode;
                }
            }
        }
        return lastNode;
    }

    @Override
    public void onInterrupt() {

    }
}
