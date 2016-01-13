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

        result.put(R.id.position_select_id, R.string.st_26a7059fe05e155afb53ea22bc6a80d3);
        result.put(R.id.mix_select_id, R.string.st_a030379814acf4a9efd5735573f71718);
        result.put(R.id.receiver_select_id, R.string.st_06edb8aaa795fb406535ad1e42f7ddd9);
        result.put(R.id.flight_style_select_id, R.string.st_4e74822308285a5d25761bc2c9387dc2);

        result.put(R.id.ch1_reverse, R.string.st_23156aa787644331f6b928b0138a8b89);
        result.put(R.id.ch2_reverse, R.string.st_be917f6b3fe715e4c62861f9729e1258);
        result.put(R.id.ch3_reverse, R.string.st_e7a0ee9287ffcd5017f33971dfd7a04b);
        result.put(R.id.ch4_reverse, R.string.st_a95ed2ec1c7f619a803847d6f867f5ab);

        result.put(R.id.stabi_pitch, R.string.st_25e1dad714a36a48c70307e9e98c6839);
        result.put(R.id.stabi_ctrldir, R.string.st_1bb89987a641483fa0a25daa7e52d27c);
        result.put(R.id.stabi_fbmode, R.string.st_64d671d1921bac26046cf0d04da37ff1);
        result.put(R.id.stabi_stick, R.string.st_bd22e71854e51838095b67f8c1d8441d);
        result.put(R.id.function_select_id, R.string.st_10fb0c0b85ce9243f0580f63f5509dd9);

        result.put(R.id.rotor_rotation, R.string.st_c6962067146e753eb26dae903a72e046);

        result.put(R.id.x_cyclic, R.string.st_40ac2993f4b9439ae49d61f0521b121d);
        result.put(R.id.rudder_common_gain, R.string.st_466a542ef886802f5bbc2c23bc91fd06);
        result.put(R.id.gyro_gain, R.string.st_e1ef2f3a7b6fa63d68cb2b9b2e72a571);

        result.put(R.id.x_pitch_rates, R.string.st_89aa34aff73db8d200536bf76779270b);
        result.put(R.id.z_yaw_rates, R.string.st_85767e852871648bc2db9dd4b052cf63);

        result.put(R.id.cyclicff, R.string.st_6c27918077d556edb488f0516b5965b6);
        result.put(R.id.cyclic_phase, R.string.st_cdf89e8bd20739bdc69e8c3bd7e1ae17);
        result.put(R.id.e_filter, R.string.st_45b61e5018b2249985746acf8e3424b3);
        result.put(R.id.geom_6deg, R.string.st_6758e8b02736ba47f992c8a921bd6eae);
        result.put(R.id.pirouette_const, R.string.st_a09dcd0b0fd2a8ed24f9d8a81b66668f);
        result.put(R.id.pitchup, R.string.st_28953f535f7d4bbe0b04ab8ffcbf3fde);
        result.put(R.id.rudder_delay, R.string.st_88ffaae209a942ffdcbf08d25ada6106);
        result.put(R.id.rudder_stop, R.string.st_050eaf6851d88f98084291c19b591a0d);
        result.put(R.id.rudder_revomix, R.string.st_0d024298132ffb58bce4dce98affdb86);
        result.put(R.id.signal_processing, R.string.st_9a77db935bc1b64ee6402e6c8d019d44);
        result.put(R.id.stick_db, R.string.st_02282a0a0af7a569d19732b906395ed5);

        result.put(R.id.rudder_limit_right, R.string.st_e11bdb4cd957e81f87afdc4b2c3134fe);
        result.put(R.id.rudder_limit_left, R.string.st_08a5a1e5848935703bcc7d12cf34f318);

        result.put(R.id.limit_range_ail_left, R.string.st_cb447c2f52a1170da6ca24c5875ba250);
        result.put(R.id.cyclic_pitch, R.string.st_b81fcebbff4a29a95ef70e6621eb8f71);

        result.put(R.id.governor_thr_min, R.string.st_3104a8b88d16fea1d7340e06f4f4628f);
        result.put(R.id.governor_thr_max, R.string.st_e3f7aaeec7262407e622dfb1b79181ab);
        result.put(R.id.governor_rampup, R.string.st_5f6a222244b449ed513ffe39b532715e);
        result.put(R.id.governor_divider, R.string.st_cffee8820dbdc898c99db0f0350440f4);
        result.put(R.id.thr_reverse, R.string.st_e4df1e47f3a179a3867c07d758619a1c);
        result.put(R.id.governor_ratio, R.string.st_794fca310ce523ae9cda3e4a0f9c5b1d);
        result.put(R.id.governor_rpm_max, R.string.st_3f768495d1b414cb1277d3b1f3ae2691);
        result.put(R.id.governor_pgain, R.string.st_b1f1153100c33f0ce17c0264026d5bec);
        result.put(R.id.governor_igain, R.string.st_d12c351a24a5319dcbcbc77d25195675);
        result.put(R.id.pitch_pump, R.string.st_6bf8cb04f7fdb6f926ad978ad55e7980);

        result.put(R.id.aileron_picker, R.string.st_be5bf619800ccb76a8ac96ef6c5b70c2);
        result.put(R.id.elevator_picker, R.string.st_6b9d810c0062eba5db4c223ae3d94fab);
        result.put(R.id.pitch_picker, R.string.st_92b0fe0b01b51063f423a7cc8b02cc1e);
        result.put(R.id.rudder_picker, R.string.st_54ceaabd2d8cca5320e800e9851b6ff3);

        result.put(R.id.governor_spoolup_select_id, R.string.st_cc41c2874a69e4ae7c3f2385ec481160);
        result.put(R.id.governor_freq_select_id, R.string.st_e6a7a3cb5827ad43acc85eac1898573a);


        result.put(R.id.servo_travel_ch1_negative, R.string.st_ed310c7971c2c68eb470582edc55e4bc);
        result.put(R.id.servo_travel_ch2_negative, R.string.st_e65b23e5ee819925adc8528b057de732);
        result.put(R.id.servo_travel_ch3_negative, R.string.st_e0190df24285d7fbcd471cdd56c434d6);
        result.put(R.id.servo_travel_ch1_positive, R.string.st_394f59a5d0657b8bd984cf5c653ff47c);
        result.put(R.id.servo_travel_ch2_positive, R.string.st_d2fc1adb2cb1077f97a2796f6f6468fd);
        result.put(R.id.servo_travel_ch3_positive, R.string.st_65664b6c1cbace3d687615342a2374a1);

        result.put(R.id.rpm_senzor_filter, R.string.st_a257dd5c98c91e67d5c02cfc2f439202);
        result.put(R.id.stabi_acro_delay, R.string.st_698dbfa44b9af463920bb489c14c81a2);


        return Collections.unmodifiableMap(result);
    }
}