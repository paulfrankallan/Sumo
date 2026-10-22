package platform

import com.corbstech.sumo.shared.R

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ResourceIdProvider {
    actual fun getResourceId(resourceName: String): Int? {
        return when (resourceName) {
            RES_ID_DIE_1 -> R.raw.die1
            RES_ID_DIE_2 -> R.raw.die2
            RES_ID_DIE_3 -> R.raw.die3
            RES_ID_DIE_4 -> R.raw.die4
            RES_ID_DIE_5 -> R.raw.die5
            RES_ID_MUSIC_3 -> R.raw.sumo_music_drums
            RES_ID_HOME_LOOP -> R.raw.sumo_music_1
            RES_ID_HAKKEYOI -> R.raw.hakkeyoi
            RES_ID_HYOSHIGI -> R.raw.hyoshigi
            RES_ID_JIKAN_DESU -> R.raw.jikan_desu
            RES_ID_MATTA_NASHI -> R.raw.matta_nashi
            RES_ID_NOKOTTA -> R.raw.nokotta
            RES_ID_SHOBUARI_GAME_OVER -> R.raw.shobuari_game_over
            RES_ID_TE_O_TSUITE -> R.raw.te_o_tsuite
            else -> null
        }
    }
}
