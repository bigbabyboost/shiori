package eu.kanade.tachiyomi.ui.manga

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import coil3.load
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.databinding.EditMangaDialogBinding
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.lang.chop
import eu.kanade.tachiyomi.util.system.dpToPx
import eu.kanade.tachiyomi.widget.materialdialogs.setTextInput
import exh.util.dropBlank
import exh.util.trimOrNull
import kotlinx.coroutines.CoroutineScope
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.source.local.isLocal

@Composable
fun EditMangaDialog(
    manga: Manga,
    onDismissRequest: () -> Unit,
    onPositiveClick: (
        title: String?,
        author: String?,
        artist: String?,
        thumbnailUrl: String?,
        description: String?,
        tags: List<String>?,
        status: Long?,
    ) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var binding by remember {
        mutableStateOf<EditMangaDialogBinding?>(null)
    }
    val colors = EditMangaDialogColors(
        textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(),
        iconColor = MaterialTheme.colorScheme.primary.toArgb(),
        tagColor = MaterialTheme.colorScheme.outlineVariant.toArgb(),
        tagFocusColor = MaterialTheme.colorScheme.outline.toArgb(),
        tagTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(),
        btnTextColor = MaterialTheme.colorScheme.onPrimary.toArgb(),
        btnBgColor = MaterialTheme.colorScheme.surfaceTint.toArgb(),
    )
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    @Suppress("NAME_SHADOWING")
                    val binding = binding ?: return@TextButton
                    onPositiveClick(
                        binding.title.text.toString(),
                        binding.mangaAuthor.text.toString(),
                        binding.mangaArtist.text.toString(),
                        binding.thumbnailUrl.text.toString(),
                        binding.mangaDescription.text.toString(),
                        binding.mangaGenresTags.getTextStrings(),
                        binding.status.selectedItemPosition.let {
                            when (it) {
                                1 -> SManga.ONGOING
                                2 -> SManga.COMPLETED
                                3 -> SManga.LICENSED
                                4 -> SManga.PUBLISHING_FINISHED
                                5 -> SManga.CANCELLED
                                6 -> SManga.ON_HIATUS
                                else -> null
                            }
                        }?.toLong(),
                    )
                    onDismissRequest()
                },
            ) {
                Text(stringResource(MR.strings.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(MR.strings.action_cancel))
            }
        },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                AndroidView(
                    factory = { factoryContext ->
                        EditMangaDialogBinding.inflate(LayoutInflater.from(factoryContext))
                            .also { binding = it }
                            .apply {
                                onViewCreated(
                                    manga,
                                    factoryContext,
                                    this,
                                    scope,
                                    colors,
                                )
                            }
                            .root
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}

class EditMangaDialogColors(
    @ColorInt val textColor: Int,
    @ColorInt val iconColor: Int,
    @ColorInt val tagColor: Int,
    @ColorInt val tagFocusColor: Int,
    @ColorInt val tagTextColor: Int,
    @ColorInt val btnTextColor: Int,
    @ColorInt val btnBgColor: Int,
)

private fun onViewCreated(
    manga: Manga,
    context: Context,
    binding: EditMangaDialogBinding,
    scope: CoroutineScope,
    colors: EditMangaDialogColors,
) {
    loadCover(manga, binding)

    val statusAdapter: ArrayAdapter<String> = ArrayAdapter(
        context,
        android.R.layout.simple_spinner_dropdown_item,
        listOf(
            MR.strings.label_default,
            MR.strings.ongoing,
            MR.strings.completed,
            MR.strings.licensed,
            MR.strings.publishing_finished,
            MR.strings.cancelled,
            MR.strings.on_hiatus,
        ).map { context.stringResource(it) },
    )

    binding.status.adapter = statusAdapter
    if (manga.status != manga.ogStatus) {
        binding.status.setSelection(
            when (manga.status.toInt()) {
                SManga.UNKNOWN -> 0
                SManga.ONGOING -> 1
                SManga.COMPLETED -> 2
                SManga.LICENSED -> 3
                SManga.PUBLISHING_FINISHED, 61 -> 4
                SManga.CANCELLED, 62 -> 5
                SManga.ON_HIATUS, 63 -> 6
                else -> 0
            },
        )
    }

    // Set Spinner's text color
    binding.status.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            if (view != null) (view as TextView).setTextColor(colors.textColor)
        }
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    // Set Spinner's dropdown caret color
    binding.status.backgroundTintList = ColorStateList.valueOf(colors.iconColor)

    if (manga.isLocal()) {
        if (manga.title != manga.url) {
            binding.title.setText(manga.title)
        }

        binding.title.hint = context.stringResource(SYMR.strings.title_hint, manga.url)
        binding.mangaAuthor.setText(manga.author.orEmpty())
        binding.mangaArtist.setText(manga.artist.orEmpty())
        binding.thumbnailUrl.setText(manga.thumbnailUrl.orEmpty())
        binding.mangaDescription.setText(manga.description.orEmpty())
        binding.mangaGenresTags.setChips(manga.genre.orEmpty().dropBlank(), scope, colors)
    } else {
        if (manga.title != manga.ogTitle) {
            binding.title.append(manga.title)
        }
        if (manga.author != manga.ogAuthor) {
            binding.mangaAuthor.append(manga.author.orEmpty())
        }
        if (manga.artist != manga.ogArtist) {
            binding.mangaArtist.append(manga.artist.orEmpty())
        }
        if (manga.thumbnailUrl != manga.ogThumbnailUrl) {
            binding.thumbnailUrl.append(manga.thumbnailUrl.orEmpty())
        }
        if (manga.description != manga.ogDescription) {
            binding.mangaDescription.append(manga.description.orEmpty())
        }
        binding.mangaGenresTags.setChips(manga.genre.orEmpty().dropBlank(), scope, colors)

        binding.title.hint = context.stringResource(SYMR.strings.title_hint, manga.ogTitle)

        binding.mangaAuthor.hint = context.stringResource(SYMR.strings.author_hint, manga.ogAuthor ?: "")
        binding.mangaArtist.hint = context.stringResource(SYMR.strings.artist_hint, manga.ogArtist ?: "")
        binding.mangaDescription.hint =
            context.stringResource(
                SYMR.strings.description_hint,
                manga.ogDescription?.takeIf { it.isNotBlank() }?.replace("\n", " ")?.chop(20) ?: ""
            )
        binding.thumbnailUrl.hint =
            context.stringResource(
                SYMR.strings.thumbnail_url_hint,
                manga.ogThumbnailUrl?.let {
                    it.chop(40) + if (it.length > 46) "." + it.substringAfterLast(".").chop(6) else ""
                } ?: ""
            )
    }
    binding.mangaGenresTags.clearFocus()

    binding.title.setTextColor(colors.textColor)
    binding.mangaAuthor.setTextColor(colors.textColor)
    binding.mangaArtist.setTextColor(colors.textColor)
    binding.thumbnailUrl.setTextColor(colors.textColor)
    binding.mangaDescription.setTextColor(colors.textColor)
    binding.titleOutline.boxStrokeColor = colors.iconColor
    binding.mangaAuthorOutline.boxStrokeColor = colors.iconColor
    binding.mangaArtistOutline.boxStrokeColor = colors.iconColor
    binding.thumbnailUrlOutline.boxStrokeColor = colors.iconColor
    binding.mangaDescriptionOutline.boxStrokeColor = colors.iconColor
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        binding.titleOutline.cursorColor = ColorStateList.valueOf(colors.iconColor)
        binding.mangaAuthorOutline.cursorColor = ColorStateList.valueOf(colors.iconColor)
        binding.mangaArtistOutline.cursorColor = ColorStateList.valueOf(colors.iconColor)
        binding.thumbnailUrlOutline.cursorColor = ColorStateList.valueOf(colors.iconColor)
        binding.mangaDescriptionOutline.cursorColor = ColorStateList.valueOf(colors.iconColor)
    }

    binding.resetTags.setTextColor(colors.btnTextColor)
    binding.resetTags.setBackgroundColor(colors.btnBgColor)
    binding.resetInfo.setTextColor(colors.btnTextColor)
    binding.resetInfo.setBackgroundColor(colors.btnBgColor)

    binding.resetTags.setOnClickListener { resetTags(manga, binding, scope, colors) }
    binding.resetInfo.setOnClickListener { resetInfo(manga, binding, scope, colors) }
}

