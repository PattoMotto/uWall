package com.pattomotto.uwall

data class UnsplashUser(val id: String,
                        val username: String,
                        val name: String)

data class UnsplashLinks(val self: String,
                         val html: String,
                         val download: String,
                         val downloadLocation: String)

data class UnsplashUrls(val raw: String,
                        val full: String,
                        val regular: String,
                        val thumb: String,
                        val custom: String)

data class UnsplashPhoto(val id: String,
                         val urls: UnsplashUrls,
                         val user: UnsplashUser,
                         val links: UnsplashLinks)

fun photoInfoFromUnsplashPhoto(unsplashPhoto: UnsplashPhoto) =
        PhotoInfo(
                Source.UNSPLASH,
                unsplashPhoto.user.name,
                unsplashPhoto.links.html,
                unsplashPhoto.urls.raw
        )

enum class Source(val sourceName: String,
                  val sourceUrl: String,
                  val refParam: String) {
    UNSPLASH("Unsplash",
            "https://unsplash.com/search/photos/",
            "?utm_source=" + BuildConfig.APPLICATION_ID + "&utm_medium=referral"),
    PIXABAY("Pixabay",
            "https://pixabay.com/en/photos/?q=",
            "?utm_source=" + BuildConfig.APPLICATION_ID + "&utm_medium=referral")
}

data class PhotoInfo(val source: Source,
                     val photographerName: String,
                     val photoUrl: String,
                     val downloadUrl: String)
