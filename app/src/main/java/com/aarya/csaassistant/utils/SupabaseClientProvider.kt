package com.aarya.csaassistant.utils

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest


object SupabaseClientProvider {
    val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRzZ3NzcnhxZmpveXJkdXd0amxqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI3MzkxODAsImV4cCI6MjA1ODMxNTE4MH0.E5NsqPsLNf5p3rSbNN3JkBnfgymJZnnQVbljSB2UEMQ",
            supabaseUrl = "https://tsgssrxqfjoyrduwtjlj.supabase.co"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}