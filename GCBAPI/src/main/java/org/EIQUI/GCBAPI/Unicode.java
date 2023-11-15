package org.EIQUI.GCBAPI;

import org.apache.commons.lang3.StringEscapeUtils;

public class Unicode {

    public static String getCharFromUnicode(String code){
        return StringEscapeUtils.unescapeJava(code);
    }
}
