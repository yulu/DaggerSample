package me.littlecheesecake.daggersample.app2;

import android.app.Application;
import android.location.LocationManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import me.littlecheesecake.daggersample.app2.ui.HomeActivity;

/**
 * Created by yulu on 25/1/15.
 */
public class DemoApplication extends Application {
    
    @Singleton
    @Component(modules = AndroidModule.class)
    public interface ApplicationComponent {
        void inject(DemoApplication application);
        void inject(HomeActivity homeActivity);
        void inject(DemoActivity demoActivity);
    }
    
    @Inject ApplicationComponent component;
    
    @Override public void onCreate() {
        super.onCreate();
        component = Dagger_DemoApplication_ApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
        
        component().inject(this);
    }
    
    public ApplicationComponent component() {
        return component;
    }
}
