package platform

const val RES_ID_DIE_1 = "res_id_die_1"
const val RES_ID_DIE_2 = "res_id_die_2"
const val RES_ID_DIE_3 = "res_id_die_3"
const val RES_ID_DIE_4 = "res_id_die_4"
const val RES_ID_DIE_5 = "res_id_die_5"
const val RES_ID_MUSIC_3 = "res_id_music_3"
const val RES_ID_HOME_LOOP = "res_id_home_loop"
const val RES_ID_HAKKEYOI = "res_id_hakkeyoi"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ResourceIdProvider {
    fun getResourceId(resourceName: String): Int?
}