package org.masood.mafia.lang;

import java.util.Locale;
import java.util.ResourceBundle;

public class Translator {

    private Locale locale;

    private Locale defaultLocale = new Locale("en");

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

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String get(String key) {
        var strings = ResourceBundle.getBundle("i18n/strings", this.locale);
        return strings.containsKey(key) ? strings.getString(key) : ResourceBundle.getBundle("i18n/strings", this.defaultLocale).getString(key);
    }

    public String get(String key, String... subs) {
        var str = get(key);
        for (var i = 1; i <= subs.length; i++) {
            str = str.replaceAll("<" + i + ">", subs[i]);
        }
        return str;
    }
}
