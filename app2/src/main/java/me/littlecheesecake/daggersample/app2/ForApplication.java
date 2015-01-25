package me.littlecheesecake.daggersample.app2;

/**
 * Created by yulu on 25/1/15.
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

@Qualifier @Retention(RetentionPolicy.RUNTIME)
public @interface ForApplication {

}