private fun resetTags(
    manga: Manga,
    binding: EditMangaDialogBinding,
    scope: CoroutineScope,
    colors: EditMangaDialogColors,
) {
    if (manga.genre.isNullOrEmpty() || manga.isLocal()) {
        binding.mangaGenresTags.setChips(emptyList(), scope, colors)
    } else {
        binding.mangaGenresTags.setChips(manga.ogGenre.orEmpty(), scope, colors)
    }
}

private fun loadCover(manga: Manga, binding: EditMangaDialogBinding) {
    binding.mangaCover.load(manga) {
        transformations(RoundedCornersTransformation(4.dpToPx.toFloat()))
    }
}

private fun resetInfo(
    manga: Manga,
    binding: EditMangaDialogBinding,
    scope: CoroutineScope,
    colors: EditMangaDialogColors,
) {
    binding.title.setText("")
    binding.mangaAuthor.setText("")
    binding.mangaArtist.setText("")
    binding.thumbnailUrl.setText("")
    binding.mangaDescription.setText("")
    resetTags(manga, binding, scope, colors)
}

private fun ChipGroup.setChips(
    items: List<String>,
    scope: CoroutineScope,
    colors: EditMangaDialogColors,
) {
    removeAllViews()

    val colorStateList = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_active),
        ),
        intArrayOf(
            colors.tagFocusColor,
            colors.tagFocusColor,
            colors.tagColor,
        ),
    )

    items.asSequence().map { item ->
        Chip(context).apply {
            text = item
            setTextColor(colors.tagTextColor)

            isCloseIconVisible = true
            closeIcon?.setTint(colors.iconColor)
            setOnCloseIconClickListener {
                removeView(this)
            }

            chipBackgroundColor = colorStateList
        }
    }.forEach {
        addView(it)
    }

    val addTagChip = Chip(context).apply {
        text = SYMR.strings.add_tag.getString(context)
        setTextColor(colors.tagTextColor)

        chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_24dp)?.apply {
            isChipIconVisible = true
            setTint(colors.iconColor)
        }

        chipBackgroundColor = colorStateList

        setOnClickListener {
            var newTag: String? = null
            MaterialAlertDialogBuilder(context)
                .setTitle(SYMR.strings.add_tag.getString(context))
                .setTextInput {
                    newTag = it.trimOrNull()
                }
                .setPositiveButton(MR.strings.action_ok.getString(context)) { _, _ ->
                    if (newTag != null) setChips(items + listOfNotNull(newTag), scope, colors)
                }
                .setNegativeButton(MR.strings.action_cancel.getString(context), null)
                .show()
        }
    }
    addView(addTagChip)
}

private fun ChipGroup.getTextStrings(): List<String> = children.mapNotNull {
    if (it is Chip && !it.text.toString().contains(context.stringResource(SYMR.strings.add_tag), ignoreCase = true)) {
        it.text.toString()
    } else {
        null
    }
}.toList()
