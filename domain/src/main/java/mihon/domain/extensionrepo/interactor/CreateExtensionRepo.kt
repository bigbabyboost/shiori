package mihon.domain.extensionrepo.interactor

import logcat.LogPriority
import mihon.domain.extensionrepo.exception.SaveExtensionRepoException
import mihon.domain.extensionrepo.model.ExtensionRepo
import mihon.domain.extensionrepo.repository.ExtensionRepoRepository
import mihon.domain.extensionrepo.service.ExtensionRepoService
import tachiyomi.core.common.util.system.logcat

class CreateExtensionRepo(
    private val repository: ExtensionRepoRepository,
    private val service: ExtensionRepoService,
) {
    private val repoRegex = """^https://.*/index\.min\.json$""".toRegex()

    suspend fun await(repoUrl: String): Result {
        // Do not allow invalid formats & avoid adding duplicating official repo
        if (!repoUrl.matches(repoRegex) || repoUrl.startsWith(OFFICIAL_REPO_BASE_URL)) {
            return Result.InvalidUrl
        }

        val baseUrl = repoUrl.removeSuffix("/index.min.json")
        return service.fetchRepoDetails(baseUrl)?.let { insert(it) } ?: Result.InvalidUrl
    }

    private suspend fun insert(repo: ExtensionRepo): Result {
        return try {
            repository.insertRepo(
                repo.baseUrl,
                repo.name,
                repo.shortName,
                repo.website,
                repo.signingKeyFingerprint,
            )
            Result.Success
        } catch (e: SaveExtensionRepoException) {
            logcat(LogPriority.WARN, e) { "SQL Conflict attempting to add new repository ${repo.baseUrl}" }
            return handleInsertionError(repo)
        }
    }

    /**
     * Error Handler for insert when there are trying to create new repositories
     *
     * SaveExtensionRepoException doesn't provide constraint info in exceptions.
     * First check if the conflict was on primary key. if so return RepoAlreadyExists
     * Then check if the conflict was on fingerprint. if so Return DuplicateFingerprint
     * If neither are found, there was some other Error, and return Result.Error
     *
     * @param repo Extension Repo holder for passing to DB/Error Dialog
     */
    private suspend fun handleInsertionError(repo: ExtensionRepo): Result {
        val repoExists = repository.getRepo(repo.baseUrl)
        if (repoExists != null) {
            return Result.RepoAlreadyExists
        }
        val matchingFingerprintRepo = repository.getRepoBySigningKeyFingerprint(repo.signingKeyFingerprint)
        if (matchingFingerprintRepo != null) {
            return Result.DuplicateFingerprint(matchingFingerprintRepo, repo)
        }
        return Result.Error
    }

    sealed interface Result {
        data class DuplicateFingerprint(val oldRepo: ExtensionRepo, val newRepo: ExtensionRepo) : Result
        data object InvalidUrl : Result
        data object RepoAlreadyExists : Result
        data object Success : Result
        data object Error : Result
    }

    companion object {
        const val OFFICIAL_REPO_BASE_URL = "https://raw.githubusercontent.com/komikku-app/extensions/repo"

        // bigbabyboost's key
        const val OFFICIAL_REPO_SIGNATURE = "cbec121aa82ebb02aaa73806992e0368a97d47b5451ed6524816d03084c45905"
    }
}
