package com.resale.homeflycommunication.logging;

import com.resale.homeflycommunication.models.enums.ActionType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogActivity {
    ActionType value();
}


