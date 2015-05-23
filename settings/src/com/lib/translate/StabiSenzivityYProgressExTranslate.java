package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;


public class StabiSenzivityYProgressExTranslate implements ProgresExViewTranslateInterface
{

    @Override
    public String translateCurrent(int current)
    {
        return String.valueOf(current - 100);
    }

    @Override
    public String translateMin(int min)
    {
        return String.valueOf(min - 100);
    }

    @Override
    public String translateMax(int max)
    {
        return String.valueOf(max - 100);
    }

}
