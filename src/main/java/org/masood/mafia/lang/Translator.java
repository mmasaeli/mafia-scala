package org.masood.mafia.lang;

import java.util.Locale;
import java.util.ResourceBundle;

public class Translator {

    private Locale locale;

    public Translator(Locale locale) {
        this.locale = locale;
    }

    public Translator(String localeString) {
        var localeParts = localeString.split("_");
        if (localeParts.length == 2)
            setLocale(new Locale(localeParts[0], localeParts[1]));
        else
            setLocale(new Locale(localeString));
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }


    public String get(String key) {
        var strings = ResourceBundle.getBundle("i18n/strings", this.locale);
        return strings.getString(key);
    }
}
