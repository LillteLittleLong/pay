package com.shangfu.pay.reconciliation.reconciliation.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by tinlly to 2019/3/14
 * Package for com.shangfu.pay.collpay.domain.util
 */
public class GsonUtils {

    public static Gson gson;

    static{
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        gson = gsonBuilder.create();
    }

    public static Gson getGson(){
        return gson;
    }

}
