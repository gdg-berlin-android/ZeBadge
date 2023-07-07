package de.berlindroid.zeapp.zeservices

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ZeContributorsService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun contributors(): Flow<List<String>> = flow {
        emit(
            context.assets.open("test.txt")
                .bufferedReader()
                .readLines(),
        )
    }.flowOn(Dispatchers.IO)
}
