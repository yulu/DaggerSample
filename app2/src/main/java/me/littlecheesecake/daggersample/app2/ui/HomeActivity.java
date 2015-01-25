package me.littlecheesecake.daggersample.app2.ui;

import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import me.littlecheesecake.daggersample.app2.DemoActivity;
import me.littlecheesecake.daggersample.app2.DemoApplication;

/**
 * Created by yulu on 25/1/15.
 */
public class HomeActivity extends DemoActivity {
    @Inject
    LocationManager locationManager;
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DemoApplication)getApplication()).component().inject(this);
        
        Log.d("HomeActivity", locationManager.toString());
    }
}
