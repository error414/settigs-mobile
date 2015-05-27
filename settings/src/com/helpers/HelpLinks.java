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

    final public static HashMap<String, String> helpPdflinks = new HashMap<String, String>() {
        {
            //link
            put("en", "dl/manual/spirit-manual-1.1.0_en.pdf");
            put("cz", "dl/manual/spirit-manual-1.1.0_cz.pdf");
            put("de", "dl/manual/spirit-manual-1.1.0_de.pdf");
            //endlink
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
     * @return
     */
    public static String getPdfUrl(String lang){
        if(languageTranslate.containsKey(lang)){
            lang = languageTranslate.get(lang);
        }

        String defaultLangT = defaultLang;
        if(languageTranslate.containsKey(defaultLangT)){
            defaultLangT = languageTranslate.get(defaultLangT);
        }

        if(helpPdflinks.containsKey(lang)){
            return baseUrl + helpPdflinks.get(lang);
        }

        if(helpPdflinks.containsKey(defaultLang)){
            return baseUrl + helpPdflinks.get(defaultLangT);
        }

        return baseUrl + helpPdflinks.get("en");
    }

    /**
     *
     * @param lang
     * @return
     */
    public static String getDocsPdfUrl(String lang){
        try {
            return docsUrl +  URLEncoder.encode(getPdfUrl(lang), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

}
