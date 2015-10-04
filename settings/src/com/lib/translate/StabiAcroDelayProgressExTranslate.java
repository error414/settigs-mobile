package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;

public class StabiAcroDelayProgressExTranslate implements ProgresExViewTranslateInterface
{

    @Override
    public String translateCurrent(int current)
    {
        return String.valueOf(((float)current / 10)) + " s";
    }

    @Override
    public String translateMin(int min)
    {
        return String.valueOf(((float)min / 10)) + " s";
    }

    @Override
    public String translateMax(int max)
    {
        return String.valueOf(((float)max / 10)) + " s";
    }

}