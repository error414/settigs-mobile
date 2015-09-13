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

public class SerialNumber {

    /**
     *
     */
    private byte[] serialNumber;

    /**
     *
     * @param serialNumber
     */
    public SerialNumber(byte[] serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     *
     * @return
     */
    public boolean isProVersion() {
        if (!isValid()) {
            return false;
        }

        return (((serialNumber[4] & 0xff) & 0x01) != 0);
    }

    /**
     *
     * @return
     */
    public boolean isValid()
    {
        return !(serialNumber == null || serialNumber.length != 6);
    }

    /**
     *
     * @return
     */
    public String getString()
    {
        String serialFormat = "";
        for (byte b : serialNumber) {
            serialFormat = serialFormat + ByteOperation.byteToHexString(b) + " ";
        }

        return serialFormat;
    }

}
