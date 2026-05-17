package xyz.elietio.routineplus.isworkday.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.elietio.routineplus.isworkday.ui.theme.terminalBg
import xyz.elietio.routineplus.isworkday.ui.theme.terminalGreen
import xyz.elietio.routineplus.isworkday.ui.theme.terminalRed
import xyz.elietio.routineplus.isworkday.ui.theme.terminalYellow

data class TerminalLine(
    val tag: String,
    val message: String,
    val level: TerminalLevel = TerminalLevel.INFO
)

enum class TerminalLevel { INFO, SUCCESS, WARNING, ERROR }

@Composable
fun SandboxTerminal(
    lines: List<TerminalLine>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 400.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(terminalBg)
            .padding(12.dp)
            .verticalScroll(scrollState)
    ) {
        lines.forEach { line ->
            Text(
                text = formatLine(line),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }
    }
}

private fun formatLine(line: TerminalLine): AnnotatedString {
    val color = when (line.level) {
        TerminalLevel.INFO -> terminalGreen.copy(alpha = 0.7f)
        TerminalLevel.SUCCESS -> terminalGreen
        TerminalLevel.WARNING -> terminalYellow
        TerminalLevel.ERROR -> terminalRed
    }

    return buildAnnotatedString {
        withStyle(SpanStyle(color = color.copy(alpha = 0.6f))) {
            append("> [${line.tag}] ")
        }
        withStyle(SpanStyle(color = color)) {
            append(line.message)
        }
    }
}
