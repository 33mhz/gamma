package io.pnut.gamma.domain.model.preference

import androidx.annotation.DrawableRes
import io.pnut.gamma.R


enum class ShapeOfAvatar(@DrawableRes val drawableRes: Int) {
    Circle(R.drawable.bg_avatar_circle),
    Rounded(R.drawable.bg_avatar_rounded),
    Square(R.drawable.bg_avatar_square);

}