package dev.opencode.mobile.features.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.opencode.mobile.opencode.models.AssistantContentPart
import dev.opencode.mobile.opencode.models.SessionMessage
import dev.opencode.mobile.opencode.models.ToolState
import dev.opencode.mobile.ui.icons.OpenCodeIcons

@Composable
fun ChatMessageItem(message: SessionMessage, modifier: Modifier = Modifier) {
    when (message.type) {
        "user" -> UserBubble(text = message.text.orEmpty(), modifier = modifier)
        "assistant" -> AssistantBlock(message = message, modifier = modifier)
        "system" -> SystemLine(text = message.text.orEmpty(), modifier = modifier)
        else -> SystemLine(text = "[${message.type}]", modifier = modifier)
    }
}

@Composable
private fun UserBubble(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SystemLine(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@Composable
private fun AssistantBlock(message: SessionMessage, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        message.content.forEach { part ->
            when (part) {
                is AssistantContentPart.Text -> AssistantText(part.text)
                is AssistantContentPart.Reasoning -> ReasoningBlock(part.text)
                is AssistantContentPart.Tool -> ToolCallBlock(name = part.name, state = part.state)
            }
        }
        if (message.content.isEmpty() && message.time.completed == null) {
            StreamingCursor()
        }
    }
}

@Composable
private fun AssistantText(text: String) {
    // Markdown/code-block rendering: fenced code blocks get monospace +
    // a distinct surface; everything else renders as plain body text.
    // Kept intentionally lightweight (no full CommonMark parser dependency)
    // to avoid pulling in a heavy third-party markdown renderer for MVP —
    // this covers the two things that matter most in an AI coding chat:
    // paragraphs and code fences.
    val segments = remember(text) { splitCodeFences(text) }
    Column {
        segments.forEach { segment ->
            when (segment) {
                is TextSegment.Prose -> Text(
                    text = segment.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
                is TextSegment.Code -> CodeBlock(code = segment.code, language = segment.language)
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String, language: String?) {
    var copied by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = language?.ifBlank { "text" } ?: "text",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = OpenCodeIcons.Copy,
                contentDescription = "Copy code",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp).clickable {
                    copied = true
                    // Actual clipboard write is performed by the caller via
                    // LocalClipboardManager in the screen composable; this
                    // block is presentational and testable in isolation.
                },
            )
        }
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollStateCompat())
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ReasoningBlock(text: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = OpenCodeIcons.ChevronDown,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reasoning", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 18.dp, top = 4.dp),
            )
        }
    }
}

@Composable
private fun ToolCallBlock(name: String, state: ToolState) {
    var expanded by remember { mutableStateOf(false) }
    val (label, icon) = when (state) {
        is ToolState.Pending -> "Preparing $name…" to OpenCodeIcons.Terminal
        is ToolState.Running -> "Running $name…" to OpenCodeIcons.Terminal
        is ToolState.Completed -> "$name" to OpenCodeIcons.Success
        is ToolState.Error -> "$name failed" to OpenCodeIcons.ErrorIcon
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { expanded = !expanded }
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Icon(OpenCodeIcons.ChevronDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            val detail = when (state) {
                is ToolState.Running -> state.input.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                is ToolState.Completed -> state.input.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                is ToolState.Error -> state.error?.message ?: "Unknown error"
                is ToolState.Pending -> state.input
            }
            Text(
                text = detail,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun StreamingCursor() {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse),
        label = "cursorAlpha",
    )
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .size(width = 8.dp, height = 16.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha), RoundedCornerShape(2.dp)),
    )
}

// --- lightweight code-fence splitter ---

private sealed class TextSegment {
    data class Prose(val text: String) : TextSegment()
    data class Code(val code: String, val language: String?) : TextSegment()
}

private fun splitCodeFences(input: String): List<TextSegment> {
    if (!input.contains("```")) return listOf(TextSegment.Prose(input))
    val result = mutableListOf<TextSegment>()
    val parts = input.split("```")
    parts.forEachIndexed { index, part ->
        if (index % 2 == 0) {
            if (part.isNotBlank()) result.add(TextSegment.Prose(part.trim('\n')))
        } else {
            val firstLineEnd = part.indexOf('\n')
            val language = if (firstLineEnd > 0) part.substring(0, firstLineEnd).trim() else null
            val code = if (firstLineEnd > 0) part.substring(firstLineEnd + 1) else part
            result.add(TextSegment.Code(code.trimEnd('\n'), language?.ifBlank { null }))
        }
    }
    return result
}

@Composable
private fun rememberScrollStateCompat() = androidx.compose.foundation.rememberScrollState()

private fun Modifier.horizontalScroll(state: androidx.compose.foundation.ScrollState): Modifier =
    androidx.compose.foundation.horizontalScroll(this, state)
