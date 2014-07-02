package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;

/**
 * trida nam prelozi cislo z velikosti stabiPitch na procenta
 * <p/>
 * -10 az 10 prelozi na -100% az 100%, vnitrne se ale bude pocitat porad s -10 az 10
 *
 * @author petrcada
 */
public class StabiPichProgressExTranslate implements ProgresExViewTranslateInterface
{

    @Override
    public String translateCurrent(int current)
    {
        return String.valueOf((current * 10)) + " %";
    }

    @Override
    public String translateMin(int min)
    {
        return String.valueOf((min * 10)) + " %";
    }

    @Override
    public String translateMax(int max)
    {
        return String.valueOf((max * 10)) + " %";
    }

}