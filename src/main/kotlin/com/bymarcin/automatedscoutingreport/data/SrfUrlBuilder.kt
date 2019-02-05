package com.bymarcin.automatedscoutingreport.data

import com.bymarcin.automatedscoutingreport.encodeURL

class SrfUrlBuilder {
    private val formUrl = "https://airtable.com/shrpoiulL1A3IFGeu?"
    private val planetTypeConverter = mapOf<String, String>(
            "HIGH METAL CONTENT BODY" to "HMC",
            "METAL RICH BODY" to "Metal-Rich",
            "ROCKY BODY" to "Rocky",
            "ICY BODY" to "Ice",
            "ROCKY ICE BODY" to "Rocky Ice"
    )
    private val formFields = mutableSetOf<String>()
    fun region(value: String): SrfUrlBuilder {
        formFields.add("prefill_Region=${value.encodeURL()}")
        return this
    }

    fun system(value: String): SrfUrlBuilder {
        formFields.add("prefill_System=${value.encodeURL()}")
        return this
    }

    fun planetName(value: String): SrfUrlBuilder {
        formFields.add("prefill_Planet+Name=${value.encodeURL()}")
        return this
    }

    fun planetType(value: String): SrfUrlBuilder {
        val upperValue = value.toUpperCase()
        if(planetTypeConverter.containsKey(upperValue))
            formFields.add("prefill_Planet+Type=${planetTypeConverter[upperValue]!!.encodeURL()}")
        return this
    }

    fun scoutedBy(value: String): SrfUrlBuilder {
        formFields.add("prefill_Scouted+by=${value.encodeURL()}")
        return this
    }

    fun bio(value: String): SrfUrlBuilder {
        formFields.add("prefill_Bio+POI%27s=${value.encodeURL()}")
        return this
    }

    fun bio(value: Int): SrfUrlBuilder {
        return bio(value.toString())
    }

    fun geo(value: String): SrfUrlBuilder {
        formFields.add("prefill_Geo+POI%27s=${value.encodeURL()}")
        return this
    }

    fun geo(value: Int): SrfUrlBuilder {
        return geo(value.toString())
    }

    fun thargoid(value: String): SrfUrlBuilder {
        formFields.add("prefill_Thargoid+POI%27s=${value.encodeURL()}")
        return this
    }

    fun thargoid(value: Int): SrfUrlBuilder {
        return thargoid(value.toString())
    }

    fun human(value: String): SrfUrlBuilder {
        formFields.add("prefill_Human+POI%27s=${value.encodeURL()}")
        return this
    }

    fun human(value: Int): SrfUrlBuilder {
        return human(value.toString())
    }

    fun materials(list: List<Material>): SrfUrlBuilder {
        if(list.isNotEmpty()) {
            val materials = list.map { it.toString() }.reduce { acc, material -> "$acc,\n$material" }
            formFields.add("prefill_Planet+Materials=${materials.encodeURL()}")
        }
        return this
    }

    fun build(): String {
        val urlParams = formFields.reduce { acc, s -> "$acc&$s" }
        return "$formUrl$urlParams"
    }
}