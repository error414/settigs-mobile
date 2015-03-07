package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;


public class GovernorThrRangeMinProgressExTranslate implements ProgresExViewTranslateInterface
{

    @Override
    public String translateCurrent(int current)
    {
        return String.valueOf(current * -1);
    }

    @Override
    public String translateMin(int min)
    {
        return String.valueOf(min * -1);
    }

    @Override
    public String translateMax(int max)
    {
        return String.valueOf(max * -1);
    }

}
