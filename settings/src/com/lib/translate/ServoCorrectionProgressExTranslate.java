package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;

/**
 * trida nam prelozi cislo z velikosti stabiPitch na procenta
 * <p/>
 * z 63 -191 na -64 + 64
 *
 * @author petrcada
 */
public class ServoCorrectionProgressExTranslate implements ProgresExViewTranslateInterface
{

    @Override
    public String translateCurrent(int current)
    {
        return String.valueOf((current - 127) * -1);
    }

    @Override
    public String translateMin(int min)
    {
        return String.valueOf((min - 127) * -1);
    }

    @Override
    public String translateMax(int max)
    {
        return String.valueOf((max - 127) * -1);
    }

}