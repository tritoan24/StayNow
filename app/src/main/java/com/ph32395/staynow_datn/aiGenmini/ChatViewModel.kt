package com.ph32395.staynow_datn.aiGenmini

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.Model.PhongTroWithId
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _messageList = MutableLiveData<List<MessageModel>>(emptyList())
    val messageList: LiveData<List<MessageModel>> get() = _messageList
    private var allRooms = listOf<PhongTroWithId>()
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = Constants.apiKey
    )

    init {
        viewModelScope.launch {
            fetchRoomsFromFirebase()
        }
    }

    fun sendMessage(question: String) {
        val currentMessages = _messageList.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(MessageModel(question, "user"))
        currentMessages.add(MessageModel("Typing...", "model"))
        _messageList.value = currentMessages

        viewModelScope.launch {
            try {
                if (isRoomRelatedQuery(question)) {
                    handleRoomQuery(question, currentMessages)
                } else {
                    handleNormalChat(question, currentMessages)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error during chat", e)
                currentMessages.removeLast()
                currentMessages.add(MessageModel("Xin lỗi, đã có lỗi xảy ra: ${e.message}", "model"))
                _messageList.value = currentMessages
            }
        }
    }

    private fun isRoomRelatedQuery(query: String): Boolean {
        val roomKeywords = listOf(
            "tìm phòng", "thuê phòng", "phòng trọ", "nhà trọ",
            "giá phòng", "phòng có", "tìm nhà", "ở trọ",
            "phòng giá", "phòng cho thuê", "giá rẻ", "sinh viên",
            "gần", "khu vực", "quận", "phường", "đường"
        )
        return roomKeywords.any { query.lowercase().contains(it) }
    }

    private suspend fun handleRoomQuery(query: String, currentMessages: MutableList<MessageModel>) {
        val requirements = analyzeRoomRequirements(query)

        // Kiểm tra xem địa điểm yêu cầu có trong hệ thống không
        val locationExists = requirements.location?.let { requestedLocation ->
            allRooms.any { room ->
                extractLocationParts(room.phongTro.diaChi).any { part ->
                    part.lowercase().contains(requestedLocation.lowercase()) ||
                            requestedLocation.lowercase().contains(part.lowercase())
                }
            }
        } ?: true

        val response = if (!locationExists) {
            buildNoMatchResponse(requirements)
        } else {
            val exactMatches = findMatchingRooms(requirements)
            if (exactMatches.isNotEmpty()) {
                buildExactMatchResponse(exactMatches, requirements)
            } else {
                val similarRooms = findSimilarRooms(requirements)
                if (similarRooms.isNotEmpty()) {
                    buildSimilarRoomResponse(similarRooms, requirements)
                } else {
                    buildNoMatchResponse(requirements)
                }
            }
        }

        currentMessages.removeAt(currentMessages.lastIndex)
        currentMessages.add(MessageModel(response, "model"))
        _messageList.value = currentMessages.toList()
    }

    private fun analyzeRoomRequirements(query: String): RoomRequirements {
        return RoomRequirements(
            maxPrice = extractMaxPrice(query),
            minPrice = extractMinPrice(query),
            location = extractLocation(query),
            features = extractFeatures(query),
            numberOfPeople = extractNumberOfPeople(query),
            propertyType = extractPropertyType(query)
        )
    }

    private fun extractMaxPrice(query: String): Double? {
        val maxPricePattern = """(?:dưới|tối đa|maximum|max|<)\s*(\d+(?:\.\d+)?)\s*(triệu|trieu|tr)""".toRegex()
        return maxPricePattern.find(query.lowercase())?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractMinPrice(query: String): Double? {
        val minPricePattern = """(?:trên|tối thiểu|minimum|min|>)\s*(\d+(?:\.\d+)?)\s*(triệu|trieu|tr)""".toRegex()
        return minPricePattern.find(query.lowercase())?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractLocation(query: String): String? {
        // Danh sách các từ khóa chỉ địa điểm
        val locationKeywords = """(?:ở|tại|khu vực|quận|huyện|phường|xã|đường|phố|khu|khu đô thị|kđt|quanh|gần|khu vực|dọc|cạnh)\s+([^\s,\.]+(?:\s+[^\s,\.]+)*)""".toRegex()

        // Tìm địa điểm trong câu hỏi
        val match = locationKeywords.find(query)?.groupValues?.get(1)?.trim()
            ?: query.split(" ").find { word ->
                isKnownLocation(word.trim().lowercase())
            }

        return match
    }
    private fun isKnownLocation(location: String): Boolean {
        // Danh sách các địa điểm đã biết, nên được cập nhật dựa trên dữ liệu thực tế từ Firebase
        val knownLocations = allRooms.flatMap { room ->
            extractLocationParts(room.phongTro.diaChi)
        }.distinct()

        return knownLocations.any {
            it.lowercase().contains(location.lowercase()) ||
                    location.lowercase().contains(it.lowercase())
        }
    }
    private fun extractLocationParts(address: String): List<String> {
        // Tách địa chỉ thành các phần để dễ tìm kiếm
        return address.split(",", " ").map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun extractFeatures(query: String): List<String> {
        val features = mutableListOf<String>()
        val featureKeywords = mapOf(
            "điều hòa" to listOf("điều hòa", "máy lạnh"),
            "ban công" to listOf("ban công", "bancong"),
            "nội thất" to listOf("nội thất", "full đồ", "đầy đủ đồ"),
            "wifi" to listOf("wifi", "internet", "mạng"),
            "gác lửng" to listOf("gác", "gác lửng", "gác xép"),
            "nhà vệ sinh" to listOf("nhà vệ sinh", "toilet", "wc", "vệ sinh"),
            "bếp" to listOf("bếp", "nấu ăn")
        )

        featureKeywords.forEach { (feature, keywords) ->
            if (keywords.any { query.lowercase().contains(it) }) {
                features.add(feature)
            }
        }
        return features
    }

    private fun extractNumberOfPeople(query: String): Int? {
        val peoplePattern = """(\d+)\s*(?:người|sinh viên|học sinh|người ở|khách)""".toRegex()
        return peoplePattern.find(query)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractPropertyType(query: String): String? {
        val typeKeywords = mapOf(
            "phòng trọ" to listOf("phòng trọ", "phòng cho thuê", "phòng"),
            "nhà nguyên căn" to listOf("nhà nguyên căn", "nhà thuê", "căn nhà"),
            "chung cư mini" to listOf("chung cư mini", "ccmini", "cc mini")
        )

        typeKeywords.forEach { (type, keywords) ->
            if (keywords.any { query.lowercase().contains(it) }) {
                return type
            }
        }
        return null
    }
    private fun findMatchingRooms(requirements: RoomRequirements): List<PhongTroWithId> {
        return allRooms.filter { roomWithId ->
            val room = roomWithId.phongTro
            var matches = true

            // Kiểm tra địa điểm
            requirements.location?.let { requestedLocation ->
                val roomLocationParts = extractLocationParts(room.diaChi)
                val found = roomLocationParts.any { part ->
                    part.lowercase().contains(requestedLocation.lowercase()) ||
                            requestedLocation.lowercase().contains(part.lowercase())
                }
                if (!found) matches = false
            }

            // Các điều kiện khác giữ nguyên...
            matches
        }.take(5)
    }
//    private fun findMatchingRooms(requirements: RoomRequirements): List<PhongTroWithId> {
//        return allRooms.filter { roomWithId ->
//            val room = roomWithId.phongTro
//            var matches = true
//
//            // Kiểm tra giá
//            requirements.maxPrice?.let { maxPrice ->
//                if (room.giaPhong > maxPrice * 1000000) matches = false
//            }
//            requirements.minPrice?.let { minPrice ->
//                if (room.giaPhong < minPrice * 1000000) matches = false
//            }
//
//            // Kiểm tra địa điểm
//            requirements.location?.let { location ->
//                if (!room.diaChiChiTiet.lowercase().contains(location.lowercase())) matches = false
//            }
//
////            // Kiểm tra tiện nghi
////            requirements.features.forEach { feature ->
////                if (!room.tienNghi.lowercase().contains(feature.lowercase())) matches = false
////            }
//
////            // Kiểm tra số người
////            requirements.numberOfPeople?.let { people ->
////                if (room.soNguoiToiDa < people) matches = false
////            }
//
//            matches
//        }.take(5)
//    }

    private fun findSimilarRooms(requirements: RoomRequirements): List<PhongTroWithId> {
        val similarRooms = mutableListOf<Pair<PhongTroWithId, Int>>()

        allRooms.forEach { room ->
            var matchScore = 0

            // Tính điểm phù hợp về giá
            requirements.maxPrice?.let { maxPrice ->
                val priceRatio = room.phongTro.giaPhong / (maxPrice * 1000000)
                if (priceRatio <= 1.2) matchScore += 3 // Giá cao hơn tối đa 20%
            }

            // Tính điểm phù hợp về địa điểm
            requirements.location?.let { location ->
                if (room.phongTro.diaChi.lowercase().contains(location.lowercase())) {
                    matchScore += 3
                } else if (isNearbyLocation(location, room.phongTro.diaChi)) {
                    matchScore += 2
                }
            }

//            // Tính điểm phù hợp về tiện nghi
//            requirements.features.forEach { feature ->
//                if (room.phongTro.tienNghi.lowercase().contains(feature.lowercase())) {
//                    matchScore += 1
//                }
//            }

//            // Tính điểm phù hợp về số người
//            requirements.numberOfPeople?.let { people ->
//                if (room.phongTro.soNguoiToiDa >= people) {
//                    matchScore += 2
//                }
//            }

            if (matchScore > 0) {
                similarRooms.add(Pair(room, matchScore))
            }
        }

        return similarRooms
            .sortedByDescending { it.second }
            .map { it.first }
            .take(3)
    }

    private fun isNearbyLocation(requestedLocation: String, actualLocation: String): Boolean {
        val nearbyLocations = when (requestedLocation.lowercase()) {
            "hà đông" -> listOf("thanh xuân", "hoàng mai", "cầu giấy")
            "cầu giấy" -> listOf("nam từ liêm", "bắc từ liêm", "thanh xuân", "tây hồ")
            "đống đa" -> listOf("hai bà trưng", "thanh xuân", "ba đình", "hoàn kiếm")
            "thanh xuân" -> listOf("hà đông", "đống đa", "cầu giấy")
            else -> emptyList()
        }

        return nearbyLocations.any { actualLocation.lowercase().contains(it.lowercase()) }
    }

    private fun buildExactMatchResponse(matches: List<PhongTroWithId>, requirements: RoomRequirements): String {
        return buildString {
            append("Tôi đã tìm thấy ${matches.size} phòng phù hợp với yêu cầu của bạn:\n\n")
            append("RECOMMENDED_ROOMS:")
            append(matches.joinToString(",") { it.id })
            append(":END_RECOMMENDED_ROOMS\n\n")
            append("Các phòng này đều đáp ứng các tiêu chí của bạn")
            requirements.location?.let { append(" tại khu vực $it") }
            requirements.maxPrice?.let { append(", giá dưới $it triệu") }
            if (requirements.features.isNotEmpty()) {
                append(" và có các tiện nghi như ${requirements.features.joinToString(", ")}")
            }
            append(".\n\nBạn có thể xem chi tiết các phòng trên để tìm phòng phù hợp nhất.")
        }
    }

    private fun buildSimilarRoomResponse(similarRooms: List<PhongTroWithId>, requirements: RoomRequirements): String {
        return buildString {
            append("Tôi không tìm thấy phòng nào hoàn toàn khớp với yêu cầu của bạn, ")
            append("nhưng tôi có một số gợi ý phòng tương tự có thể phù hợp:\n\n")

            requirements.maxPrice?.let {
                append("- Có thể bạn sẽ cần cân nhắc một chút về giá để có nhiều lựa chọn hơn\n")
            }

            requirements.location?.let {
                append("- Bạn có thể xem xét các khu vực lân cận như ")
                append(getNearbyLocations(it).joinToString(", "))
                append("\n")
            }

            append("\nDưới đây là các phòng gợi ý phù hợp nhất từ dữ liệu của chúng tôi:\n")
            append("RECOMMENDED_ROOMS:")
            append(similarRooms.joinToString(",") { it.id })
            append(":END_RECOMMENDED_ROOMS\n\n")
            append("Bạn có thể xem chi tiết các phòng này để tham khảo và lựa chọn phòng phù hợp nhất.")
        }
    }

    private fun buildNoMatchResponse(requirements: RoomRequirements): String {
        return buildString {
            append("Xin lỗi, hiện tại StayNow không có phòng nào")
            requirements.location?.let {
                append(" ở $it")
            }
            append(" phù hợp với yêu cầu của bạn.\n\n")

            // Thêm gợi ý các khu vực có phòng
            val availableLocations = allRooms.flatMap { room ->
                extractLocationParts(room.phongTro.diaChi)
            }.distinct().take(5)

            if (availableLocations.isNotEmpty()) {
                append("Bạn có thể tham khảo các khu vực đang có phòng trống:\n")
                availableLocations.forEach { location ->
                    append("- $location\n")
                }
            }

            append("\nBạn có muốn xem phòng ở các khu vực này không?")
        }
    }

    private fun getNearbyLocations(location: String): List<String> {
        return when (location.lowercase()) {
            "hà đông" -> listOf("Thanh Xuân", "Hoàng Mai", "Nam Từ Liêm")
            "cầu giấy" -> listOf("Nam Từ Liêm", "Bắc Từ Liêm", "Thanh Xuân", "Tây Hồ")
            "đống đa" -> listOf("Hai Bà Trưng", "Thanh Xuân", "Ba Đình", "Hoàn Kiếm")
            "thanh xuân" -> listOf("Hà Đông", "Đống Đa", "Nam Từ Liêm")
            "tây hồ" -> listOf("Ba Đình", "Cầu Giấy", "Bắc Từ Liêm")
            "hai bà trưng" -> listOf("Hoàn Kiếm", "Đống Đa", "Hoàng Mai")
            "nam từ liêm" -> listOf("Bắc Từ Liêm", "Cầu Giấy", "Thanh Xuân", "Hà Đông")
            "bắc từ liêm" -> listOf("Tây Hồ", "Cầu Giấy", "Nam Từ Liêm")
            "hoàng mai" -> listOf("Hai Bà Trưng", "Thanh Xuân", "Hà Đông")
            "ba đình" -> listOf("Tây Hồ", "Cầu Giấy", "Đống Đa", "Hoàn Kiếm")
            "hoàn kiếm" -> listOf("Hai Bà Trưng", "Ba Đình", "Đống Đa")
            else -> listOf(location)
        }
    }
    private suspend fun handleNormalChat(question: String, currentMessages: MutableList<MessageModel>) {
        // Thêm ngữ cảnh cho AI bằng cách thêm vào như một tin nhắn model
        val contextMessage = MessageModel("""
            Tôi là trợ lý thông minh của ứng dụng tìm phòng trọ StayNow. 
            Tôi có thể trả lời các câu hỏi chung về cuộc sống, học tập, công việc.
            Với các câu hỏi về phòng trọ, tôi chỉ giới thiệu các phòng có trong hệ thống của StayNow.
            Tôi không đề xuất hoặc giới thiệu bất kỳ phòng trọ nào từ bên ngoài.
        """.trimIndent(), "model")

        val chat = generativeModel.startChat(
            history = listOf(content(contextMessage.role) { text(contextMessage.message) }) +
                    currentMessages.map {
                        content(it.role) { text(it.message) }
                    }
        )

        val response = chat.sendMessage(question)
        val responseText = response.text?.toString() ?: "Xin lỗi, tôi không thể xử lý yêu cầu này. Vui lòng thử lại."

        // Kiểm tra và chặn nếu AI cố gắng giới thiệu phòng trọ bên ngoài
        val filteredResponse = if (containsExternalRoomSuggestion(responseText)) {
            "Xin lỗi, tôi chỉ có thể tư vấn về các phòng trọ có trong hệ thống StayNow. Bạn có thể hỏi tôi về các tiêu chí cụ thể (như giá cả, khu vực) để tôi tìm phòng phù hợp trong hệ thống cho bạn."
        } else {
            responseText
        }

        currentMessages.removeAt(currentMessages.lastIndex)
        currentMessages.add(MessageModel(filteredResponse, "model"))
        _messageList.value = currentMessages.toList()
    }

    private fun containsExternalRoomSuggestion(response: String): Boolean {
        val externalKeywords = listOf(
            "chợ tốt", "batdongsan", "chotot", "facebook", "phongtro123",
            "nhà đất", "môi giới", "sàn bất động sản", "trang web",
            "có thể tìm", "nên tìm", "thử tìm", "tham khảo"
        )

        return externalKeywords.any { keyword ->
            response.lowercase().contains(keyword)
        }
    }

    private suspend fun fetchRoomsFromFirebase() {
        _loading.value = true
        try {
            val roomsSnapshot = db.collection("PhongTro").get().await()
            allRooms = roomsSnapshot.documents.mapNotNull { document ->
                val phongTro = PhongTroModel.fromDocument(document)
                phongTro?.let {
                    PhongTroWithId(document.id, it)
                }
            }
            Log.d("RoomRecommendation", "Đã tải ${allRooms.size} phòng từ Firebase")
        } catch (e: Exception) {
            Log.e("RoomRecommendation", "Lỗi khi tải danh sách phòng", e)
            _error.value = "Không thể tải danh sách phòng. Vui lòng thử lại sau."
            throw e
        } finally {
            _loading.value = false
        }
    }
}

data class RoomRequirements(
    val maxPrice: Double? = null,
    val minPrice: Double? = null,
    val location: String? = null,
    val features: List<String> = emptyList(),
    val numberOfPeople: Int? = null,
    val propertyType: String? = null
)
