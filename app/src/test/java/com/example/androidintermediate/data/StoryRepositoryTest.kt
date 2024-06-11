package com.example.androidintermediate.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.androidintermediate.DataDummy
import com.example.androidintermediate.MainDispatcherRule
import com.example.androidintermediate.MockLog
import com.example.androidintermediate.StoryPagingSource
import com.example.androidintermediate.data.local.StoryDatabase
import com.example.androidintermediate.data.model.StoryResponse
import com.example.androidintermediate.data.remote.ApiService
import com.example.androidintermediate.data.repository.StoryRepository
import com.example.androidintermediate.getOrAwaitValue
import com.example.androidintermediate.observeForTesting
import com.example.androidintermediate.views.adapter.StoryAdapter
import com.example.androidintermediate.views.viewmodel.StoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner::class)
class StoryRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyDatabase: StoryDatabase

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Before
    fun setup() {
        MockLog
    }

    @Test
    fun `Get stories with pager - successfully`() = runTest {
        val dummyStories = DataDummy.generateDummyListStory()
        val data: PagingData<StoryResponse.Story> = StoryPagingSource.snapshot(dummyStories)

        val expectedStory = MutableLiveData<PagingData<StoryResponse.Story>>()
        expectedStory.value = data

        `when`(storyRepository.getStory()).thenReturn(expectedStory)

        val storyViewModel = StoryViewModel(storyRepository)
        val actualQuote: PagingData<StoryResponse.Story> = storyViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = mainDispatcherRule.testDispatcher,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])

    }

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}