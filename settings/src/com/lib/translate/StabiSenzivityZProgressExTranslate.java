package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;

import java.util.Locale;

/**
 * trida nam prelozi cislo z velikosti stabiPitch na procenta
 * <p/>
 * -10 az 10 prelozi na -100% az 100%, vnitrne se ale bude pocitat porad s -10 az 10
 *
 * @author petrcada
 */
public class StabiSenzivityZProgressExTranslate implements ProgresExViewTranslateInterface
{

    private Locale currentLocale;

    public StabiSenzivityZProgressExTranslate(Locale currentLocale) {
        this.currentLocale = currentLocale;
    }

    @Override
    public String translateCurrent(int current)
    {
        String ret = String.valueOf(((float)(current + 50) / 100) ) + "x";

        if(currentLocale.getLanguage().equals("cs")){
            ret = ret.replace(".", ",");
        }

        return ret;
    }

    @Override
    public String translateMin(int min)
    {
        String ret = String.valueOf(((float)(min + 50) / 100)) + "x";

        if(currentLocale.getLanguage().equals("cs")){
            ret = ret.replace(".", ",");
        }

        return ret;
    }

    @Override
    public String translateMax(int max)
    {
        String ret = String.valueOf(((float)(max + 50) / 100)) + "x";

        if(currentLocale.getLanguage().equals("cs")){
            ret = ret.replace(".", ",");
        }

        return ret;
    }

}
