package com.beat.data.model

data class Patch(
    var id: String? = "",
    val title: String,
    val patch: Int,
    var isDataLoaded: Boolean = false,
    var list: Any? = null
)