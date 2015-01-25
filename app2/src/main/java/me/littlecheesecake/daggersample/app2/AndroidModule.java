package me.littlecheesecake.daggersample.app2;

import android.content.Context;
import android.location.LocationManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A module for Android-specific dependencies which require a {@link Context}
 * or {@link android.app.Application} to create.
 *  
 * Created by yulu on 25/1/15.
 */
@Module
public class AndroidModule {
    private final DemoApplication application;
    
    public AndroidModule(DemoApplication application) {
        this.application = application;
    }

    /**
     * Allow the application context to be injected but require that it be annotated with
     * {@link ForApplication @Annotation} to explicitly differentiate it from activity context 
     */
    @Provides @Singleton @ForApplication Context provideApplicationContext() {
        return application;
    }
    
    @Provides @Singleton LocationManager provideLocationManager() {
        return (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
    }
    
}
