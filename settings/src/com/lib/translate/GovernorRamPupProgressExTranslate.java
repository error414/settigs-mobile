package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;


public class GovernorRamPupProgressExTranslate implements ProgresExViewTranslateInterface {

    @Override
    public String translateCurrent(int current) {
        return String.valueOf(current) + " μs";
    }

    @Override
    public String translateMin(int min) {
        return String.valueOf(min) + " μs";
    }

    @Override
    public String translateMax(int max) {
        return String.valueOf(max) + " μs";
    }

}
