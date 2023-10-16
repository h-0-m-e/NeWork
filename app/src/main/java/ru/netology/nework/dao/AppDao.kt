package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.UserEntity

@Dao
interface AppDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun postGetAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun eventGetAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM PostEntity WHERE authorId = :authorId ORDER BY id DESC")
    fun postGetPostsById(authorId: Long): Flow<List<PostEntity>>

    @Query("SELECT * FROM UserEntity WHERE id = :userId")
    suspend fun userGet(userId: Long): UserEntity

    @Query("SELECT * FROM JobEntity WHERE userId = :userId")
    suspend fun jobGet(userId: Long): JobEntity?

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun postIsEmpty(): Boolean

    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun eventIsEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun postInsert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun eventInsert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun userInsert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun jobInsert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun postInsert(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun eventInsert(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun userInsert(users: List<UserEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun postRemoveById(id: Long)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun eventRemoveById(id: Long)

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun postGetById(id: Long): PostEntity

    @Query("SELECT * FROM EventEntity WHERE id = :id")
    suspend fun eventGetById(id: Long): EventEntity

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes + 1,
        likedByMe = 1
        WHERE id = :id
        """
    )
    suspend fun postLikeById(id: Long)

    @Query(
        """
        UPDATE EventEntity SET
        likes = likes + 1,
        likedByMe = 1
        WHERE id = :id
        """
    )
    suspend fun eventLikeById(id: Long)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes - 1,
        likedByMe = 0
        WHERE id = :id
        """
    )
    suspend fun postUnlikeById(id: Long)

    @Query(
        """
        UPDATE EventEntity SET
        likes = likes - 1,
        likedByMe = 0
        WHERE id = :id
        """
    )
    suspend fun eventUnlikeById(id: Long)

}
