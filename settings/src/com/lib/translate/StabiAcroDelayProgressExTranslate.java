package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;

import java.util.Locale;

public class StabiAcroDelayProgressExTranslate implements ProgresExViewTranslateInterface
{
    private Locale currentLocale;

    public StabiAcroDelayProgressExTranslate(Locale currentLocale) {
        this.currentLocale = currentLocale;
    }

    @Override
    public String translateCurrent(int current)
    {
        String ret = String.valueOf(((float)current / 10)) + " s";

        if(currentLocale.getLanguage().equals("cs")){
            ret = ret.replace(".", ",");
        }

        return ret;
    }

    @Override
    public String translateMin(int min)
    {
        String ret = String.valueOf(((float)min / 10)) + " s";

        if(currentLocale.getLanguage().equals("cs")){
            ret = ret.replace(".", ",");
        }

        return ret;
    }

    @Override
    public String translateMax(int max)
    {
        String ret = String.valueOf(((float)max / 10)) + " s";

        if(currentLocale.getLanguage().equals("cs")){
            ret = ret.replace(".", ",");
        }

        return ret;
    }

}