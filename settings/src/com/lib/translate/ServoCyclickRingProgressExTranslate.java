package com.lib.translate;

import com.customWidget.picker.ProgresExViewTranslateInterface;

/**
 * trida nam prelozi cislo z velikosti stabiPitch na procenta
 * <p/>
 * -10 az 10 prelozi na -100% az 100%, vnitrne se ale bude pocitat porad s -10 az 10
 *
 * @author petrcada
 */
public class ServoCyclickRingProgressExTranslate implements ProgresExViewTranslateInterface {

    /**
     *
     */
    private int geometry = 0;

    public ServoCyclickRingProgressExTranslate(int geometry) {
        this.geometry = geometry;
    }

    @Override
    public String translateCurrent(int current) {
        float angle = (6 * current * 1.418439716f) / geometry;

        return String.valueOf(current) + " (~" + String.valueOf(Math.floor(angle)) + "°)";
    }

    @Override
    public String translateMin(int min) {
        return String.valueOf(min);
    }

    @Override
    public String translateMax(int max) {
        return String.valueOf(max);
    }

}
