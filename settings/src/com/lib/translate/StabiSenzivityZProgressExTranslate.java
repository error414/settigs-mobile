package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;

/**
 * trida nam prelozi cislo z velikosti stabiPitch na procenta
 * <p/>
 * -10 az 10 prelozi na -100% az 100%, vnitrne se ale bude pocitat porad s -10 az 10
 *
 * @author petrcada
 */
public class StabiSenzivityZProgressExTranslate implements ProgresExViewTranslateInterface
{

    @Override
    public String translateCurrent(int current)
    {
        return String.valueOf(((float)(current + 50) / 100) ) + " X";
    }

    @Override
    public String translateMin(int min)
    {
        return String.valueOf(((float)(min + 50) / 100)) + " X";
    }

    @Override
    public String translateMax(int max)
    {
        return String.valueOf(((float)(max + 50) / 100)) + " X";
    }

}
