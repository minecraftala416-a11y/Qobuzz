package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.MusicViewModel
import com.example.ui.theme.AppleCardDark
import com.example.ui.theme.AppleMusicHiResGold
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun BrowseScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val genreGradients = listOf(
        listOf(Color(0xFFE52D27), Color(0xFFB31217)),
        listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
        listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
        listOf(Color(0xFFFF8008), Color(0xFFFFC837)),
        listOf(Color(0xFF2C3E50), Color(0xFF3498DB)),
        listOf(Color(0xFFFC5C7D), Color(0xFF6A82FB)),
        listOf(Color(0xFF434343), Color(0xFF000000)),
        listOf(Color(0xFFF12711), Color(0xFFF5AF19)),
        listOf(Color(0xFF009FFF), Color(0xFFEC2F4B))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("browse_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "EXPLORAR",
                    color = AppleMusicRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Explorar",
                    color = AppleTextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Lossless Audio Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FFD700)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = AppleMusicHiResGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audio Sin Pérdida y Estudio Master",
                            color = AppleTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Disfruta de canciones completas en calidad de estudio 320 kbps y 24-bit sin cortes ni fragmentos.",
                            color = AppleTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Categories Grid
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Explorar por Género Musical", subtitle = "Selecciona para ver los éxitos más sonados")
        }

        val chunkedGenres = viewModel.genres.chunked(2)
        items(chunkedGenres.size) { rowIndex ->
            val pair = chunkedGenres[rowIndex]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEachIndexed { colIndex, genre ->
                    val gradientIndex = (rowIndex * 2 + colIndex) % genreGradients.size
                    val gradient = genreGradients[gradientIndex]

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(gradient))
                            .clickable {
                                viewModel.onSearchQueryChanged(genre)
                                viewModel.selectTab(com.example.ui.AppNavigationTab.SEARCH)
                            }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = genre,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                        )
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Featured Albums Carousel
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Álbumes Imprescindibles", subtitle = "Producciones maestras de América Latina")
        }

        items(viewModel.featuredAlbums) { album ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.playAlbum(album) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    AsyncImage(
                        model = album.coverUrl,
                        contentDescription = album.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.title,
                        color = AppleTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${album.artist} • ${album.releaseYear} • ${album.genre}",
                        color = AppleTextSecondary,
                        fontSize = 12.sp
                    )
                }
                if (album.isHiRes) {
                    Text(
                        text = "HI-RES",
                        color = AppleMusicHiResGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
