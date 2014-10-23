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


import com.spirit.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HelpMap {

    public static final Map<Integer, Integer> HELPMAP = createMap();

    private static Map<Integer, Integer> createMap() {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();

        result.put(R.id.position_select_id,                 R.string.st_26a7059fe05e155afb53ea22bc6a80d3);
        result.put(R.id.mix_select_id,                      R.string.st_b85e1174f4196a0bc9953209b0fdd6e0);
        result.put(R.id.receiver_select_id,                 R.string.st_06edb8aaa795fb406535ad1e42f7ddd9);
        result.put(R.id.cyclic_servo_reverse_select_id,     R.string.st_916898ebed3d1dbcb55c93994fec7ee1);
        result.put(R.id.flight_style_select_id,             R.string.st_9227b10a9b8a8dedbd04953e79838429);

        result.put(R.id.stabi_pitch,                        R.string.st_e26228953ce397eb25e87bcff3607d0e);
        result.put(R.id.stabi_ctrldir,                      R.string.st_9b25a70239fd85bc298f57a20029fbd1);
        result.put(R.id.stabi_fbmode,                       R.string.st_e0b37eca21ca760ec03fd9ac52a6593e);
        result.put(R.id.function_select_id,                 R.string.st_e0b37eca21ca760ec03fd9ac52a6593e);
        result.put(R.id.stabi_stick,                        R.string.st_701a785854a820040b882b1496fc7955);

        result.put(R.id.x_cyclic,                           R.string.st_57d3739e1771229afb2a5cf08f4de57f);
        result.put(R.id.z_rudder,                           R.string.st_a0a5325d05a2fcf5406274dd8e8d4bbb);
        result.put(R.id.gyro_gain,                          R.string.st_db27f93942fe39c3180a2dce7ae1ade7);

        result.put(R.id.x_pitch_reverse,                    R.string.st_95e83a5f5c4f175cfefe9b06620fc385);
        result.put(R.id.y_roll_reverse,                     R.string.st_972307e460ecd3df8303f72849faaf36);
        result.put(R.id.z_yaw_reverse,                      R.string.st_6008eeff8fc4acff6e4ac6c6236c392e);

        result.put(R.id.x_pitch_rates,                      R.string.st_4e53a94a4c21b522bce145863f4a5172);
        result.put(R.id.z_yaw_rates,                        R.string.st_85767e852871648bc2db9dd4b052cf63);

        result.put(R.id.cyclicff,                           R.string.st_6c27918077d556edb488f0516b5965b6);
        result.put(R.id.cyclic_phase,                       R.string.st_cdf89e8bd20739bdc69e8c3bd7e1ae17);
        result.put(R.id.e_filter,                           R.string.st_45b61e5018b2249985746acf8e3424b3);
        result.put(R.id.geom_6deg,                          R.string.st_472b0880db5cbccd3313e907fdc060b8);
        result.put(R.id.piro_opt,                           R.string.st_91bffbd63276730c22ff5dc509529fa7);
        result.put(R.id.pirouette_const,                    R.string.st_7ce4b0712e836431a22ff08c683ad49f);
        result.put(R.id.pitchup,                            R.string.st_6385c49bb6758a6bd3d458d4da55e7f7);
        result.put(R.id.rudder_delay,                       R.string.st_88ffaae209a942ffdcbf08d25ada6106);
        result.put(R.id.rudder_stop,                        R.string.st_756f850763677150a656703d746c341d);
        result.put(R.id.rudder_revomix,                     R.string.st_686fb3385b5643aee06ef0d818ff7f00);
        result.put(R.id.signal_processing,                  R.string.st_89812e191ca975f09841f272d593c418);
        result.put(R.id.stick_db,                           R.string.st_02282a0a0af7a569d19732b906395ed5);


        return Collections.unmodifiableMap(result);
    }
}
