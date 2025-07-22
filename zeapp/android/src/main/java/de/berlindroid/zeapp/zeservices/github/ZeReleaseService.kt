package de.berlindroid.zeapp.zeservices.github

import de.berlindroid.zeapp.BuildConfig
import javax.inject.Inject
import okio.IOException
import retrofit2.HttpException
import timber.log.Timber

class ZeReleaseService
    @Inject
    constructor(
        private val githubApi: GitHubApi,
    ) {
        suspend fun getLatestRelease() =
            try {
                // We only need the latest release
                githubApi.getReleases(pageSize = 1).firstOrNull()
            } catch (ioException: IOException) {
                Timber.w("Failed to get latest version", ioException)
                null
            } catch (httpException: HttpException) {
                Timber.w("Failed to get latest version", httpException)
                null
            }

        suspend fun getNewRelease(): Int? =
            try {
                getLatestRelease()?.tagName?.toInt()?.takeIf {
                    it > BuildConfig.VERSION_CODE
                }
            } catch (nfe: NumberFormatException) {
                null
            }
    }
