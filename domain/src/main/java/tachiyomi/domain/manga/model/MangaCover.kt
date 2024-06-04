package tachiyomi.domain.manga.model

import tachiyomi.domain.manga.interactor.GetCustomMangaInfo
import uy.kohesive.injekt.injectLazy
import java.util.concurrent.ConcurrentHashMap

/**
 * Contains the required data for MangaCoverFetcher
 */
data class MangaCover(
    val mangaId: Long,
    val sourceId: Long,
    val isMangaFavorite: Boolean,
    // SY -->
    val ogUrl: String?,
    // SY <--
    val lastModified: Long,
) {
    // SY -->
    private val customThumbnailUrl = if (isMangaFavorite) {
        getCustomMangaInfo.get(mangaId)?.thumbnailUrl
    } else {
        null
    }
    val url: String? = customThumbnailUrl ?: ogUrl
    // SY <--

    /**
     * [vibrantCoverColor] is used to set the color theme in manga detail page.
     * It contains color for all mangas, both in library or browsing.
     *
     * It reads/saves to a hashmap in [MangaCover.vibrantCoverColorMap] for multiple mangas.
     */
    var vibrantCoverColor: Int?
        get() = vibrantCoverColorMap[mangaId]
        set(value) {
            vibrantCoverColorMap[mangaId] = value
        }

    /**
     * [dominantCoverColors] is used to set cover/text's color in Library (Favorite) grid view.
     * It contains only color for in-library (favorite) mangas.
     *
     * It reads/saves to a hashmap in [MangaCover.coverColorMap].
     *
     * Format: <first: cover color, second: text color>.
     *
     * Set in *[MangaCoverMetadata.setRatioAndColors]* whenever browsing meets a favorite manga
     *  by loading from *[CoverCache]*.
     *
     * Get in *[CommonMangaItem.MangaCompactGridItem]*, *[CommonMangaItem.MangaComfortableGridItem]* and
     *  *[CommonMangaItem.MangaListItem]*
     */
    @Suppress("KDocUnresolvedReference")
    var dominantCoverColors: Pair<Int, Int>?
        get() = coverColorMap[mangaId]
        set(value) {
            value ?: return
            coverColorMap[mangaId] = value.first to value.second
        }

    var ratio: Float?
        get() = coverRatioMap[mangaId]
        set(value) {
            value ?: return
            coverRatioMap[mangaId] = value
        }
    companion object {
        private val getCustomMangaInfo: GetCustomMangaInfo by injectLazy()

        /**
         * [vibrantCoverColorMap] store color generated while browsing library.
         * It always empty at beginning each time app starts, then add more color while browsing.
         */
        val vibrantCoverColorMap: HashMap<Long, Int?> = hashMapOf()

        /**
         * [coverColorMap] stores favorite manga's cover & text's color as a joined string in Prefs.
         * They will be loaded each time *[App]* is initialized with *[MangaCoverMetadata.load]*.
         *
         * They will be saved back when *[MainActivity.onPause]* is triggered.
         */
        @Suppress("KDocUnresolvedReference")
        var coverColorMap = ConcurrentHashMap<Long, Pair<Int, Int>>()

        var coverRatioMap = ConcurrentHashMap<Long, Float>()
    }
}

fun Manga.asMangaCover(): MangaCover {
    return MangaCover(
        mangaId = id,
        sourceId = source,
        isMangaFavorite = favorite,
        ogUrl = thumbnailUrl,
        lastModified = coverLastModified,
    )
}
