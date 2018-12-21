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
    boolean mNeedAddPerson = false;


    private static int tabcount = -1;
    private static StringBuilder sb;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        setCurrentActivityName(event);
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        try {
            if (currentActivityName.contains(ACTIVITY_LAUNCHER)) {
                AccessibilityNodeInfo node = findAllChilden(nodeInfo, "android.widget.TextView", "搜索");
                if (node != null) {
                    onClick(node);
                    mNeedAddPerson = true;
                }
            }

            if (!mNeedAddPerson) {
                return;
            }


            if (currentActivityName.contains(ACTIVITY_FTSMAIN)) {
                findAllChilden(nodeInfo);
                Log.d(TAG, "--------------------------------");
                AccessibilityNodeInfo node2 = findAllChilden(nodeInfo, "android.widget.TextView", "查找手机/QQ号:18582312341");
                if (node2 != null) {
                    onClick(node2);
                    return;
                }
                AccessibilityNodeInfo node = findAllChilden(nodeInfo, "android.widget.EditText", "搜索");
                if (node != null) {
                    Bundle arg = new Bundle();
                    arg.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "18582312341");
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg);
                }


            }
            if (currentActivityName.contains(ACTIVITY_FTSFRIEND)) {


            }


//
            if (currentActivityName.contains(ACTIVITY_CONTACT_INFO)) {
                AccessibilityNodeInfo node3 = findAllChilden(nodeInfo, "android.widget.Button", "添加到通讯录");
                if (node3 != null) {
                    onClick(node3);
                }
            }


            if (currentActivityName.contains(ACTIVITY_SAY_HI)) {
                AccessibilityNodeInfo node4 = findAllChilden(nodeInfo, "android.widget.EditText", "我是许则则");
                if (node4 != null) {
                    Bundle arg = new Bundle();
                    arg.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "我是你的专属销售");
                    node4.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg);
                }
                AccessibilityNodeInfo node5 = findAllChilden(nodeInfo, "android.widget.TextView", "发送");
                if (node5 != null) {
                    onClick(node5);
                    mNeedAddPerson = false;
//                    performGlobalAction(GLOBAL_ACTION_BACK);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

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


    private void findAllChilden(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        if (!TextUtils.isEmpty(nodeInfo.getText())) {
            Log.d(TAG, "find nodeInfo: " + nodeInfo.getText() + " ---- " + nodeInfo.getClassName());
        } else {
            Log.d(TAG, "find nodeInfo: no text getContentDescription is" + nodeInfo.getContentDescription() + " >>>>>> " + nodeInfo.getClassName());
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findAllChilden(nodeInfo.getChild(i));
        }
    }

    private AccessibilityNodeInfo findInputerButton(AccessibilityNodeInfo node) {
        if (node == null)
            return null;

        //非layout元素
        if (node.getChildCount() == 0) {
            if ("android.widget.Button".equals(node.getClassName()))
                return node;
            else
                return null;
        }

        //layout元素，遍历找button
        AccessibilityNodeInfo button;
        for (int i = 0; i < node.getChildCount(); i++) {
            button = findInputerButton(node.getChild(i));
            if (button != null)
                return button;
        }
        return null;
    }

    private AccessibilityNodeInfo findOpenButton(AccessibilityNodeInfo node) {
        if (node == null)
            return null;

        //非layout元素
        if (node.getChildCount() == 0) {
            if ("android.widget.Button".equals(node.getClassName()))
                return node;
            else
                return null;
        }

        //layout元素，遍历找button
        AccessibilityNodeInfo button;
        for (int i = 0; i < node.getChildCount(); i++) {
            button = findOpenButton(node.getChild(i));
            if (button != null)
                return button;
        }
        return null;
    }

    @Override
    public void onInterrupt() {

    }
}
