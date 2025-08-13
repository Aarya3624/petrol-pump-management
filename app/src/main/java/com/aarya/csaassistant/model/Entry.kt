package com.aarya.csaassistant.model

// Custom Serializer for Timestamptz to LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Custom Serializer for Timestamptz to LocalDateTime
object LocalDateTimeSerializer : KSerializer<LocalDateTime?> {
    // Supabase often returns timestamptz like "2025-04-09T07:38:45.934+00:00" or "2025-04-09 07:38:45.934+00"
    // We need to handle both the 'T' separator and the space separator.
    private val formatters = listOf(
        DateTimeFormatter.ISO_OFFSET_DATE_TIME, // Handles "YYYY-MM-DDTHH:mm:ss.SSS+XX:XX"
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX") // Handles "YYYY-MM-DD HH:mm:ss.SSS+XX" (X for no colon in offset)
            .withZone(ZoneId.of("UTC")),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ") // Handles offset like +0000
            .withZone(ZoneId.of("UTC")),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXXX") // Handles offset like +00:00:00
            .withZone(ZoneId.of("UTC")),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS VV") // Handles Zone ID like Europe/Paris
            .withZone(ZoneId.systemDefault())
    )


    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime?) {
        value?.let {
            // Serialize to ISO_OFFSET_DATE_TIME format for Supabase consistency if you send it back
            encoder.encodeString(it.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        } ?: encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): LocalDateTime? {
        val dateString = decoder.decodeString()
        if (dateString.isBlank()) return null

        for (formatter in formatters) {
            try {
                // Attempt to parse as OffsetDateTime first as it's more robust with offsets
                return OffsetDateTime.parse(dateString, formatter)
                    .atZoneSameInstant(ZoneId.systemDefault()) // Convert to user's local timezone
                    .toLocalDateTime()
            } catch (e: DateTimeParseException) {
                // Try next formatter
            }
        }
        // Fallback for unexpected formats, or log an error
        println("Failed to parse LocalDateTime: $dateString with any known format.")
        return null
    }
}


@Serializable
data class Entry @OptIn(ExperimentalTime::class) constructor(
    val id: Int? = null,

    @Contextual
    val created_at: Instant? = null,
    val opening_readings: Map<String, Float>? = null, // Or emptyMap() if you prefer an empty map over null
    val closing_readings: Map<String, Float>? = null, // Or emptyMap()
    val total_sales: Float? = null,                 // Or 0.0f if that's a sensible default
    val sales_details: Map<String, Float>? = null,   // Or emptyMap()
    val settlement_details: Map<String, Float>? = null,
    val balance_short: Float? = null,               // Or 0.0f
    val credit_details: Map<String, Float>? = null,  // Or emptyMap()
    val handed_over_by: String = "", // Non-nullable String, default to empty string
    val handed_over_to: String? = null,
    val shift: String? = null,
    val paytm_card_no: String? = null,
    val closing_images_url: List<String>? = null // Or emptyMap()
)






//@Serializable
//data class Entry(
//    // Ensure the types match your Supabase table schema
//    val id: Int?, // From your example: 10
//
//    @Serializable(with = LocalDateTimeSerializer::class)
//    val created_at: LocalDateTime?, // From your example: 2025-04-09 07:38:45.934+00
//
//    // For JSONB columns that are simple key-value pairs (String to Number/Float)
//    // Supabase client should handle deserializing JSON strings into Maps if column type is JSONB
//    val opening_readings: Map<String, Float>?,
//    val closing_readings: Map<String, Float>?,
//
//    val total_sales: Float?, // From your example: 0 (assuming it can be Float)
//
//    // sales_details: "{""hpPay"": 0, ""upiCard"": 0, ""cashPayable"": 0}"
//    // This is a JSON object. Ensure the keys are consistent.
//    val sales_details: Map<String, Float>?,
//
//    val balance_short: Float?, // From your example: 0
//
//    // credit_details: "{""Kanu"": 25}"
//    val credit_details: Map<String, Float>?,
//
//    val handed_over_by: String?, // Rajani
//    val handed_over_to: String?, // Basu
//    val shift: String?,          // Shift
//
//    val paytm_card_no: String?, // 95 (Can be string to accommodate non-numeric if needed)
//
//    // closing_images_url could be a single URL string, a list of strings, or a map
//    // Based on your example `,,` it seems it might be empty or not a structured JSON in this particular row
//    // If it's intended to be a JSON map: Map<String, String>?
//    // If it's a simple string: String?
//    // Let's assume it *could* be a Map for flexibility, or make it String? if simpler.
//    val closing_images_url: Map<String, String>? = null, // Or String? if it's not a JSON map
//
//    // settlement_details: (empty in your example)
//    // Assuming this is also a JSONB map if it holds data
//    val settlement_details: Map<String, Float>? = null // Example: {"cash": 500.0, "upi": 200.0}
//) {
//    // Secondary constructor for initialization in EntryViewModel (optional but can be handy)
////    @OptIn(kotlin.time.ExperimentalTime::class)
////    constructor() : this(
////        id = null, // Or a unique temporary ID like -1 if 0 is a valid ID from DB
////        created_at = null, // Will be set by DB or on submission
////        opening_readings = null,
////        closing_readings = null,
////        total_sales = null,
////        sales_details = null,
////        balance_short = null,
////        credit_details = null,
////        handed_over_by = "", // Default for new entry form
////        handed_over_to = null,
////        shift = null,
////        paytm_card_no = null,
////        closing_images_url = null,
////        settlement_details = null
////    )
//}