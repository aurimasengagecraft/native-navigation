package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;

import com.facebook.react.devsupport.DoubleTapReloadRecognizer;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

public abstract class ReactAwareActivity extends AppCompatActivity
        implements ReactAwareActivityFacade, DefaultHardwareBackBtnHandler, PermissionAwareActivity {

    private DoubleTapReloadRecognizer mDoubleTapReloadRecognizer = new DoubleTapReloadRecognizer();

    @Nullable private PermissionListener mPermissionListener;

    @Override
    protected void onPause() {
        ReactNavigationCoordinator.sharedInstance.getReactInstanceManager().onHostPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReactNavigationCoordinator.sharedInstance.getReactInstanceManager().onHostResume(this, this);
    }

    @Override
    protected void onDestroy() {
        ReactNavigationCoordinator.sharedInstance.getReactInstanceManager().onHostDestroy(this);
        super.onDestroy()
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        onBackPressed();
    }

    /**
     * Schedules the shared element transition to be started immediately after the shared element has been measured and laid out within the activity's
     * view hierarchy. Some common places where it might make sense to call this method are:
     * <p>
     * (1) Inside a Fragment's onCreateView() method (if the shared element lives inside a Fragment hosted by the called Activity).
     * <p>
     * (2) Inside a Glide Callback object (if you need to wait for Glide to asynchronously load/scale a bitmap before the transition can begin).
     */
    public void scheduleStartPostponedTransition() {
        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                });
    }

    boolean supportIsDestroyed() {
        return AndroidVersion.isAtLeastJellyBeanMR1() && isDestroyed();
    }

    boolean isSuccessfullyInitialized() {
        return ReactNavigationCoordinator.sharedInstance.isSuccessfullyInitialized();
    }

    NavigationImplementation getImplementation() {
        return ReactNavigationCoordinator.sharedInstance.getImplementation();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (/* BuildConfig.DEBUG && */keyCode == KeyEvent.KEYCODE_MENU) {
            // TODO(lmr): disable this in prod
            ReactNavigationCoordinator.sharedInstance.getReactInstanceManager().getDevSupportManager().showDevOptionsDialog();
            return true;
        }
        if (keyCode == 0) { // this is the "backtick"
            // TODO(lmr): disable this in prod
            ReactNavigationCoordinator.sharedInstance.getReactInstanceManager().getDevSupportManager().showDevOptionsDialog();
            return true;
        }
        if (mDoubleTapReloadRecognizer.didDoubleTapR(keyCode, getCurrentFocus())) {
            ReactNavigationCoordinator.sharedInstance.getReactInstanceManager().getDevSupportManager().handleReloadJS();
        }

        return super.onKeyUp(keyCode, event);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        mPermissionListener = listener;
        requestPermissions(permissions, requestCode);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mPermissionListener != null && mPermissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            mPermissionListener = null;
        }
    }
}
