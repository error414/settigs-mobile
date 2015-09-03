/*
Copyright (C) Petr Cada and Tomas Jedrzejek
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package com.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;

public class HelpLinks {

    final public static String defaultLang = Locale.getDefault().getLanguage();

    final public static String baseUrl = "http://spirit-system.com/";

    final public static String docsUrl = "http://docs.google.com/viewer?url=";

    final public static HashMap<String, String> helpPdflinksHeli = new HashMap<String, String>() {
        {
            //linkHeli
            put("en", "dl/manual/spirit-manual-1.2.0_en.pdf");
            put("cz", "dl/manual/spirit-manual-1.2.0_cz.pdf");
            //endlinkHeli
        }
    };

    final public static HashMap<String, String> helpPdflinksAero = new HashMap<String, String>() {
        {
            //linkAero
            //endlinkAero
        }
    };

    final public static HashMap<String, String> languageTranslate = new HashMap<String, String>() {
        {
            put("cs", "cz");
        }
    };

    /**
     *
     * @param lang
     * @param mode
     * @return
     */
    public static String getPdfUrl(String lang, int mode){
        if(languageTranslate.containsKey(lang)){
            lang = languageTranslate.get(lang);
        }

        String defaultLangT = defaultLang;
        if(languageTranslate.containsKey(defaultLangT)){
            defaultLangT = languageTranslate.get(defaultLangT);
        }

        switch (mode)
        {
            default:
            case DstabiProfile.HELI:
                return getPdfUrlHeli(lang, defaultLangT);
            case DstabiProfile.AERO:
                return getPdfUrlAero(lang, defaultLangT);
        }
    }

    /**
     *
     * @param lang
     * @param defaultLangT
     * @return
     */
    public static String getPdfUrlHeli(String lang, String defaultLangT){
        if(helpPdflinksHeli.containsKey(lang)){
            return baseUrl + helpPdflinksHeli.get(lang);
        }

        if(helpPdflinksHeli.containsKey(defaultLang)){
            return baseUrl + helpPdflinksHeli.get(defaultLangT);
        }

        return baseUrl + helpPdflinksHeli.get("en");
    }

    /**
     *
     * @param lang
     * @param defaultLangT
     * @return
     */
    public static String getPdfUrlAero(String lang, String defaultLangT){


        if(helpPdflinksAero.containsKey(lang)){
            return baseUrl + helpPdflinksAero.get(lang);
        }

        if(helpPdflinksAero.containsKey(defaultLang)){
            return baseUrl + helpPdflinksAero.get(defaultLangT);
        }

        return baseUrl + helpPdflinksAero.get("en");
    }

    /**
     *
     * @param lang
     * @param mode
     * @return
     */
    public static String getDocsPdfUrl(String lang, int mode){
        try {
            return docsUrl +  URLEncoder.encode(getPdfUrl(lang, mode), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

}
