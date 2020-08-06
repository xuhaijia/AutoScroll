package com.xhj.autoscroll;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends AccessibilityService {

    public static MyService mService;
    private boolean isStart;
    private boolean isRunning;
    private int width;
    private int height;
    public Timer timer;
    private List<String> list = new ArrayList<>();


    public void start() {
        isStart = true;
    }

    public static MyService getInstance() {
        return mService;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mService = this;
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
        list.add("com.kuaishou.nebula");
        list.add("com.ss.android.ugc.aweme.lite");
        list.add("com.jm.video");
        timer = new Timer();
        Log.d("MyService", "服务已启动");
        Log.d("MyService", "屏幕分辨率宽:" + width + ",高:" + height);
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (!isStart) {
            return;
        }
        if (event == null && event.getPackageName() == null) {
            return;
        }
        if (list.contains(event.getPackageName())) {
            final AccessibilityNodeInfo nodeInfo = event.getSource();
            if (nodeInfo == null) {
                return;
            }
            Log.d("MyService", "nodeInfo.getClassName():" + nodeInfo.getClassName());
            if (nodeInfo.getClassName().toString().contains("ViewPager") || nodeInfo.getClassName().toString().contains("RecyclerView")) {
                if (!isRunning) {
                    isRunning = true;
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            int bottomY = height * 9 / 10;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Path mPath = new Path();//线性的path代表手势路径,点代表按下,封闭的没用
                                mPath.moveTo(width / 2, bottomY);//代表从哪个点开始滑动
                                mPath.lineTo(width / 2, 100);//滑动到哪个点
                                dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription
                                        (mPath, 10, 400)).build(), new AccessibilityService.GestureResultCallback() {
                                    @Override
                                    public void onCompleted(GestureDescription gestureDescription) {
                                        super.onCompleted(gestureDescription);
                                        Log.d("MyService", "滑动完成");
                                    }

                                    @Override
                                    public void onCancelled(GestureDescription gestureDescription) {
                                        super.onCancelled(gestureDescription);
                                    }
                                }, null);
                            } else {
                                String command = "input swipe " + width / 2 + " " + bottomY + " " + width / 2 + " " + 100;
                                ShellUtils.execCommand(command, true);
                                Log.d("MyService", "滑动完成");
                            }

                        }
                    };
                    timer.schedule(task, 15000, 15000);
                    Toast.makeText(MyService.this, "开始执行!!!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    public void onInterrupt() {
        mService = null;

        Log.d("MyService", "服务已停止");
    }

    public void stop() {
        Toast.makeText(MyService.this, "停止执行!!!", Toast.LENGTH_LONG).show();

        isRunning = false;
        isStart = false;
        if (timer != null) {
            timer.cancel();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mService.disableSelf();
        }
    }


}
