package com.resale.loveresalecommunication.logging;

import com.resale.loveresalecommunication.models.enums.ActionType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogActivity {
    ActionType value();
}


