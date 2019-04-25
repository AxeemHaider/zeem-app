package org.octabyte.zeem.Utils;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import org.octabyte.zeem.Camera.CameraActivity;
import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.Home.PublicActivity;
import org.octabyte.zeem.Notification.NotificationActivity;
import org.octabyte.zeem.Publish.SelectStatusActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Search.NearByActivity;

/**
 * Created by Azeem on 8/29/2017.
 */

public class BottomNavigationSetup {
    public static void init(final BottomNavigationView navigation, final Activity activity, final int position, final String mode){
        try {
            navigation.getMenu().getItem(position).setChecked(true);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        //BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navHome:
                        if (position != 0) {
                            activity.startActivity(new Intent(activity, HomeActivity.class));
                            activity.overridePendingTransition(0,0);
                        }
                        break;
                    case R.id.navNearBy:
                        if (position != 1) {
                            final Intent intent = new Intent(activity, NearByActivity.class);
                            intent.putExtra("ARG_NAV_SENDER_MODE_TYPE", mode);
                            activity.startActivity(intent);
                            activity.overridePendingTransition(0, 0);
                        }
                        break;
                    case R.id.navNotification:
                        if (position != 2) {
                            final Intent intent2 = new Intent(activity, NotificationActivity.class);
                            int[] startingLocation2 = new int[2];
                            navigation.getLocationOnScreen(startingLocation2);
                            intent2.putExtra(NotificationActivity.ARG_DRAWING_START_LOCATION, startingLocation2[1]);
                            intent2.putExtra("ARG_NAV_SENDER_MODE_TYPE", mode);
                            activity.startActivity(intent2);
                            activity.overridePendingTransition(0, 0);
                        }
                        break;
                    case R.id.navStatus:
                        if (position != 3) {
                            final Intent intent1 = new Intent(activity, SelectStatusActivity.class);
                            int[] startingLocation1 = new int[2];
                            navigation.getLocationOnScreen(startingLocation1);
                            intent1.putExtra(SelectStatusActivity.ARG_DRAWING_START_LOCATION, startingLocation1[1]);
                            intent1.putExtra("ARG_NAV_SENDER_MODE_TYPE", mode);
                            activity.startActivity(intent1);
                            activity.overridePendingTransition(0, 0);
                        }
                        break;
                    case R.id.navStory:
                        if (position != 4) {
                            int[] startingLocation3 = new int[2];
                            navigation.getLocationOnScreen(startingLocation3);
                            startingLocation3[0] += navigation.getWidth() / 2;

                            if (activity instanceof HomeActivity || activity instanceof PublicActivity){
                                CameraActivity.startCameraFromLocation(startingLocation3, activity, false);
                                activity.overridePendingTransition(0, 0);
                            }else if (mode.equals("PRIVATE")) {
                                Intent intent = new Intent(activity, HomeActivity.class);
                                intent.putExtra("ARG_STORY_THROUGH_THIS", true);
                                intent.putExtra("ARG_STORY_LOCATION_THROUGH_THIS", startingLocation3);
                                activity.startActivity(intent);
                            }else if (mode.equals("PUBLIC")){
                                Intent intent = new Intent(activity, PublicActivity.class);
                                intent.putExtra("ARG_STORY_THROUGH_THIS", true);
                                intent.putExtra("ARG_STORY_LOCATION_THROUGH_THIS", startingLocation3);
                                activity.startActivity(intent);
                            }

                        }
                        break;

                }
                return true;
            }
        });
    }
}
