package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;


public class GovernorRpmMaxProgressExTranslate implements ProgresExViewTranslateInterface
{

    @Override
    public String translateCurrent(int current)
    {
        return String.valueOf((current * 10) + 1500);
    }

    @Override
    public String translateMin(int min)
    {
        return String.valueOf((min * 10) + 1500);
    }

    @Override
    public String translateMax(int max)
    {
        return String.valueOf((max * 10)  + 1500);
    }

}
