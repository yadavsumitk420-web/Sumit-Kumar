package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class NewsRepository(private val articleDao: NewsArticleDao) {

    val allArticles: Flow<List<NewsArticle>> = articleDao.getAllArticles()
    val bookmarkedArticles: Flow<List<NewsArticle>> = articleDao.getBookmarkedArticles()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    suspend fun getArticlesByCategory(category: String): Flow<List<NewsArticle>> {
        return articleDao.getArticlesByCategory(category)
    }

    suspend fun toggleBookmark(articleId: String, isBookmarked: Boolean) {
        articleDao.updateBookmarkStatus(articleId, isBookmarked)
    }

    suspend fun updateArticle(article: NewsArticle) {
        articleDao.updateArticle(article)
    }

    suspend fun insertDefaultSeedArticles() {
        val seed = listOf(
            NewsArticle(
                id = "seed_1",
                title = "रामरेखा घाट पर गंगा दशहरा महा आरती का भव्य आयोजन, उमड़ी श्रद्धालुओं की भारी भीड़",
                content = "बक्सर के ऐतिहासिक रामरेखा घाट पर गंगा दशहरा के पावन अवसर पर भव्य 'महा गंगा आरती' का आयोजन किया गया। बनारस के विद्वान पंडितों द्वारा शंखनाद और वैदिक मंत्रोच्चार के बीच गंगा मैया की आरती उतारी गई। इस वर्ष घाट को विशेष रंग-बिरंगी लाइटों और फूलों से सजाया गया था। शाम ढलते ही पूरा घाट दीयों की रोशनी से जगमगा उठा। जिला प्रशासन के अनुसार, आरती में शामिल होने के लिए लगभग 50,000 से अधिक श्रद्धालु पहुंचे। सुरक्षा के कड़े इंतजाम किए गए थे और जल पुलिस के साथ एसडीआरएफ की टीमें भी मुस्तैद रहीं। स्थानीय न्यास समिति ने श्रद्धालुओं के लिए विशेष प्रसाद और पेयजल की व्यवस्था की थी।",
                summary = "गंगा दशहरा पर ऐतिहासिक रामरेखा घाट पर 50 हजार भक्तों की उपस्थिति में बनारस के तर्ज पर भव्य गंगा आरती की गई।",
                category = "Culture",
                date = "2026-06-19",
                author = "बक्सर ब्यूरो",
                imageUrl = "art_ramrekha"
            ),
            NewsArticle(
                id = "seed_2",
                title = "किला मैदान में तीन दिवसीय जिलास्तरीय युवा खेल महोत्सव का शुभारंभ, 15 खेलों में दिखेगा दमखम",
                content = "बक्सर के ऐतिहासिक किला मैदान में आज खेल विभाग द्वारा आयोजित तीन दिवसीय 'जिलास्तरीय युवा खेल महोत्सव' का उद्घाटन जिलाधिकारी द्वारा दीप प्रज्वलित कर किया गया। इस महोत्सव में पूरे जिले के विभिन्न प्रखंडों से आए लगभग 1200 खिलाड़ी हिस्सा ले रहे हैं। कड़ाके की धूप के बावजूद युवाओं में भारी उत्साह देखा गया। फुटबॉल, कबड्डी, एथलेटिक्स, खो-खो, और कुश्ती सहित कुल 15 मुख्य स्पर्धाओं का आयोजन किया जा रहा है। उद्घाटन मैच ब्रह्मपुर और बक्सर सदर के बीच फुटबॉल का खेला गया, जिसमें बक्सर सदर की टीम ने 2-1 से जीत हासिल की। विजेता खिलाड़ियों को राज्यस्तरीय प्रतियोगिता के लिए चुना जाएगा।",
                summary = "किला मैदान में जिला खेल महोत्सव शुरू हुआ। 1200 से अधिक खिलाड़ी अपनी प्रतिभा का प्रदर्शन कर रहे हैं।",
                category = "Events",
                date = "2026-06-18",
                author = "संदीप ओझा",
                imageUrl = "art_sports"
            ),
            NewsArticle(
                id = "seed_3",
                title = "बक्सर कृषि मंडी में नया धान और गेहूं की आवक तेज, कीमतों में उछाल से किसान उत्साहित",
                content = "बक्सर की मुख्य औद्योगिक क्षेत्र रोड स्थित कृषि मंडी (मंडी यार्ड) में इस सप्ताह गेहूं और महीन धान की आवक में जबरदस्त वृद्धि हुई है। बिचौलियों की सक्रियता कम होने और सरकारी क्रय केंद्रों पर सुचारू खरीद के कारण किसानों को इस बार काफी अच्छे दाम मिल रहे हैं। मंडी व्यापारियों के अनुसार, महीन सोनाचूर धान ₹2800 से ₹3100 प्रति क्विंटल तक बिक रहा है, जबकि गेहूं का दाम ₹2450 से ₹2600 प्रति क्विंटल के बीच बना हुआ है। किसानों का कहना है कि समय पर मानसून पूर्व तैयारियों और सिंचाई की उत्तम व्यवस्था के कारण इस बार पैदावार अच्छी हुई है, जिससे उन्हें अधिक लाभ हो रहा है।",
                summary = "बक्सर अनाज मंडी में कीमतों में आई तेजी से किसानों के चेहरों पर खुशी। सोनाचूर धान ₹3100 प्रति क्विंटल तक बिका।",
                category = "Mandi",
                date = "2026-06-17",
                author = "मंडी प्रतिनिधि",
                imageUrl = "art_mandi"
            ),
            NewsArticle(
                id = "seed_4",
                title = "ऐतिहासिक चौसा के युद्ध स्थल को राष्ट्रीय पर्यटन मानचित्र पर चमकाने की तैयारी शुरू",
                content = "सन 1539 में शेरशाह सूरी और मुगल सम्राट हुमायूं के बीच हुए ऐतिहासिक चौसा के युद्ध स्थल के जीर्णोद्धार का काम पर्यटन विभाग द्वारा तेजी से शुरू कर दिया गया है। पहले चरण में युद्ध स्थल परिसर की घेराबंदी, सुंदर पार्क का निर्माण, लाइट एंड साउंड शो की व्यवस्था और इतिहास को दर्शाने वाली कलाकृतियों की स्थापना की जाएगी। स्थानीय विधायक और जिला पर्यटन पदाधिकारी ने संयुक्त रूप से स्थल का निरीक्षण किया। उन्होंने कहा कि चौसा युद्ध का भारतीय इतिहास में बहुत बड़ा महत्व है। इस स्थल का सुंदरीकरण होने से देश-विदेश से पर्यटक यहाँ आ सकेंगे जिससे स्थानीय युवाओं को रोजगार का नया अवसर मिलेगा।",
                summary = "शेरशाह सूरी और हुमायूं के बीच प्रसिद्ध युद्ध की भूमि चौसा का जीर्णोद्धार शुरू। लाइट एंड साउंड शो की होगी व्यवस्था।",
                category = "History",
                date = "2026-06-16",
                author = "इतिहास विभाग",
                imageUrl = "art_chausa"
            ),
            NewsArticle(
                id = "seed_5",
                title = "नौलखा मंदिर में नौ दिवसीय शतचंडी महायज्ञ एवं भव्य प्रवचन का आगाज़",
                content = "बक्सर स्टेशन रोड के निकट प्रसिद्ध नौलखा मंदिर में नौ दिवसीय शतचंडी महायज्ञ और राष्ट्र कल्याण हेतु अखंड संकीर्तन का आयोजन आज कलश यात्रा के साथ शुरू हुआ। सुबह पवित्र गंगा जल लेकर 551 महिलाओं ने भव्य कलश यात्रा निकाली जो शहर के मुख्य मार्गों से होते हुए वापस मंदिर परिसर पहुंची। यज्ञ के मुख्य यजमान पूज्य स्वामी जी ने बताया कि यह आयोजन संपूर्ण शाहाबाद क्षेत्र की खुशहाली, शांति और संवर्धन के लिए किया जा रहा है। प्रतिदिन संध्या काल में बनारस और अयोध्या से पधारे मर्मज्ञ संतों द्वारा श्रीराम कथा एवं श्रीमद्भागवत कथा का अमृत प्रवचन किया जाएगा।",
                summary = "प्रसिद्ध नौलखा मंदिर में कलश यात्रा के साथ शतचंडी महायज्ञ प्रारंभ। देश के प्रतिष्ठित संत कर रहे हैं कथा वाचन।",
                category = "Culture",
                date = "2026-06-15",
                author = "श्रद्धांजलि रिपोर्टर",
                imageUrl = "art_naulakha"
            )
        )
        articleDao.insertArticles(seed)
    }

    suspend fun generateAiNews(apiKey: String): Result<List<NewsArticle>> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key not configured in system secrets."))
        }

        val prompt = """
            Generate exactly 4 realistic local news articles for Buxar district (including Buxar town, Dumraon, Chausa, Brahmpur, etc.) in Bihar, India.
            The articles must be in Hindi. Make them sound highly realistic, journalistic, positive, and deeply local (mentioning real Buxar geographical aspects, local challenges, progress, temples, or river Saryu/Ganges etc.).
            
            Each article MUST fit one of these categories: "Breaking", "Events", "Mandi", "Culture", "History". Choose distinct categories.
            
            You must return the articles precisely in a JSON format matching this array structure:
            [
              {
                "title": "Clean, compelling news title in Hindi",
                "content": "A detailed 4-5 sentence news content in Hindi",
                "summary": "1 sentence brief summary in Hindi",
                "category": "One of: Breaking, Events, Mandi, Culture, History",
                "author": "Journalist's name in Hindi (or 'बक्सर संवाददाता')"
              }
            ]
            
            Ensure the fields are properly encoded. Do not include any markdown backticks, explanations, or extra characters. Just the JSON array.
        """.trimIndent()

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7,
                    responseMimeType = "application/json"
                )
            )

            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Empty response from AI engine"))

            Log.d("NewsRepository", "Raw Gemini Output: $jsonText")

            val type = Types.newParameterizedType(List::class.java, GeneratedArticle::class.java)
            val adapter = moshi.adapter<List<GeneratedArticle>>(type)
            val generatedList = adapter.fromJson(jsonText)
                ?: return@withContext Result.failure(Exception("JSON parsing of articles failed"))

            val sdf = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault())
            val todayStr = sdf.format(Date())

            val formattedArticles = generatedList.mapIndexed { index, gen ->
                NewsArticle(
                    id = "ai_" + UUID.randomUUID().toString().take(6) + "_" + index,
                    title = gen.title,
                    content = gen.content,
                    summary = gen.summary,
                    category = gen.category,
                    date = todayStr,
                    author = gen.author,
                    imageUrl = when (gen.category) {
                        "Culture" -> "art_ramrekha"
                        "Events" -> "art_sports"
                        "Mandi" -> "art_mandi"
                        "History" -> "art_chausa"
                        else -> "art_news"
                    },
                    isBookmarked = false,
                    isAiGenerated = true
                )
            }

            articleDao.insertArticles(formattedArticles)
            return@withContext Result.success(formattedArticles)
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error generating articles", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun translateArticleToBhojpuri(apiKey: String, article: NewsArticle): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API Key missing."))
        }

        val prompt = """
            Take this news article written in Hindi and translate it into authentic, sweet, local Bhojpuri (भोजपुरी) language as spoken in Buxar district, Bihar.
            Maintain a warm, enthusiastic, local conversational tone, like how a village head or local elder explains something newsy to a friend.
            
            Title: ${article.title}
            Content: ${article.content}
            
            Provide only the final translated text in Bhojpuri (both Title and Content translated nicely). Do not add any preamble, footnotes, explanations or english characters.
        """.trimIndent()

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(temperature = 0.5)
            )

            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            val resultBhojpuri = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Bhojpuri translation is blank"))

            val updated = article.copy(bhojpuriTranslation = resultBhojpuri)
            articleDao.insertArticle(updated)

            return@withContext Result.success(resultBhojpuri)
        } catch (e: java.lang.Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun askBuxarMitra(apiKey: String, history: List<ChatBubble>, question: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured."))
        }

        // Build prompt context
        val contextPrompt = """
            You are "बक्सर मित्र" (Buxar Mitra), an intelligent, warm, traditional yet highly knowledgeable AI assistant dedicated to Buxar district in Bihar, India.
            You speak fluent Hindi and occasionally mix in warm Bhojpuri words (like 'का हाल बा!', 'रउआ', 'बकलोल', 'बहुत नीक', 'शुभकामना').
            
            You know everything about:
            - Buxar History (Battle of Buxar 1764, Battle of Chausa 1539, Maharishi Vishwamitra's Ashram, Lord Rama getting education here and killing Tadaka, Ahilya Uddhar, Panchkoshi Parikrama)
            - Holy sites (Ramrekha Ghat, Naulakha Mandir, Bihariji Mandir, Panchmukhi Hanuman Mandir)
            - Local culture (Litti-Chokha, local fairs, Ganges river)
            - Geographics (bordered by Ganges river, Uttar Pradesh border, Dumraon subdivision famous for Ustad Bismillah Khan, Brahmpur animal fair, Chausa solar plant)
            
            Answer the user's question with utmost respect, details, and love. If they ask generic questions, guide them politely back to Buxar's context or answer pleasantly.
            
            Format your response cleanly with structured bullet points if sharing recommendations, and keep your tone highly polite and enthusiastic (using 'जी').
        """.trimIndent()

        val contentsList = mutableListOf<GeminiContent>()
        contentsList.add(GeminiContent(parts = listOf(GeminiPart(text = contextPrompt))))

        // Add history
        history.forEach { chat ->
            contentsList.add(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            text = "${if (chat.isUser) "User" else "Assistant"}: ${chat.message}"
                        )
                    )
                )
            )
        }

        contentsList.add(GeminiContent(parts = listOf(GeminiPart(text = "User: $question"))))

        try {
            val request = GeminiRequest(
                contents = contentsList,
                generationConfig = GeminiGenerationConfig(temperature = 0.7)
            )

            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("No answer from Buxar Mitra"))

            return@withContext Result.success(reply)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}

// Intermediary class for Moshi JSON parsing
@JsonClass(generateAdapter = true)
data class GeneratedArticle(
    val title: String,
    val content: String,
    val summary: String,
    val category: String,
    val author: String
)

data class ChatBubble(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
