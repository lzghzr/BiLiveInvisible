package io.github.lzghzr.biliveinvisible;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.lzghzr.biliveinvisible.hooks.mHook;

public class MainHook implements IXposedHookLoadPackage {
  public static final String appName = "哔哩哔哩直播隐身观看";

  private Context mainContext;
  private String addCommonParamClass = "com.bilibili.bililive.infra.network.interceptor.a";
  private String watchHeartbeatClass = "com.bilibili.bililive.watchheartbeat.state.e";

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    if (!lpparam.packageName.equals("tv.danmaku.bili") && !lpparam.packageName.equals("com.bilibili.app.in"))
      return;

    if (mainContext == null) {
      XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
          mainContext = (Context) param.args[0];
          hookApp(lpparam);
        }
      });
    } else {
      hookApp(lpparam);
    }
  }

  private void hookApp(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    int appVersionCode = mainContext.getPackageManager().getPackageInfo(mainContext.getPackageName(), 0).versionCode;
    getClassName(lpparam, appVersionCode);
    mHook biliApp = new mHook(lpparam, addCommonParamClass, watchHeartbeatClass);

    try {
      biliApp.hook();
    } catch (Throwable throwable) {
      XposedBridge.log(appName + " " + lpparam.packageName + " " + appVersionCode + " 加载异常 " + throwable);
      Toast.makeText(mainContext, appName + ": " + appVersionCode + " 加载异常 ", Toast.LENGTH_LONG).show();
    }
  }

  private void getClassName(XC_LoadPackage.LoadPackageParam lpparam, int versionCode) {
    SharedPreferences sp = mainContext.getSharedPreferences("biliveinvisible", Context.MODE_PRIVATE);
    int spVersionCode = sp.getInt("versionCode", 0);
    if (spVersionCode == versionCode) {
      addCommonParamClass = sp.getString("addCommonParamClass", "");
      watchHeartbeatClass = sp.getString("watchHeartbeatClass", "");
    } else {
      System.loadLibrary("dexkit");
      try (DexKitBridge bridge = DexKitBridge.create(lpparam.appInfo.sourceDir)) {
        String addCommonParamClassName = findAddCommonParam(bridge);
        String watchHeartbeatClassName = findWatchHeartbeat(bridge);
        if (addCommonParamClassName != null && watchHeartbeatClassName != null) {
          addCommonParamClass = addCommonParamClassName;
          watchHeartbeatClass = watchHeartbeatClassName;
          sp.edit()
              .putInt("versionCode", versionCode)
              .putString("addCommonParamClass", addCommonParamClass)
              .putString("watchHeartbeatClass", watchHeartbeatClass)
              .apply();
        }
      }
    }
  }

  // 查找addCommonParam
  @Nullable
  private String findAddCommonParam(DexKitBridge bridge) {
    MethodData methodData = bridge.findMethod(FindMethod.create()
        .searchPackages("com.bilibili.bililive.infra.network.interceptor")
        .matcher(MethodMatcher.create()
            .name("addCommonParam"))
    ).singleOrNull();
    return methodData != null ? methodData.getClassName() : null;
  }

  // 查找watchHeartbeat
  @Nullable
  private String findWatchHeartbeat(DexKitBridge bridge) {
    ClassData classData = bridge.findClass(FindClass.create()
        .matcher(ClassMatcher.create()
            .usingStrings("_WatchTimeInitialState")
        )
    ).singleOrNull();
    if (classData == null)
      return null;

    String className;
    String fullClassName = classData.getName();
    int lastDotIndex = fullClassName.lastIndexOf('.');
    if (lastDotIndex == -1)
      return null;

    className = fullClassName.substring(0, lastDotIndex);
    MethodData methodData = bridge.findMethod(FindMethod.create()
            .searchPackages(className)
            .matcher(
                MethodMatcher.create()
                    .name("run")))
        .singleOrNull();
    return methodData != null ? methodData.getClassName() : null;
  }
}
