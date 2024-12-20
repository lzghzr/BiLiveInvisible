package io.github.lzghzr.biliveinvisible.hooks;

import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class mHook {
  protected XC_LoadPackage.LoadPackageParam lpparam;

  public mHook(XC_LoadPackage.LoadPackageParam lpparam) {
    this.lpparam = lpparam;
  }

  public void hook() throws Throwable {
    addCommonParam();
    roomid();
    userApi();
    watchheartbeat();
  }

  // 获取房间弹幕服务器
  protected void addCommonParam() throws Throwable {
    XposedHelpers.findAndHookMethod(
        "com.bilibili.bililive.infra.network.interceptor.a",
        lpparam.classLoader,
        "addCommonParam",
        Map.class,
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            if (((Map<String, ?>) param.args[0]).containsKey("is_anchor")) {
              ((Map<String, ?>) param.args[0]).remove("access_key");
            }
          }
        });
  }

  // 连接弹幕服务器
  protected void roomid() throws Throwable {
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
            }
          }
        });
  }

  // 获取用户信息
  protected void userApi() throws Throwable {
    XposedHelpers.findAndHookMethod(
        "com.bilibili.bililive.api.user.UserApi",
        lpparam.classLoader,
        "d",
        long.class,
        int.class,
        int.class,
        int.class,
        int.class,
        int.class,
        "kotlin.coroutines.c",
        new XC_MethodHook() {
          @Override
          protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            param.args[5] = 1;
          }
        });
  }

  // 开始房间心跳
  protected void watchheartbeat() throws Throwable {
    XposedHelpers.findAndHookMethod(
        "com.bilibili.bililive.watchheartbeat.state.e",
        lpparam.classLoader,
        "run",
        new XC_MethodReplacement() {
          @Override
          protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            return true;
          }
        });
  }
}
