package feature.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.LocalScreen
import app.presentation.CustomTopBar
import app.presentation.segmentedbutton.SegmentedButton
import app.presentation.segmentedbutton.SegmentedButtonDefaults
import app.presentation.segmentedbutton.SegmentedButtonItem
import app.theme.colorHomeDarkGreen
import app.theme.colorHomeLightGreen
import app.util.isNotNullOrEmpty
import com.russhwolf.settings.ExperimentalSettingsApi
import feature.common.presentation.Intent
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import platform.crashlytics.TYPE_BUG_REPORT
import platform.crashlytics.TYPE_FEATURE_REQUEST
import platform.crashlytics.TYPE_GENERAL_QUERY
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.feedback
import sumo.shared.generated.resources.major

// TODO - Extract strings

@OptIn(ExperimentalResourceApi::class)
@ExperimentalSettingsApi
@ExperimentalMaterial3Api
@Composable
fun UserFeedbackScreen(
    showInterstitialAd: () -> Unit,
) {
    LaunchedEffect(Unit) {
        showInterstitialAd()
    }
    val viewModel: UserFeedbackViewModel = koinInject()

    val font = FontFamily(Font(Res.font.major))
    Box(
        modifier = Modifier
            .background(colorHomeDarkGreen)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomTopBar(
                title = stringResource(resource = Res.string.feedback),
                titleColor = colorHomeLightGreen,
                font = font,
                backIconColor = colorHomeLightGreen
            )
            UserFeedbackContent(
                onIntent = viewModel::onIntent,
                modifier = Modifier
            )
        }
    }
}

@ExperimentalResourceApi
@ExperimentalMaterial3Api
@Composable
fun UserFeedbackContent(
    onIntent: (Intent) -> Unit,
    modifier: Modifier,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var feedbackText by remember { mutableStateOf(TextFieldValue()) }
    var feedbackType by remember { mutableStateOf(TYPE_GENERAL_QUERY) }
    var isFocused by remember { mutableStateOf(false) }
    var feedbackSent by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        val textAreaHeight = maxHeight * 0.2f
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            val characterLimit = 1000
            val font = FontFamily(Font(Res.font.major))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${feedbackText.text.length}/$characterLimit",
                    fontSize = 18.sp,
                    color = colorHomeLightGreen,
                )
            }
            TextField(
                value = feedbackText,
                enabled = !feedbackSent,
                onValueChange = {
                    feedbackText = if (feedbackText.text.length <= characterLimit) {
                        it
                    } else {
                        TextFieldValue(it.text.take(characterLimit))
                    }
                },
                label = {
                    if (!isFocused && feedbackText.text.isEmpty()) {
                        Text(
                            text = "Add your feedback here...",
                            color = colorHomeLightGreen,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(textAreaHeight)
                    .border(
                        width = 6.dp,
                        color = colorHomeLightGreen,
                        shape = TextFieldDefaults.shape,
                    )
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        isFocused = false
                        focusManager.clearFocus()
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorHomeDarkGreen,
                    unfocusedContainerColor = colorHomeDarkGreen,
                    focusedIndicatorColor = colorHomeLightGreen,
                    unfocusedIndicatorColor = colorHomeLightGreen,
                    cursorColor = colorHomeLightGreen,
                    unfocusedTextColor = colorHomeLightGreen,
                    focusedTextColor = colorHomeLightGreen,
                    disabledContainerColor = colorHomeDarkGreen,
                ),
                textStyle = TextStyle(
                    fontSize = 20.sp,
                ),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Feedback Sent!",
                    fontSize = 22.sp,
                    color = colorHomeLightGreen,
                    modifier = Modifier
                        .alpha(if (feedbackSent) 1f else 0f)
                        .padding(vertical = 8.dp),

                )
            }
            FeedbackTypeChooser(
                font = font,
                onFeedbackTypeSelected = {
                    feedbackType = it
                },
                enabled = !feedbackSent
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    enabled = feedbackText.text.isNotNullOrEmpty() && !feedbackSent,
                    onClick = {
                        onIntent(
                            UserFeedbackIntent.SubmitFeedback(
                                feedback = feedbackText.text,
                                type = feedbackType
                            )
                        )
                        isFocused = false
                        focusManager.clearFocus()
                        feedbackSent = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colorHomeDarkGreen,
                        contentColor = colorHomeLightGreen
                    ),
                    border = BorderStroke(2.dp, colorHomeLightGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = if (feedbackSent) "Sent!" else "Send",
                        fontSize = 48.sp,
                        fontFamily = font,
                    )
                }
            }
            Spacer(modifier = Modifier.imePadding())
        }
    }
}

@ExperimentalResourceApi
@Composable
fun FeedbackTypeChooser(
    font: FontFamily,
    enabled: Boolean = true,
    onFeedbackTypeSelected: (String) -> Unit,
) {
    val screenWidth = LocalScreen.current.width
    val fontSize = if (screenWidth <= 360.dp) 16.sp else 18.sp
    val itemList = remember {
        listOf(
            SegmentedButtonItem(
                title = {
                    Text(
                        text = "General Query",
                        fontFamily = font,
                        fontSize = fontSize,
                        lineHeight = fontSize,
                        textAlign = TextAlign.Center
                    )
                },
                onClick = {
                    onFeedbackTypeSelected(TYPE_GENERAL_QUERY)
                }
            ),
            SegmentedButtonItem(
                title = {
                    Text(
                        text = "Suggest Feature",
                        fontFamily = font,
                        fontSize = fontSize,
                        lineHeight = fontSize,
                        textAlign = TextAlign.Center
                    )
                },
                onClick = {
                    onFeedbackTypeSelected(TYPE_FEATURE_REQUEST)
                }
            ),
            SegmentedButtonItem(
                title = {
                    Text(
                        text = "Report A Bug",
                        fontFamily = font,
                        fontSize = fontSize,
                        lineHeight = fontSize,
                        textAlign = TextAlign.Center
                    )
                }, onClick = {
                    onFeedbackTypeSelected(TYPE_BUG_REPORT)
                }
            ),
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            defaultSelectedItemIndex = 0,
            items = itemList,
            color = SegmentedButtonDefaults.segmentedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                outlineColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                selectedContentColor = MaterialTheme.colorScheme.primary,
            ),
            enabled = enabled,
        )
    }
}
