package com.serviseyem.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.domain.entities.RatingReviewEntity
import com.serviseyem.ui.screens.dashboard.components.UnifiedEmptyState
import com.serviseyem.ui.screens.dashboard.components.UnifiedReviewsSection
import com.serviseyem.utils.VisualThemePalette

@Composable
fun TabReviewsFeedback(
    reviews: List<RatingReviewEntity>,
    themeColors: VisualThemePalette,
    onReplySubmit: (reviewId: String, replyText: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "⭐ آراء وتقييمات العملاء (${reviews.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textPrimary
        )

        if (reviews.isEmpty()) {
            UnifiedEmptyState(
                title = "لا توجد تقييمات مسجلة حالياً",
                description = "ستظهر تقييمات العملاء وتعليقاتهم هنا بعد إتمام الخدمات.",
                iconText = "⭐",
                themeColors = themeColors
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(reviews, key = { it.id }) { rev ->
                    UnifiedReviewsSection(
                        userName = rev.authorName.ifBlank { "عميل" },
                        rating = rev.rating,
                        comment = rev.comment,
                        ownerReply = "",
                        themeColors = themeColors,
                        onReplySubmit = { replyText ->
                            onReplySubmit(rev.id, replyText)
                        }
                    )
                }
            }
        }
    }
}
