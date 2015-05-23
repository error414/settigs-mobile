package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;


public class GovernorgearRatioProgressExTranslate implements ProgresExViewTranslateInterface
{

    @Override
    public String translateCurrent(int current)
    {
        return String.valueOf((double)current / 20);
    }

    @Override
    public String translateMin(int min)
    {
        return String.valueOf((double)min / 20);
    }

    @Override
    public String translateMax(int max)
    {
        return String.valueOf((double)max / 20);
    }

}
