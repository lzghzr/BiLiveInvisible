package io.github.lzghzr.biliveinvisible.hooks;

import static io.github.lzghzr.biliveinvisible.MainHook.appName;

import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class mHook {
  private static final boolean DEBUG = false;
  public String product;
  protected XC_LoadPackage.LoadPackageParam lpparam;

  public mHook(XC_LoadPackage.LoadPackageParam lpparam, String product) {
    this.lpparam = lpparam;
    this.product = product;
  }

  public void hook() throws Throwable {
    addCommonParam();
    fastjson();
    watchheartbeat();
  }

  // 添加请求参数
  protected void addCommonParam() throws Throwable {
    mHook that = this;
    XposedHelpers.findAndHookMethod(
        "com.bilibili.bililive.infra.network.interceptor.a",
        lpparam.classLoader,
        "addCommonParam",
        Map.class,
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            // 获取房间弹幕服务器
            if (((Map<String, ?>) param.args[0]).containsKey("is_anchor")) {
              ((Map<String, ?>) param.args[0]).remove("access_key");
              that.debugLog("addCommonParam access_key removed");
            }
            // 获取用户信息
            if (((Map<String, ?>) param.args[0]).containsKey("not_mock_enter_effect")) {
              ((Map<String, String>) param.args[0]).replace("not_mock_enter_effect", "0", "1");
              that.debugLog("addCommonParam not_mock_enter_effect replaced");
            }
          }
        });
  }

  // 连接弹幕服务器
  protected void fastjson() throws Throwable {
    mHook that = this;
    XposedHelpers.findAndHookMethod(
        "com.alibaba.fastjson.JSONObject",
        lpparam.classLoader,
        "put",
        String.class,
        Object.class,
        new XC_MethodHook() {
          @Override
          protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (param.args[0].equals("uid") && ((Map<String, ?>) param.thisObject).containsKey("group")) {
              param.args[1] = 0;
              that.debugLog("fastjson uid replaced");
            }
          }
        });
  }

  // 开始房间心跳
  protected void watchheartbeat() throws Throwable {
    mHook that = this;
    XposedHelpers.findAndHookMethod(
        "com.bilibili.bililive.watchheartbeat.state.e",
        lpparam.classLoader,
        "run",
        new XC_MethodReplacement() {
          @Override
          protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            that.debugLog("watchheartbeat run replaced");
            return true;
          }
        });
  }

  // debug日志
  public void debugLog(String log) {
    if (DEBUG) {
      XposedBridge.log(appName + " " + this.product + " " + log);
    }
  }
}
