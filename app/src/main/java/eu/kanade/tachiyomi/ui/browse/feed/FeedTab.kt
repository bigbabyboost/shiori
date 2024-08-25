package eu.kanade.tachiyomi.ui.browse.feed

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalHapticFeedback
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.FeedActionsDialog
import eu.kanade.presentation.browse.FeedAddDialog
import eu.kanade.presentation.browse.FeedAddSearchDialog
import eu.kanade.presentation.browse.FeedDeleteConfirmDialog
import eu.kanade.presentation.browse.FeedScreen
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.TabContent
import eu.kanade.tachiyomi.ui.browse.AddDuplicateMangaDialog
import eu.kanade.tachiyomi.ui.browse.AllowDuplicateDialog
import eu.kanade.tachiyomi.ui.browse.BulkFavoriteScreenModel
import eu.kanade.tachiyomi.ui.browse.ChangeMangaCategoryDialog
import eu.kanade.tachiyomi.ui.browse.ChangeMangasCategoryDialog
import eu.kanade.tachiyomi.ui.browse.RemoveMangaDialog
import eu.kanade.tachiyomi.ui.browse.bulkSelectionButton
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import eu.kanade.tachiyomi.ui.home.HomeScreen
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tachiyomi.domain.source.interactor.GetRemoteManga
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun feedTab(
    // KMK -->
    screenModel: FeedScreenModel,
    bulkFavoriteScreenModel: BulkFavoriteScreenModel,
    // KMK <--
): TabContent {
    val navigator = LocalNavigator.currentOrThrow
    val state by screenModel.state.collectAsState()

    // KMK -->
    val bulkFavoriteState by bulkFavoriteScreenModel.state.collectAsState()

    val haptic = LocalHapticFeedback.current

    BackHandler(enabled = bulkFavoriteState.selectionMode) {
        bulkFavoriteScreenModel.backHandler()
    }

    LaunchedEffect(bulkFavoriteState.selectionMode) {
        HomeScreen.showBottomNav(!bulkFavoriteState.selectionMode)
    }
    // KMK <--

    DisposableEffect(navigator.lastEvent) {
        if (navigator.lastEvent == StackEvent.Push) {
            screenModel.pushed = true
        } else if (!screenModel.pushed) {
            screenModel.init()
        }

        onDispose {
            if (navigator.lastEvent == StackEvent.Idle && screenModel.pushed) {
                screenModel.pushed = false
            }
        }
    }

    return TabContent(
        titleRes = SYMR.strings.feed,
        actions = persistentListOf(
            AppBar.Action(
                title = stringResource(MR.strings.action_add),
                icon = Icons.Outlined.Add,
                onClick = {
                    screenModel.openAddDialog()
                },
            ),
            // KMK -->
            bulkSelectionButton(bulkFavoriteScreenModel::toggleSelectionMode),
            // KMK <--
        ),
        content = { contentPadding, snackbarHostState ->
            FeedScreen(
                state = state,
                contentPadding = contentPadding,
                onClickSavedSearch = { savedSearch, source ->
                    screenModel.sourcePreferences.lastUsedSource().set(savedSearch.source)
                    navigator.push(
                        BrowseSourceScreen(
                            source.id,
                            listingQuery = null,
                            savedSearch = savedSearch.id,
                        ),
                    )
                },
                onClickSource = { source ->
                    screenModel.sourcePreferences.lastUsedSource().set(source.id)
                    navigator.push(
                        BrowseSourceScreen(
                            source.id,
                            // KMK -->
                            listingQuery = if (!source.supportsLatest) {
                                GetRemoteManga.QUERY_POPULAR
                            } else {
                                // KMK <--
                                GetRemoteManga.QUERY_LATEST
                            },
                        ),
                    )
                },
                // KMK -->
                onLongClickFeed = screenModel::openActionsDialog,
                // KMK <--
                onClickManga = { manga ->
                    // KMK -->
                    if (bulkFavoriteState.selectionMode) {
                        bulkFavoriteScreenModel.toggleSelection(manga)
                    } else {
                        // KMK <--
                        navigator.push(MangaScreen(manga.id, true))
                    }
                },
                // KMK -->
                onLongClickManga = { manga ->
                    if (!bulkFavoriteState.selectionMode) {
                        bulkFavoriteScreenModel.addRemoveManga(manga, haptic)
                    } else {
                        navigator.push(MangaScreen(manga.id, true))
                    }
                },
                selection = bulkFavoriteState.selection,
                // KMK <--
                onRefresh = screenModel::init,
                getMangaState = { manga -> screenModel.getManga(initialManga = manga) },
            )

            state.dialog?.let { dialog ->
                when (dialog) {
                    is FeedScreenModel.Dialog.AddFeed -> {
                        FeedAddDialog(
                            sources = dialog.options,
                            onDismiss = screenModel::dismissDialog,
                            onClickAdd = {
                                if (it != null) {
                                    screenModel.openAddSearchDialog(it)
                                }
                                screenModel.dismissDialog()
                            },
                        )
                    }
                    is FeedScreenModel.Dialog.AddFeedSearch -> {
                        FeedAddSearchDialog(
                            source = dialog.source,
                            savedSearches = dialog.options,
                            onDismiss = screenModel::dismissDialog,
                            onClickAdd = { source, savedSearch ->
                                screenModel.createFeed(source, savedSearch)
                                screenModel.dismissDialog()
                            },
                        )
                    }
                    is FeedScreenModel.Dialog.DeleteFeed -> {
                        FeedDeleteConfirmDialog(
                            feed = dialog.feed,
                            onDismiss = screenModel::dismissDialog,
                            onClickDeleteConfirm = {
                                screenModel.deleteFeed(it)
                                screenModel.dismissDialog()
                            },
                        )
                    }
                    // KMK -->
                    is FeedScreenModel.Dialog.FeedActions -> {
                        FeedActionsDialog(
                            feedItem = dialog.feedItem,
                            hasPrevFeed = dialog.prevFeed != null,
                            hasNextFeed = dialog.nextFeed != null,
                            onDismiss = screenModel::dismissDialog,
                            onClickDelete = {
                                screenModel.openDeleteDialog(dialog.feedItem.feed)
                                screenModel.dismissDialog()
                            },
                            onMoveUp = {
                                dialog.prevFeed?.let { screenModel.swapFeedOrder(dialog.feedItem.feed, it) }
                                screenModel.dismissDialog()
                            },
                            onMoveDown = {
                                dialog.nextFeed?.let { screenModel.swapFeedOrder(dialog.feedItem.feed, it) }
                                screenModel.dismissDialog()
                            },
                            onMoveBottom = {
                                dialog.nextFeed?.let { screenModel.moveToBottom(dialog.feedItem.feed) }
                                screenModel.dismissDialog()
                            },
                        )
                    }
                    // KMK <--
                }
            }

            // KMK -->
            when (bulkFavoriteState.dialog) {
                is BulkFavoriteScreenModel.Dialog.AddDuplicateManga ->
                    AddDuplicateMangaDialog(bulkFavoriteScreenModel)
                is BulkFavoriteScreenModel.Dialog.RemoveManga ->
                    RemoveMangaDialog(bulkFavoriteScreenModel)
                is BulkFavoriteScreenModel.Dialog.ChangeMangaCategory ->
                    ChangeMangaCategoryDialog(bulkFavoriteScreenModel)
                is BulkFavoriteScreenModel.Dialog.ChangeMangasCategory ->
                    ChangeMangasCategoryDialog(bulkFavoriteScreenModel)
                is BulkFavoriteScreenModel.Dialog.AllowDuplicate ->
                    AllowDuplicateDialog(bulkFavoriteScreenModel)
                else -> {}
            }
            // KMK <--

            val internalErrString = stringResource(MR.strings.internal_error)
            val tooManyFeedsString = stringResource(KMR.strings.too_many_in_feed)
            LaunchedEffect(Unit) {
                screenModel.events.collectLatest { event ->
                    when (event) {
                        FeedScreenModel.Event.FailedFetchingSources -> {
                            launch { snackbarHostState.showSnackbar(internalErrString) }
                        }
                        FeedScreenModel.Event.TooManyFeeds -> {
                            launch { snackbarHostState.showSnackbar(tooManyFeedsString) }
                        }
                    }
                }
            }
        },
    )
}
