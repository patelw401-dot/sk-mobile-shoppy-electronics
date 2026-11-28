package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.LightBlueBg
import com.example.ui.theme.LightBlueText
import com.example.ui.theme.LightCardBg
import com.example.ui.theme.LightCardSubtleBg
import com.example.ui.theme.LightOutline
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.ShopPrimaryBlue
import com.example.update.AppUpdateInfo

@Composable
fun UpdateAvailableDialog(
    updateInfo: AppUpdateInfo,
    onUpdateNow: () -> Unit,
    onLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onLater,
        containerColor = LightCardBg,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = LightBlueBg,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = "App Update Available",
                    tint = LightBlueText,
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        title = {
            Text(
                text = "App Update Available (v${updateInfo.versionName})",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LightTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = LightCardSubtleBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, LightOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Installed Version",
                                fontSize = 11.sp,
                                color = LightTextSecondary
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Latest Release",
                                fontSize = 11.sp,
                                color = LightTextSecondary
                            )
                            Text(
                                text = "v${updateInfo.versionName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LightBlueText
                            )
                        }
                    }
                }

                Text(
                    text = updateInfo.releaseNotes,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = LightTextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onUpdateNow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShopPrimaryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("update_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download Update",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Update Now",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onLater,
                border = BorderStroke(1.dp, LightOutline),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("update_later_button")
            ) {
                Text(
                    text = "Later",
                    fontWeight = FontWeight.SemiBold,
                    color = LightTextSecondary
                )
            }
        }
    )
}
