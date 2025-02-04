package io.github.lzghzr.biliveinvisible;

import android.content.Context;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.lzghzr.biliveinvisible.hooks.cHook;
import io.github.lzghzr.biliveinvisible.hooks.gHook;
import io.github.lzghzr.biliveinvisible.hooks.mHook;

public class MainHook implements IXposedHookLoadPackage {
  public static final String appName = "哔哩哔哩直播隐身观看";
  private Context systcmContext;
  private int appVersionCode = 0;
  private mHook biliApp;

  // https://github.com/freedom-introvert/biliSendCommAntifraud/blob/010192b8f981b70ccc5d03e342bb407699ee98e0/biliSendCommAntifraud/app/src/main/java/icu/freedomIntrovert/biliSendCommAntifraud/xposed/XposedInit.java#L37
  private static Context systemContext() {
    Object am = null;
    Class<?> findClassIfExists = XposedHelpers.findClass("android.app.ActivityThread", null);
    if (findClassIfExists != null) {
      am = XposedHelpers.callStaticMethod(findClassIfExists, "currentActivityThread", Arrays.copyOf(new Object[0], 0));
    }
    return (Context) XposedHelpers.callMethod(am, "getSystemContext", Arrays.copyOf(new Object[0], 0));
  }

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    if (systcmContext == null) {
      systcmContext = systemContext();
    }

    if (lpparam.packageName.equals("tv.danmaku.bili")) {//国内版
      this.appVersionCode = systcmContext.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionCode;
      this.biliApp = new cHook(lpparam);
    } else if (lpparam.packageName.equals("com.bilibili.app.in")) {//国际版
      this.appVersionCode = systcmContext.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionCode;
      this.biliApp = new gHook(lpparam);
    }

    if (this.biliApp != null) {
      try {
        this.biliApp.hook();
      } catch (Throwable throwable) {
        XposedBridge.log(appName + " " + this.biliApp.product + " " + this.appVersionCode + " 加载异常 " + throwable);
      }
    }
  }
}
