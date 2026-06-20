package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.ChatBubble
import com.example.data.NewsArticle
import com.example.ui.theme.*
import com.example.viewmodel.NewsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val viewModel: NewsViewModel = viewModel()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Trigger Android Toasts dynamically
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    var currentTab by remember { mutableStateOf(0) } // 0 = Home, 1 = Mandi, 2 = Buxar Mitra, 3 = Bookmarks

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_bottom_bar"),
                containerColor = Color(0xFFF3F4F9),
                tonalElevation = 0.dp
            ) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyDark,
                    selectedTextColor = NavyDark,
                    indicatorColor = AccentLightBlue,
                    unselectedIconColor = NeutralGreyText,
                    unselectedTextColor = NeutralGreyText
                )
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Newspaper, contentDescription = "मुख्य समाचार") },
                    label = { Text("मुख्य", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp, letterSpacing = (-0.2).sp) },
                    colors = itemColors,
                    modifier = Modifier.testTag("nav_tab_home")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "मंडी भाव") },
                    label = { Text("मंडी भाव", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp, letterSpacing = (-0.2).sp) },
                    colors = itemColors,
                    modifier = Modifier.testTag("nav_tab_mandi")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.ChatBubble, contentDescription = "बक्सर मित्र एआई") },
                    label = { Text("बक्सर मित्र", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp, letterSpacing = (-0.2).sp) },
                    colors = itemColors,
                    modifier = Modifier.testTag("nav_tab_mitra")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "सुरक्षित समाचार") },
                    label = { Text("सुरक्षित", fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp, letterSpacing = (-0.2).sp) },
                    colors = itemColors,
                    modifier = Modifier.testTag("nav_tab_bookmarks")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(viewModel = viewModel)
                1 -> MandiScreen(viewModel = viewModel)
                2 -> MitraChatScreen(viewModel = viewModel)
                3 -> BookmarkedScreen(viewModel = viewModel)
            }

            // Article Detail Dialog Overlay
            ArticleDetailOverlay(viewModel = viewModel)
        }
    }
}

@Composable
fun HomeScreen(viewModel: NewsViewModel) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isGenerating by viewModel.newsGenerationLoading.collectAsStateWithLifecycle()

    val categories = listOf("All", "Breaking", "Events", "Mandi", "Culture", "History")
    val categoryNames = mapOf(
        "All" to "सब समाचार",
        "Breaking" to "ताज़ा ख़बरें",
        "Events" to "स्थानीय घटनाएँ",
        "Mandi" to "मंडी व्यापार",
        "Culture" to "ज्ञान और भक्ति",
        "History" to "इतिहास-विरासत"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_column")
    ) {
        // App Branding Top Block
        item {
            BrandingHeaderBlock()
        }

        // Hero Slide Block
        item {
            HeroBannerSliderBlock()
        }

        // Breaking News Badge banner
        item {
            BreakingBadgeBlock()
        }

        // Search & Refresh Action Panel
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("समाचार खोजें...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "खोजें") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "हटाएं")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_field_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // AI News Generation trigger
                Box(contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { viewModel.refreshAiNews() },
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("ai_refresh_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "एआई समाचार", tint = CorporateBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI ताज़ा", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Category Horizontal Selector
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedCategory.value = cat },
                        label = {
                            Text(
                                text = categoryNames[cat] ?: cat,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_chip_$cat")
                    )
                }
            }
        }

        // Warning or Alert if AI is generating
        if (isGenerating) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "बक्सर मित्र एआई न्यूज़ रूम से ताज़ा स्थानीय समाचार संकलित कर रहा है...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Empty State check
        if (articles.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "कोई समाचार नहीं",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "कोई समाचार नहीं मिला।",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "कृपया कोई दूसरा विषय खोजें या 'AI ताज़ा' बटन दबाएं!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // News Articles List
        items(articles, key = { it.id }) { article ->
            ArticleCardItem(
                article = article,
                onClick = { viewModel.selectedArticle.value = article },
                onBookmarkClick = { viewModel.toggleBookmark(article.id, article.isBookmarked) }
            )
        }
    }
}

