package com.sunnyday.library.Utils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by zb on 2020/6/12 13:27
 */
public class ElementUtil {
    /**
     * 获得包名
     *
     * @param elementUtil Elements instance
     * @param typeElement typeElement
     * @return pkgName
     */
    public static String getPkgName(Elements elementUtil, TypeElement typeElement) {
        return elementUtil.getPackageOf(typeElement).toString();
    }


    public static String getEnclosingClassName(TypeElement typeElement) {
        return typeElement.getSimpleName().toString();
    }
}
