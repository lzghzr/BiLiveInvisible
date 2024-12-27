package io.github.lzghzr.biliveinvisible.hooks;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class gHook extends mHook {

  public gHook(XC_LoadPackage.LoadPackageParam lpparam) {
    super(lpparam, "国际版");
  }
}