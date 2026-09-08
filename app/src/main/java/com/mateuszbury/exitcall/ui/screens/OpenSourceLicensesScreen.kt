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
import androidx.compose.foundation.lazy.items
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

private const val APACHE_LICENSE_URL =
    "https://www.apache.org/licenses/LICENSE-2.0"

private data class LicenseComponent(
    val name: String,
    val version: String,
    val copyright: String
)

@Composable
fun OpenSourceLicensesScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val linkFailureMessage = stringResource(R.string.licenses_link_failure)
    val androidCopyright = stringResource(R.string.licenses_android_copyright)
    val kotlinCopyright = stringResource(R.string.licenses_kotlin_copyright)
    val components = listOf(
        LicenseComponent(
            stringResource(R.string.licenses_kotlin),
            "2.2.10",
            kotlinCopyright
        ),
        LicenseComponent(
            stringResource(R.string.licenses_core_ktx),
            "1.10.1",
            androidCopyright
        ),
        LicenseComponent(
            stringResource(R.string.licenses_lifecycle),
            "2.6.1",
            androidCopyright
        ),
        LicenseComponent(
            stringResource(R.string.licenses_activity_compose),
            "1.8.0",
            androidCopyright
        ),
        LicenseComponent(
            stringResource(R.string.licenses_compose_ui),
            stringResource(R.string.licenses_compose_bom_version),
            androidCopyright
        ),
        LicenseComponent(
            stringResource(R.string.licenses_compose_graphics),
            stringResource(R.string.licenses_compose_bom_version),
            androidCopyright
        ),
        LicenseComponent(
            stringResource(R.string.licenses_compose_preview),
            stringResource(R.string.licenses_compose_bom_version),
            androidCopyright
        ),
        LicenseComponent(
            stringResource(R.string.licenses_material3),
            stringResource(R.string.licenses_compose_bom_version),
            androidCopyright
        ),
        LicenseComponent(
            stringResource(R.string.licenses_splashscreen),
            "1.2.0",
            androidCopyright
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExitCallTopBar(
                title = stringResource(R.string.licenses_title),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.licenses_intro),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            items(components) { component ->
                LicenseCard(component)
            }

            item {
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
                        .padding(18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.licenses_apache_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Text(
                        text = stringResource(R.string.licenses_apache_summary),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            runCatching {
                                uriHandler.openUri(APACHE_LICENSE_URL)
                            }.onFailure {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        linkFailureMessage
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.licenses_view_text))
                    }
                    Text(
                        text = stringResource(R.string.licenses_url_fallback),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.licenses_scope_note),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun LicenseCard(component: LicenseComponent) {
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
            .padding(18.dp)
    ) {
        Text(
            text = component.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
        Text(
            text = stringResource(
                R.string.licenses_version,
                component.version
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = component.copyright,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = stringResource(R.string.licenses_apache_name),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
