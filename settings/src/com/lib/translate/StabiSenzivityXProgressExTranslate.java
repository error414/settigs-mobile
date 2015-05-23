package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;


public class StabiSenzivityXProgressExTranslate implements ProgresExViewTranslateInterface
{

    @Override
    public String translateCurrent(int current)
    {
        return String.valueOf(current + 20);
    }

    @Override
    public String translateMin(int min)
    {
        return String.valueOf(min + 20);
    }

    @Override
    public String translateMax(int max)
    {
        return String.valueOf(max + 20);
    }

}
