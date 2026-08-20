package io.pnut.gamma.domain

import io.pnut.gamma.domain.entity.User

enum class Relationship {
    Follow, UnFollow,
    Block, UnBlock,
    Mute, UnMute;

    companion object {
        fun getRelationship(user: User): Relationship {
            return when {
                user.youBlocked -> Block
                user.youFollow -> Follow
                else -> UnFollow
            }
        }
    }
}