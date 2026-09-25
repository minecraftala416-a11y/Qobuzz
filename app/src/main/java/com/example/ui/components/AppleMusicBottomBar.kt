package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Grid3x3
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavigationTab
import com.example.ui.theme.AppleBorderDark
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.AppleSurfaceDark
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun AppleMusicBottomBar(
    selectedTab: AppNavigationTab,
    onTabSelected: (AppNavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(width = 0.5.dp, color = AppleBorderDark)
            .testTag("apple_music_bottom_bar"),
        containerColor = AppleSurfaceDark.copy(alpha = 0.96f),
        contentColor = AppleTextPrimary,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Listen Now
        NavigationBarItem(
            selected = selectedTab == AppNavigationTab.LISTEN_NOW,
            onClick = { onTabSelected(AppNavigationTab.LISTEN_NOW) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppNavigationTab.LISTEN_NOW) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircleOutline,
                    contentDescription = "Listen Now"
                )
            },
            label = {
                Text(
                    text = "Listen Now",
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == AppNavigationTab.LISTEN_NOW) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppleMusicRed,
                selectedTextColor = AppleMusicRed,
                unselectedIconColor = AppleTextSecondary,
                unselectedTextColor = AppleTextSecondary,
                indicatorColor = Color.Transparent
            ),
            modifier = Modifier.testTag("tab_listen_now")
        )

        // Tab 2: Browse
        NavigationBarItem(
            selected = selectedTab == AppNavigationTab.BROWSE,
            onClick = { onTabSelected(AppNavigationTab.BROWSE) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppNavigationTab.BROWSE) Icons.Filled.Grid3x3 else Icons.Outlined.Grid3x3,
                    contentDescription = "Browse"
                )
            },
            label = {
                Text(
                    text = "Browse",
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == AppNavigationTab.BROWSE) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppleMusicRed,
                selectedTextColor = AppleMusicRed,
                unselectedIconColor = AppleTextSecondary,
                unselectedTextColor = AppleTextSecondary,
                indicatorColor = Color.Transparent
            ),
            modifier = Modifier.testTag("tab_browse")
        )

        // Tab 3: Radio
        NavigationBarItem(
            selected = selectedTab == AppNavigationTab.RADIO,
            onClick = { onTabSelected(AppNavigationTab.RADIO) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppNavigationTab.RADIO) Icons.Filled.Radio else Icons.Outlined.Radio,
                    contentDescription = "Radio"
                )
            },
            label = {
                Text(
                    text = "Radio",
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == AppNavigationTab.RADIO) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppleMusicRed,
                selectedTextColor = AppleMusicRed,
                unselectedIconColor = AppleTextSecondary,
                unselectedTextColor = AppleTextSecondary,
                indicatorColor = Color.Transparent
            ),
            modifier = Modifier.testTag("tab_radio")
        )

        // Tab 4: Library
        NavigationBarItem(
            selected = selectedTab == AppNavigationTab.LIBRARY,
            onClick = { onTabSelected(AppNavigationTab.LIBRARY) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppNavigationTab.LIBRARY) Icons.Filled.FolderSpecial else Icons.Outlined.FolderSpecial,
                    contentDescription = "Library"
                )
            },
            label = {
                Text(
                    text = "Library",
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == AppNavigationTab.LIBRARY) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppleMusicRed,
                selectedTextColor = AppleMusicRed,
                unselectedIconColor = AppleTextSecondary,
                unselectedTextColor = AppleTextSecondary,
                indicatorColor = Color.Transparent
            ),
            modifier = Modifier.testTag("tab_library")
        )

        // Tab 5: Search
        NavigationBarItem(
            selected = selectedTab == AppNavigationTab.SEARCH,
            onClick = { onTabSelected(AppNavigationTab.SEARCH) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppNavigationTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            label = {
                Text(
                    text = "Search",
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == AppNavigationTab.SEARCH) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppleMusicRed,
                selectedTextColor = AppleMusicRed,
                unselectedIconColor = AppleTextSecondary,
                unselectedTextColor = AppleTextSecondary,
                indicatorColor = Color.Transparent
            ),
            modifier = Modifier.testTag("tab_search")
        )
    }
}
