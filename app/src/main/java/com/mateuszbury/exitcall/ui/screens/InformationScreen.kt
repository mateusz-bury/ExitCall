package com.mateuszbury.exitcall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateuszbury.exitcall.R
import com.mateuszbury.exitcall.ui.components.ExitCallTopBar
import kotlinx.coroutines.launch

private const val DEVELOPER_PROFILE_URL = "https://github.com/mateusz-bury"

@Composable
fun InformationScreen(
    appVersionName: String,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val linkFailureMessage = stringResource(R.string.info_link_failure)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExitCallTopBar(
                title = stringResource(R.string.info_title),
                onBack = onBack
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 8.dp,
                end = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.info_intro),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            item {
                InformationSection(title = stringResource(R.string.info_privacy_title)) {
                    Text(
                        text = stringResource(R.string.info_privacy_body),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InformationBullet(stringResource(R.string.info_privacy_local))
                    InformationBullet(stringResource(R.string.info_privacy_network))
                    InformationBullet(stringResource(R.string.info_privacy_contacts))
                    InformationBullet(stringResource(R.string.info_privacy_permissions))
                }
            }

            item {
                InformationSection(title = stringResource(R.string.info_safe_use_title)) {
                    Text(
                        text = stringResource(R.string.info_safe_use_body),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InformationBullet(stringResource(R.string.info_safe_use_marker))
                    InformationBullet(stringResource(R.string.info_safe_use_delivery))
                    InformationBullet(stringResource(R.string.info_safe_use_emergency))
                }
            }

            item {
                InformationSection(title = stringResource(R.string.info_support_title)) {
                    Text(
                        text = stringResource(R.string.info_support_body),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            runCatching {
                                uriHandler.openUri(DEVELOPER_PROFILE_URL)
                            }.onFailure {
                                scope.launch {
                                    snackbarHostState.showSnackbar(linkFailureMessage)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.info_support_action))
                    }
                    Text(
                        text = stringResource(R.string.info_support_fallback),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            item {
                InformationSection(title = stringResource(R.string.info_about_title)) {
                    Text(
                        text = stringResource(R.string.info_about_identity),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Text(
                        text = stringResource(R.string.info_about_author),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.info_about_version,
                            appVersionName
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InformationSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(18.dp),
            content = { content() }
        )
    }
}

@Composable
private fun InformationBullet(text: String) {
    Text(
        text = "• $text",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
