package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = NewsRepository(db.newsArticleDao())

    // UI States and filters
    val selectedCategory = MutableStateFlow("All")
    val searchQuery = MutableStateFlow("")

    val articles: StateFlow<List<NewsArticle>> = combine(
        repository.allArticles,
        selectedCategory,
        searchQuery
    ) { list, category, query ->
        var result = list
        if (category != "All") {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }
        if (query.isNotEmpty()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.summary.contains(query, ignoreCase = true)
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedArticles: StateFlow<List<NewsArticle>> = repository.bookmarkedArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chatbot States
    private val _chatHistory = MutableStateFlow<List<ChatBubble>>(listOf(
        ChatBubble(
            message = "प्रणाम! हम बक्सर मित्र बानी। रउवा बक्सर जिला (बक्सर, डुमरांव, चौसा, ब्रह्मपुर) के इतिहास, पर्यटन स्थल भा कवनो ताज़ा जानकारी चाहे समाचार के संक्षेप समझे के बा त रउआ हमसे पूछ सकत बानी। का सहायता करीं?",
            isUser = false
        )
    ))
    val chatHistory: StateFlow<List<ChatBubble>> = _chatHistory.asStateFlow()

    val chatbotLoading = MutableStateFlow(false)
    val newsGenerationLoading = MutableStateFlow(false)
    val translationLoading = MutableStateFlow(false)
    val toastMessage = MutableStateFlow<String?>(null)

    // Details article state
    val selectedArticle = MutableStateFlow<NewsArticle?>(null)

    init {
        viewModelScope.launch {
            // Check if db is empty, if so, populate with seed articles
            repository.allArticles.first().let {
                if (it.isEmpty()) {
                    repository.insertDefaultSeedArticles()
                }
            }
        }
    }

    // Toggle Bookmarking
    fun toggleBookmark(articleId: String, isCurrentlyBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(articleId, !isCurrentlyBookmarked)
            // Update selected article to reflect change
            selectedArticle.value?.let { current ->
                if (current.id == articleId) {
                    selectedArticle.value = current.copy(isBookmarked = !isCurrentlyBookmarked)
                }
            }
        }
    }

    // Refresh News leveraging Gemini AI
    fun refreshAiNews() {
        if (newsGenerationLoading.value) return
        newsGenerationLoading.value = true
        viewModelScope.launch {
            val key = getApiKey()
            val result = repository.generateAiNews(key)
            result.onSuccess {
                toastMessage.value = "एआई द्वारा बक्सर समाचार सफलतापूर्वक जनरेट कवल गईल!"
            }.onFailure { err ->
                toastMessage.value = "मैसेज: ${err.message ?: "अज्ञात त्रुटि"}. डिफ़ॉल्ट समाचार लोड बा।"
            }
            newsGenerationLoading.value = false
        }
    }

    // Translate open article to Bhojpuri
    fun translateToBhojpuri(article: NewsArticle) {
        if (translationLoading.value) return
        if (article.bhojpuriTranslation != null) {
            // Already translated and cached!
            return
        }
        translationLoading.value = true
        viewModelScope.launch {
            val key = getApiKey()
            val result = repository.translateArticleToBhojpuri(key, article)
            result.onSuccess { translation ->
                selectedArticle.value = article.copy(bhojpuriTranslation = translation)
                toastMessage.value = "भोजपुरी में अनुवाद कयल गईल!"
            }.onFailure { err ->
                toastMessage.value = "अनुवाद विफल: ${err.message}"
            }
            translationLoading.value = false
        }
    }

    // Chatbot Answer Question
    fun askQuestion(question: String) {
        if (question.trim().isEmpty() || chatbotLoading.value) return
        
        // Add user question to history
        val userBubble = ChatBubble(message = question, isUser = true)
        _chatHistory.value = _chatHistory.value + userBubble
        
        chatbotLoading.value = true
        viewModelScope.launch {
            val key = getApiKey()
            val result = repository.askBuxarMitra(key, _chatHistory.value.dropLast(1), question)
            result.onSuccess { reply ->
                _chatHistory.value = _chatHistory.value + ChatBubble(message = reply, isUser = false)
            }.onFailure { err ->
                _chatHistory.value = _chatHistory.value + ChatBubble(
                    message = "माफ़ करीं, नेटवर्क में दिक्कत बा: ${err.message}. की-कॉन्फ़िगरेशन (API Key) जाँच करीं।",
                    isUser = false
                )
            }
            chatbotLoading.value = false
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatBubble(
                message = "प्रणाम! हम बक्सर मित्र बानी। रउवा बक्सर जिला (बक्सर, डुमरांव, चौसा, ब्रह्मपुर) के इतिहास, पर्यटन स्थल भा कवनो ताज़ा जानकारी चाहे समाचार के संक्षेप समझे के बा त रउआ हमसे पूछ सकत बानी। का सहायता करीं?",
                isUser = false
            )
        )
    }

    fun clearToast() {
        toastMessage.value = null
    }

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }
}
