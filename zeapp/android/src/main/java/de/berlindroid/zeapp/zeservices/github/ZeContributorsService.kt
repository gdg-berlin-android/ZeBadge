package de.berlindroid.zeapp.zeservices.github

import de.berlindroid.zeapp.zeui.zeabout.Contributor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ZeContributorsService
    @Inject
    constructor(
        private val githubApi: GitHubApi,
    ) {
        fun contributors(page: Int): Flow<List<Contributor>> =
            flow {
                val contributors = githubApi.getContributors(page)

                emit(
                    contributors.map { Contributor(it.login, it.url, it.imageUrl, it.contributions) },
                )
            }.flowOn(Dispatchers.IO)
    }
