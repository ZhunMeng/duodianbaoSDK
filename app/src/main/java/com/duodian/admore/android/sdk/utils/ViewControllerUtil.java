package com.duodian.admore.android.sdk.utils;

import com.duodian.admore.android.sdk.adview.BaseAdView;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ViewControllerUtil {
    private Map<String, WeakReference<BaseAdView>> map;
    private boolean registered;
    private boolean registering;

    private static class ViewControllerUtilHolder {
        private static ViewControllerUtil INSTANCE = new ViewControllerUtil();

        private ViewControllerUtilHolder() {
        }
    }

    private ViewControllerUtil() {
        this.map = new ConcurrentHashMap();
    }

    public static ViewControllerUtil getInstance() {
        return ViewControllerUtilHolder.INSTANCE;
    }

    public void addView(BaseAdView baseAdView) {
        this.map.put(baseAdView.getUuid(), new WeakReference(baseAdView));
        LogUtil.e("ViewControllerUtil", this.map.size() + "addView" + baseAdView.getUuid());
    }

    public void removeView(BaseAdView baseAdView) {
        if (baseAdView != null && this.map.containsKey(baseAdView.getUuid())) {
            this.map.remove(baseAdView.getUuid());
            LogUtil.e("ViewControllerUtil", this.map.size() + "removeView" + baseAdView.getUuid());
        }
    }

    public boolean isRegistering() {
        return this.registering;
    }

    public boolean isRegistered() {
        return this.registered;
    }

    public void setRegistering(boolean registering) {
        this.registering = registering;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
        notifyViewsRegistered();
    }

    private void notifyViewsRegistered() {
        for (String key : this.map.keySet()) {
            WeakReference<BaseAdView> weakReference = (WeakReference) this.map.get(key);
            if (weakReference != null) {
                BaseAdView baseAdView = (BaseAdView) weakReference.get();
                if (baseAdView != null) {
                    baseAdView.onAppRegistered();
                }
            }
        }
    }
}
