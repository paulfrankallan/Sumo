package platform

const val RES_ID_DIE_1 = "res_id_die_1"
const val RES_ID_DIE_2 = "res_id_die_2"
const val RES_ID_DIE_3 = "res_id_die_3"
const val RES_ID_DIE_4 = "res_id_die_4"
const val RES_ID_DIE_5 = "res_id_die_5"
const val RES_ID_MUSIC_3 = "res_id_music_3"
const val RES_ID_HOME_LOOP = "res_id_home_loop"
const val RES_ID_HAKKEYOI = "res_id_hakkeyoi"
const val RES_ID_JIKAN_DESU = "res_id_jikan_desu"
const val RES_ID_MATTA_NASHI = "res_id_matta_nashi"
const val RES_ID_NOKOTTA = "res_id_nokotta"
const val RES_ID_SHOBUARI_GAME_OVER = "res_id_shobuari_game_over"
const val RES_ID_TE_O_TSUITE = "res_id_te_o_tsuite"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ResourceIdProvider {
    fun getResourceId(resourceName: String): Int?
}