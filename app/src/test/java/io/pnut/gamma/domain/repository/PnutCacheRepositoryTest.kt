package io.pnut.gamma.domain.repository

import android.content.Context
import io.pnut.gamma.data.db.dao.CacheDao
import io.pnut.gamma.data.db.entities.CachedPostEntity
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.sample.Posts
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.junit.Assert.assertEquals
import java.io.File

class PnutCacheRepositoryTest {

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var cacheDao: CacheDao

    private lateinit var repository: PnutCacheRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(context.cacheDir).thenReturn(File("/tmp"))
        repository = PnutCacheRepository("user1", context, cacheDao)
    }

    @Test
    fun `storePosts should resize list using min when cacheSize is positive`() = runBlocking {
        val posts = listOf(
            PageableItemWrapper.Item(Posts.normalPost),
            PageableItemWrapper.Item(Posts.normalPost),
            PageableItemWrapper.Item(Posts.normalPost)
        )
        val cacheSize = 2

        repository.storePosts(posts, StreamType.Home, cacheSize)

        val captor = argumentCaptor<List<CachedPostEntity>>()
        verify(cacheDao).replacePosts(any(), any(), captor.capture())

        assertEquals(2, captor.firstValue.size)
    }

    @Test
    fun `storePosts should not resize list when cacheSize is zero`() = runBlocking {
        val posts = listOf(
            PageableItemWrapper.Item(Posts.normalPost),
            PageableItemWrapper.Item(Posts.normalPost),
            PageableItemWrapper.Item(Posts.normalPost)
        )
        val cacheSize = 0

        repository.storePosts(posts, StreamType.Home, cacheSize)

        val captor = argumentCaptor<List<CachedPostEntity>>()
        verify(cacheDao).replacePosts(any(), any(), captor.capture())
        
        assertEquals(3, captor.firstValue.size)
    }
}
