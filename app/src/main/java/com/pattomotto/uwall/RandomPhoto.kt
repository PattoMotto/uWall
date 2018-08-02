package com.pattomotto.uwall

data class RandomPhoto(val id: String,
                       val urls: Urls,
                       val user: User,
                       val links: Links)

data class User(val id: String,
                val username: String,
                val name: String)

data class Links(val self: String,
                 val html: String,
                 val download: String,
                 val downloadLocation: String)

data class Urls(val raw: String,
                val full: String,
                val regular: String,
                val thumb: String,
                val custom: String)