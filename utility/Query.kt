package com.matteblack.utility

import com.runemate.game.api.hybrid.location.Area
import com.runemate.game.api.hybrid.queries.NpcQueryBuilder
import com.runemate.game.api.hybrid.region.Npcs
import com.runemate.game.api.hybrid.region.Players

object Query {
    fun getTargetableNpcs(names: String? = null, area: Area? = null): NpcQueryBuilder {
        val query =  Npcs.newQuery()
        if(names != null) {
            query.names(names)
        }
        if(area != null) {
            query.within(area)
        }
        return query.filter { (it.target == null || it.target == Players.getLocal())
                //Healthguage is null or percent health is greater than 0.
                && (it.healthGauge == null || it.healthGauge!!.percent > 0) }
    }
}