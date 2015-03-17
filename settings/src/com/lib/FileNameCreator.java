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
package com.lib;


import java.io.File;

public class FileNameCreator {

    final static int bankCount = 3;

    final static String FILE_EXT = "4ds";

    /**
     *
     * @param filePath
     * @return
     */
    public static File[] createFilePathActiveBank(String filePath){
        File[] buffer = new File[bankCount];
        for(int i = 0; i < bankCount; i++){
            buffer[i] = createFilePathForBank(filePath, i);
        }

        return buffer;
    }

    /**
     *
     * @param filePath
     * @return
     */
    public static File createFilePathNoBank(String filePath){
        filePath = filePath.replaceAll(".4ds$", "");
        return new File(filePath + "." + FILE_EXT);
    }

    /**
     *
     * @param filePath
     * @param bank
     * @return
     */
    public static File createFilePathForBank(String filePath, int bank){
        filePath = filePath.replaceAll(".4ds$", "");
        filePath = filePath.replaceAll("-b[0-9]$", "");
        return new File(filePath + "-b" + bank + "." + FILE_EXT);
    }

}
