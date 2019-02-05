package com.bymarcin.automatedscoutingreport.data

class ScoutForm(
        val planetName: String,
        val planetType: String,
        val planetMaterials: String,
        val system: String,
        val region: String = "Inner Orion Spur",
        val bio: String = "0",
        val geo: String = "0",
        val thargoid: String = "0",
        val scoutedBy: String = "0",
        val stellarPhenomena: Boolean = false
)