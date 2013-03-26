/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v7.app;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.WindowCompat;
import android.support.v7.appcompat.R;
import android.support.v7.view.MenuInflater;
import android.support.v7.view.MenuItem;
import android.support.v7.internal.view.SupportMenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Base class for activities that use the support library action bar features.
 */
public class ActionBarActivity extends FragmentActivity implements ActionBar.Callback {
    ActionBarActivityDelegate mImpl;
    ActionBar mActionBar;
    MenuInflater mMenuInflater;

    // true if the compatibility implementation has installed a window sub-decor layout.
    boolean mSubDecorInstalled;

    // true if this activity has an action bar.
    boolean mHasActionBar;

    // true if this activity's action bar overlays other activity content.
    boolean mOverlayActionBar;

    /**
     * Support library version of {@link Activity#getActionBar}.
     *
     * <p>Retrieve a reference to this activity's ActionBar.
     *
     * @return The Activity's ActionBar, or null if it does not have one.
     */
    public ActionBar getSupportActionBar() {
        // The Action Bar should be lazily created as mHasActionBar or mOverlayActionBar
        // could change after onCreate
        if (mHasActionBar || mOverlayActionBar) {
            if (mActionBar == null) {
                mActionBar = mImpl.createSupportActionBar();
            }
        } else {
            // If we're not set to have a Action Bar, null it just in case it's been set
            mActionBar = null;
        }
        return mActionBar;
    }

    /**
     * Support library version of {@link Activity#getMenuInflater}.
     *
     * <p>Returns a {@link MenuInflater} with this context.
     *
     * @return The Activity's menu inflater.
     */
    public MenuInflater getSupportMenuInflater() {
        if (mMenuInflater == null) {
            mMenuInflater = new SupportMenuInflater(this);
        }
        return mMenuInflater;
    }

    @Override
    public void setContentView(int layoutResID) {
        mImpl.setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        mImpl.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        mImpl.setContentView(view, params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mImpl = new ActionBarActivityDelegateICS(this);
        } else if (version >= Build.VERSION_CODES.HONEYCOMB) {
            mImpl = new ActionBarActivityDelegateHC(this);
        } else {
            mImpl = new ActionBarActivityDelegateCompat(this);
        }

        TypedArray a = obtainStyledAttributes(R.styleable.ActionBarWindow);
        mHasActionBar = a.getBoolean(R.styleable.ActionBarWindow_windowActionBar, false);
        mOverlayActionBar = a.getBoolean(R.styleable.ActionBarWindow_windowActionBarOverlay, false);
        a.recycle();

        mImpl.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mImpl.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mImpl.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, android.view.Menu frameworkMenu) {
        return mImpl.onCreatePanelMenu(featureId, frameworkMenu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, android.view.Menu menu) {
        return mImpl.onPreparePanel(featureId, view, menu);
    }

    @Override
    public View onCreatePanelView(int featureId) {
        if (featureId == Window.FEATURE_OPTIONS_PANEL)
            return mImpl.onCreatePanelView(featureId);
        else {
            return super.onCreatePanelView(featureId);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, android.view.MenuItem item) {
        if (mImpl.onMenuItemSelected(featureId, item)) {
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        mImpl.setTitle(title);
    }

    /**
     * Enable extended support library window features.
     * <p>
     * This is a convenience for calling
     * {@link android.view.Window#requestFeature getWindow().requestFeature()}.
     * </p>
     *
     * @param featureId The desired feature as defined in
     * {@link android.view.Window} or {@link WindowCompat}.
     * @return Returns true if the requested feature is supported and now enabled.
     *
     * @see android.app.Activity#requestWindowFeature
     * @see android.view.Window#requestFeature
     */
    public boolean supportRequestWindowFeature(int featureId) {
        return mImpl.requestWindowFeature(featureId);
    }

    @Override
    public void supportInvalidateOptionsMenu() {
        // Only call up to super on HC+, mImpl will handle otherwise
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            super.supportInvalidateOptionsMenu();
        }
        mImpl.supportInvalidateOptionsMenu();
    }

    /**
     * Support library version of {@link Activity#onPrepareOptionsMenu}.
     *
     * <p>Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     *
     * <p>The default implementation updates the system menu items based on the
     * activity's state.  Deriving classes should always call through to the
     * base class implementation.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateSupportOptionsMenu().
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
     * @see #onCreateSupportOptionsMenu
     */
    public boolean onPrepareSupportOptionsMenu(android.support.v7.view.Menu menu) {
        return true;
    }

    /**
     * Support library version of {@link Activity#onCreateOptionsMenu}.
     *
     * <p>Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareSupportOptionsMenu}.
     *
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link android.support.v7.view.Menu#CATEGORY_SYSTEM}
     * group so that they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     *
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateSupportOptionsMenu() is called.
     *
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onSupportOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
     * @see #onPrepareSupportOptionsMenu
     * @see #onSupportOptionsItemSelected
     */
    public boolean onCreateSupportOptionsMenu(android.support.v7.view.Menu menu) {
        return true;
    }

    /**
     * Support library version of {@link Activity#onOptionsItemSelected}.
     *
     * <p>This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     *
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     *
     * @see #onPrepareSupportOptionsMenu
     * @see #onCreateSupportOptionsMenu
     */
    public boolean onSupportOptionsItemSelected(android.support.v7.view.MenuItem item) {
        return false;
    }

    /**
     * Support library version of {@link Activity#onMenuItemSelected}.
     *
     * <p>Default implementation of {@link android.view.Window.Callback#onMenuItemSelected}
     * for activities.  This calls through to the new {@link #onSupportOptionsItemSelected}
     * method for the {@link android.view.Window#FEATURE_OPTIONS_PANEL}
     * panel, so that subclasses of Activity don't need to deal with feature codes.
     */
    public boolean onSupportMenuItemSelected(int featureId, MenuItem item) {
        return false;
    }

    void superSetContentView(int resId) {
        super.setContentView(resId);
    }

    void superSetContentView(View v) {
        super.setContentView(v);
    }

    void superSetContentView(View v, ViewGroup.LayoutParams lp) {
        super.setContentView(v, lp);
    }

    void superAddContentView(View v, ViewGroup.LayoutParams lp) {
        super.addContentView(v, lp);
    }

    boolean superOnCreatePanelMenu(int featureId, android.view.Menu frameworkMenu) {
        return super.onCreatePanelMenu(featureId, frameworkMenu);
    }

    boolean superOnPreparePanelMenu(int featureId, View view, android.view.Menu menu) {
        return super.onPreparePanel(featureId, view, menu);
    }

    interface ActionBarActivityDelegate {
        ActionBar createSupportActionBar();
        void onCreate(Bundle savedInstanceState);
        void onPostCreate(Bundle savedInstanceState);
        void onConfigurationChanged(Configuration newConfig);
        void setContentView(View v);
        void setContentView(int resId);
        void setContentView(View v, ViewGroup.LayoutParams lp);
        void addContentView(View v, ViewGroup.LayoutParams lp);
        boolean requestWindowFeature(int featureId);
        void setTitle(CharSequence title);
        void supportInvalidateOptionsMenu();

        // Methods used to create and respond to options menu
        View onCreatePanelView(int featureId);
        boolean onCreatePanelMenu(int featureId, android.view.Menu frameworkMenu);
        boolean onMenuItemSelected(int featureId, android.view.MenuItem frameworkItem);
        boolean onPreparePanel(int featureId, View view, android.view.Menu frameworkMenu);
    }

}