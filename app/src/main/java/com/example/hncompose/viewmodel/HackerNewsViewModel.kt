package com.example.hncompose.viewmodel

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackernetwork.HNItem
import com.example.hackernetwork.HackerNewsRepo
import com.example.hncompose.AppDataStatus
import com.example.hncompose.R
import kotlinx.coroutines.launch

/**
 * Activity View Model for the [MainActivity] that manages Hacker News data from
 * the network, and other non-Composable UI logic.
 */
class HackerNewsViewModel(private val repo: HackerNewsRepo): ViewModel() {

    private val pageSize: Int = 30

    /**
     * Refresh the list of Top stories from the network.
     *
     * @param fresh a boolean to force-reload of stories.
     */
    fun getTopStories(fresh: Boolean = true) {
        if (fresh || AppDataStatus.topStories.isNullOrEmpty()) {
            viewModelScope.launch {
                val response = repo.getTopStories()
                response.body()?.let { topStoryIds ->
                    AppDataStatus.topStoryIdChunks = topStoryIds.chunked(pageSize)
                    getTopStoryChunkDetails()
                }
            }
        }
    }

    /**
     * Refresh the list of New stories from the network.
     *
     * @param fresh a boolean to force-reload of stories.
     */
    fun getNewStories(fresh: Boolean = false) {
        if (fresh || AppDataStatus.newStories.isNullOrEmpty()) {
            viewModelScope.launch {
                val response = repo.getNewStories()
                response.body()?.let { newStoryIds ->
                    AppDataStatus.newStoryIdChunks = newStoryIds.chunked(pageSize)
                    getNewStoryChunkDetails()
                }
            }
        }
    }

    /**
     * Refresh the list of Job stories from the network.
     *
     * @param fresh a boolean to force-reload of stories.
     */
    fun getJobStories(fresh: Boolean = false) {
        if (fresh || AppDataStatus.jobStories.isNullOrEmpty()) {
            viewModelScope.launch {
                val response = repo.getJobStories()
                response.body()?.let { jobStoryIds ->
                    AppDataStatus.jobStoryIdChunks = jobStoryIds.chunked(pageSize)
                    getJobStoryChunkDetails()
                }
            }
        }
    }

    /**
     * Get the next page of Top news details.
     */
    fun getNextTopNewsChunk() {
        getTopStoryChunkDetails(nextChunkIndex(AppDataStatus.topStories.size))
    }

    /**
     * Get the next page of New news details.
     */
    fun getNextNewNewsChunk() {
        getNewStoryChunkDetails(nextChunkIndex(AppDataStatus.newStories.size))
    }

    /**
     * Get the next page of Job news details.
     */
    fun getNextJobNewsChunk() {
        getJobStoryChunkDetails(nextChunkIndex(AppDataStatus.jobStories.size))
    }

    val listenerHandler: HackerNewsListenerHandler = HackerNewsListenerHandler()

    /**
     * Callback to notify the VM to launch a Custom Tabs Intent to selected URL.
     */
    fun storyClicked(url: String?, context: Context) {
        url?.also {
            CustomTabsIntent.Builder()
                .setToolbarColor(context.getColor(R.color.purple500))
                .build()
                .apply {
                    launchUrl(context, Uri.parse(url))
                }
        }
    }

    // region Private Functions

    private fun getTopStoryChunkDetails(chunkIndex: Int = 0) {
        if (chunkIndex in AppDataStatus.topStoryIdChunks.indices) {
            viewModelScope.launch {
                AppDataStatus.topStories.addAll(
                    AppDataStatus.topStoryIdChunks[chunkIndex].mapIndexed { index, storyId ->
                        return@mapIndexed repo.getItem(storyId.toString()).body() ?: HNItem(id = index)
                    }
                )
            }
        }
    }

    private fun getNewStoryChunkDetails(chunkIndex: Int = 0) {
        if (chunkIndex in AppDataStatus.newStoryIdChunks.indices) {
            viewModelScope.launch {
                AppDataStatus.newStories.addAll(
                    AppDataStatus.newStoryIdChunks[chunkIndex].mapIndexed { index, storyId ->
                        return@mapIndexed repo.getItem(storyId.toString()).body() ?: HNItem(id = index)
                    }
                )
            }
        }
    }

    private fun getJobStoryChunkDetails(chunkIndex: Int = 0) {
        if (chunkIndex in AppDataStatus.jobStoryIdChunks.indices) {
            viewModelScope.launch {
                AppDataStatus.jobStories.addAll(
                    AppDataStatus.jobStoryIdChunks[chunkIndex].mapIndexed { index, storyId ->
                        return@mapIndexed repo.getItem(storyId.toString()).body() ?: HNItem(id = index)
                    }
                )
            }
        }
    }

    private fun nextChunkIndex(listSize: Int, chunkSize: Int = pageSize): Int {
        return listSize / chunkSize
    }

    // endregion Private Functions

    inner class HackerNewsListenerHandler (
        val handleLoadMoreTopStories: () -> Unit = { getNextTopNewsChunk() },
        val handleLoadMoreNewStories: () -> Unit = { getNextNewNewsChunk() },
        val handleLoadMoreJobStories: () -> Unit = { getNextJobNewsChunk() }
    )

}