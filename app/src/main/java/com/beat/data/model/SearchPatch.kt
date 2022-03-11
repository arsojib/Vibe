package com.beat.data.model

data class SearchPatch(
    val title: String,
    val patch: Int,
    var list: Any? = null
)