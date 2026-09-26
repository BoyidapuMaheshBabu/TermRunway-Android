package com.termrunway.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TermRunwayTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold, fontSize = 34.sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(lineHeight = 24.sp)
    )
}
