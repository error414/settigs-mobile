package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;

/**
 * @author petrcada
 */
public class GovernorThrRangeProgressExTranslate implements ProgresExViewTranslateInterface {

    @Override
    public String translateCurrent(int current) {
        return String.valueOf(current * 10) + " μs";
    }

    @Override
    public String translateMin(int min) {
        return String.valueOf(min * 10) + " μs";
    }

    @Override
    public String translateMax(int max) {
        return String.valueOf(max * 10) + " μs";
    }

}
