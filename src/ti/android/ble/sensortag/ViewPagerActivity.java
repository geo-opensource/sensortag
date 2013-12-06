/**************************************************************************************************
  Filename:       ViewPagerActivity.java
  Revised:        $Date: 2013-09-05 05:55:20 +0200 (to, 05 sep 2013) $
  Revision:       $Revision: 27614 $

  Copyright 2013 Texas Instruments Incorporated. All rights reserved.
 
  IMPORTANT: Your use of this Software is limited to those specific rights
  granted under the terms of a software license agreement between the user
  who downloaded the software, his/her employer (which must be your employer)
  and Texas Instruments Incorporated (the "License").  You may not use this
  Software unless you agree to abide by the terms of the License. 
  The License limits your use, and you acknowledge, that the Software may not be 
  modified, copied or distributed unless used solely and exclusively in conjunction 
  with a Texas Instruments Bluetooth device. Other than for the foregoing purpose, 
  you may not use, reproduce, copy, prepare derivative works of, modify, distribute, 
  perform, display or sell this Software and/or its documentation for any purpose.
 
  YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
  PROVIDED “AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
  INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
  NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
  TEXAS INSTRUMENTS OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT,
  NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER
  LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
  INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE
  OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT
  OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
  (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 
  Should you have any questions regarding your right to use this Software,
  contact Texas Instruments Incorporated at www.TI.com

 **************************************************************************************************/
package ti.android.ble.sensortag;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

public class ViewPagerActivity extends FragmentActivity {
  // Constants
  private static final String TAG = "ViewPagerActivity";

  // GUI
  protected static ViewPagerActivity mThis = null;
  protected SectionsPagerAdapter mSectionsPagerAdapter;
  private ViewPager mViewPager;
  protected int mResourceFragmentPager;
  protected int mResourceIdPager;

  private int mCurrentTab = 0;

  protected ViewPagerActivity() {
    Log.d(TAG, "construct");
    mThis = this;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    setContentView(mResourceFragmentPager);

    // Set up the action bar
    final ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    ImageView view = (ImageView) findViewById(android.R.id.home);
    view.setPadding(10, 0, 20, 10);

    // Set up the ViewPager with the sections adapter.
    mViewPager = (ViewPager) findViewById(mResourceIdPager);
    mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int n) {
        Log.d(TAG, "onPageSelected: " + n);
        actionBar.setSelectedNavigationItem(n);
      }
    });
    // Create the adapter that will return a fragment for each section
    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    // Set up the ViewPager with the sections adapter.
    mViewPager.setAdapter(mSectionsPagerAdapter);
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy");
    mSectionsPagerAdapter = null;
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    if (mCurrentTab != 0)
      getActionBar().setSelectedNavigationItem(0);
    else
      super.onBackPressed();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Log.d(TAG, "onOptionsItemSelected");
    // Handle presses on the action bar items
    switch (item.getItemId()) {
    // Respond to the action bar's Up/Home button
    case android.R.id.home:
      onBackPressed();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  protected void openAboutDialog() {
    final Dialog dialog = new AboutDialog(this);
    dialog.show();
  }

  public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> mFragmentList;
    private List<String> mTitles;

    public SectionsPagerAdapter(FragmentManager fm) {
      super(fm);
      mFragmentList = new ArrayList<Fragment>();
      mTitles = new ArrayList<String>();
    }

    public void addSection(Fragment fragment, String title) {
      final ActionBar actionBar = getActionBar();
      mFragmentList.add(fragment);
      mTitles.add(title);
      actionBar.addTab(actionBar.newTab().setText(title).setTabListener(tabListener));
      notifyDataSetChanged();
      Log.d(TAG, "Tab: " + title);
    }

    @Override
    public Fragment getItem(int position) {
      return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
      return mTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      if (position < getCount()) {
        return mTitles.get(position);
      } else {
        return null;
      }
    }
  }

  // Create a tab listener that is called when the user changes tabs.
  ActionBar.TabListener tabListener = new ActionBar.TabListener() {

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
      int n = tab.getPosition();
      Log.d(TAG, "onTabSelected: " + n);
      mCurrentTab = n;
      mViewPager.setCurrentItem(n);
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
      int n = tab.getPosition();
      Log.d(TAG, "onTabUnselected: " + n);
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
      int n = tab.getPosition();
      Log.d(TAG, "onTabReselected: " + n);
    }
  };
}