@Composable
fun BrandingHeaderBlock() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circle Avatar Badge for Logo / Menu
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = "AAPNA BUXAR",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "अपना बक्सर • सच की आवाज़",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search or notifications imitation action badges
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HeroBannerSliderBlock() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("hero_banner_card"),
        shape = RoundedCornerShape(32.dp), // matched `rounded-[2rem]` exactly
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.buxar_news_banner),
                contentDescription = "Aapna Buxar News Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Deep gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
            )

            // Info overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(20.dp), // 5 CSS is ~20dp
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary, // #005AC1 top headline bg
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "शाहाबाद मुख्य समाचार",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "बक्सर के गंगा घाटों का होगा कायाकल्प, केंद्र सरकार ने दी मंजूरी",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        lineHeight = 22.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BreakingBadgeBlock() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("breaking_badge_container"),
        colors = CardDefaults.cardColors(
            containerColor = AlertRedBg
        ),
        border = BorderStroke(0.5.dp, RedDot.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp) // rounded-2xl
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Little dot representing the active breaking signal
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(RedDot)
            )
            Text(
                text = "BREAKING: रेलवे ट्रैक मरम्मत कार्य के चलते 2 ट्रेनें रद्द",
                color = AlertRedText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun ArticleCardItem(
    article: NewsArticle,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    val categoryLabels = mapOf(
        "Breaking" to "ताज़ा ख़बर",
        "Events" to "स्थानीय खबर",
        "Mandi" to "मंडी अपडेट",
        "Culture" to "आध्यात्म-धर्म",
        "History" to "ऐतिहासिक धरोहर"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("article_card_${article.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // News content thumbnail or default vector representations
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when (article.category) {
                        "Culture" -> Icons.Default.TempleHindu
                        "Events" -> Icons.Default.Celebration
                        "Mandi" -> Icons.Default.Agriculture
                        "History" -> Icons.Default.Castle
                        else -> Icons.Default.Newspaper
                    }
                    val iconTint = when (article.category) {
                        "Culture" -> CorporateBlue
                        "Events" -> Color(0xFFBA1A1A)
                        "Mandi" -> Color(0xFF005AC1)
                        "History" -> Color(0xFF44474E)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = article.category,
                            tint = iconTint,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = categoryLabels[article.category] ?: "ताज़ा",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // News Core Information
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "बक्सर • ${article.date}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (article.isAiGenerated) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = "एआई लिखित",
                                        tint = CorporateBlue,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("AI जनित", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = article.title,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.sp,
                        maxLines = 2,
                        lineHeight = 19.sp,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = article.summary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // News Card Action Footer Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "लेखक: ${article.author}",
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(
                        onClick = { onBookmarkClick() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "सुरक्षित करें",
                            tint = if (article.isBookmarked) RedDot else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, article.title)
                                putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.summary}\n\n - अपना बक्सर न्यूज़ ऐप से शेयर्ड")
                            }
                            context.startActivity(Intent.createChooser(intent, "कहाँ शेयर करें?"))
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "शेयर करें",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MandiScreen(viewModel: NewsViewModel) {
    // Mandi Rates table static setup representing real Buxar Mandi (बक्सर कृषि मंडी) rates
    val rawMandiRates = listOf(
        MandiCommodity("Swarna Paddy (धान स्वर्णा)", "₹2180", "₹2200", true),
        MandiCommodity("Sonachur Paddy (सोनाचूर धान)", "₹2950", "₹3100", true),
        MandiCommodity("Sharbati Wheat (गेहूं शरबती)", "₹2480", "₹2550", true),
        MandiCommodity("Lokwan Wheat (गेहूं लोकवान)", "₹2350", "₹2420", false),
        MandiCommodity("Yellow Mustard (पीली सरसों)", "₹5600", "₹5900", true),
        MandiCommodity("Maize (मक्का शाहाबाद)", "₹1980", "₹2050", false),
        MandiCommodity("White Potato (सफेद आलू)", "₹1400", "₹1550", true),
        MandiCommodity("Red Onion (लाल प्याज़)", "₹1800", "₹2000", false),
        MandiCommodity("Chana (देसी चना)", "₹6200", "₹6400", true)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("mandi_screen_column")
    ) {
        Text(
            text = "बक्सर कृषि उत्पादन मंडी",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "औद्योगिक थाना रोड, बक्सर • ताज़ा भाव सूचकांक (प्रति क्विंटल)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Weather Mini Widget reflecting Ganges-side weather
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("बक्सर आज का मौसम", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("34°C • साफ़ धूप", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("पवित्र गंगा स्नान शुभ काल: 04:15 AM से 06:30 AM", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    Icons.Default.WbSunny,
                    contentDescription = "धूप",
                    tint = CorporateBlue,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Mandi Rates Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("फसल नाम (बक्सर मंडी)", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, modifier = Modifier.weight(1.8f))
                    Text("न्यूनतम", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("अधिकतम", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("रूझान", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                }

                Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                // Table Rows
                rawMandiRates.forEach { commodity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = commodity.name,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1.8f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = commodity.min,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = commodity.max,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier.weight(0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (commodity.isStableOrUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = "Trend",
                                tint = if (commodity.isStableOrUp) Color(0xFF4CAF50) else Color(0xFFE53935),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Mandi Analysis Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "मंडी सलाह", tint = CorporateBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("मंडी एआई समीक्षा (शाहाबाद क्षेत्र)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "धान स्वर्णा और सोनाचूर की माँग बनारस और पटना की मंडियों में बढ़ती आवक के कारण स्थिर है। किसानों को सलाह दी जाती है कि वे अपने कूपन के अनुसार क्रय केंद्र पर सीधे जाएँ, बिचौलियों से बचें। गेहूं की कीमतें अगले सप्ताह ₹30-50 प्रति क्विंटल तक बढ़ने की संभावना है।",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class MandiCommodity(
    val name: String,
    val min: String,
    val max: String,
    val isStableOrUp: Boolean
)

@Composable
fun MitraChatScreen(viewModel: NewsViewModel) {
    val history by viewModel.chatHistory.collectAsStateWithLifecycle()
    val loading by viewModel.chatbotLoading.collectAsStateWithLifecycle()
    var inputQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberScrollState()

    // Smooth auto-scroll chatbot history when message arrives
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            delay(100)
            listState.animateScrollTo(listState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mitra_chat_screen")
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mascot Placeholder Representing Friendly Pandit/Journalist
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CorporateBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = "बक्सर मित्र", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("बक्सर मित्र AI (बक्सर के जानकार)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("ऋषि विश्वामित्र की भूमि से", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }

            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "चैट साफ़ करें", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        // Scrollable Chat Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(listState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            history.forEach { bubble ->
                ChatBubbleRow(bubble = bubble)
            }

            if (loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("मित्र सोच रहा है...", fontSize = 12.sp, fontStyle = FontStyle.Italic)
                        }
                    }
                }
            }
        }

        // Question quick suggestions
        val suggestions = listOf(
            "रामरेखा घाट का इतिहास बताओ",
            "बक्सर का युद्ध कब और क्यों हुआ?",
            "बक्सर में घूमने वाली प्रसिद्ध जगहें",
            "नौलखा मंदिर के बारे में बताओ"
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { keyword ->
                Surface(
                    onClick = {
                        inputQuery = keyword
                        viewModel.askQuestion(keyword)
                        inputQuery = ""
                    },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = keyword,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat Input Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.ime),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("बक्सर मित्र से पूछें...", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputQuery.trim().isNotEmpty()) {
                        viewModel.askQuestion(inputQuery)
                        inputQuery = ""
                        focusManager.clearFocus()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (inputQuery.trim().isNotEmpty()) {
                        viewModel.askQuestion(inputQuery)
                        inputQuery = ""
                        focusManager.clearFocus()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "मैसेज भेजें",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubbleRow(bubble: ChatBubble) {
    val alignment = if (bubble.isUser) Arrangement.End else Arrangement.Start
    val bubbleColor = if (bubble.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (bubble.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val shape = if (bubble.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = bubble.message,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun BookmarkedScreen(viewModel: NewsViewModel) {
    val bookmarkedArticles by viewModel.bookmarkedArticles.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bookmarks_screen")
    ) {
        // Bookmarks Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "सुरक्षित समाचार",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "ऑफ़लाइन पढ़ने के लिए बचाए गए स्थानीय बुलेटिन",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (bookmarkedArticles.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.BookmarkBorder,
                    contentDescription = "रिक्त",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "कोई सुरक्षित समाचार नहीं मिला।",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "समाचार कार्ड पर बने बुकमार्क सिंबल को दबाकर समाचार सुरक्षित करें।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(bookmarkedArticles, key = { it.id }) { article ->
                    ArticleCardItem(
                        article = article,
                        onClick = { viewModel.selectedArticle.value = article },
                        onBookmarkClick = { viewModel.toggleBookmark(article.id, article.isBookmarked) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailOverlay(viewModel: NewsViewModel) {
    val selectedArticle by viewModel.selectedArticle.collectAsStateWithLifecycle()
    val isTranslating by viewModel.translationLoading.collectAsStateWithLifecycle()

    selectedArticle?.let { article ->
        AlertDialog(
            onDismissRequest = { viewModel.selectedArticle.value = null },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("article_detail_dialog"),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("बक्सर बुलेटिन", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { viewModel.selectedArticle.value = null }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "पीछे")
                                }
                            },
                            actions = {
                                IconButton(onClick = { viewModel.toggleBookmark(article.id, article.isBookmarked) }) {
                                    Icon(
                                        imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "सुरक्षित करें",
                                        tint = if (article.isBookmarked) RedDot else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Category Badge and Date representation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = article.category.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "बक्सर • ${article.date}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Article Title
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Author signature
                        Text(
                            text = "विशेष संवाददाता: ${article.author}",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        )

                        Divider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        )

                        // Bhojpuri translation dynamic prompt trigger
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (article.bhojpuriTranslation != null) {
                                    CorporateBlue.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            border = BorderStroke(1.dp, CorporateBlue.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                if (article.bhojpuriTranslation == null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "भोजपुरी में सोचेके बा?",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "एआई बक्सर मित्र से रउआ इ समाचार ठेठ भोजपुरी में सुने-पढ़े खातिर अनुवाद कराईं!",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                        Button(
                                            onClick = { viewModel.translateToBhojpuri(article) },
                                            enabled = !isTranslating,
                                            colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            if (isTranslating) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Translate, contentDescription = "भोजपुरी", modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("भोजपुरी", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = "भोजपुरी अनुवादित", tint = CorporateBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ठेठ भोजपुरी संस्करण (बक्सर मित्र अनुवाद)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CorporateBlue
                                        )
                                    }

                                    Text(
                                        text = article.bhojpuriTranslation!!,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Core news body text representation
                        Text(
                            text = article.content,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
