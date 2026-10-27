package com.miladrostami.moed.domain

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.miladrostami.moed.BuildConfig

data class LocalGoogleUser(val id: String, val name: String, val email: String, val picture: String?,val phone:String?=null,val provider:String="google")

class GoogleAuthManager(private val context: Context) {
    private val preferences = context.getSharedPreferences("google_profile", Context.MODE_PRIVATE)
    private val credentials = CredentialManager.create(context)

    val configured: Boolean get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()
    val hasCompletedOnboarding: Boolean get() = preferences.getBoolean("onboarding_done", false)

    fun completeOnboarding() { preferences.edit().putBoolean("onboarding_done", true).apply() }

    fun currentUser(): LocalGoogleUser? {
        val id = preferences.getString("id", null) ?: return null
        return LocalGoogleUser(
            id = id,
            name = preferences.getString("name", "") ?: "",
            email = preferences.getString("email", "") ?: "",
            picture = preferences.getString("picture", null),
            phone = preferences.getString("phone", null),
            provider = preferences.getString("provider", "google") ?: "google"
        )
    }

    suspend fun signIn(activity: Activity): LocalGoogleUser {
        check(configured) { "شناسه OAuth گوگل هنوز تنظیم نشده است" }
        val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential = credentials.getCredential(activity, request).credential
        require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "پاسخ ورود گوگل معتبر نیست"
        }
        val google = GoogleIdTokenCredential.createFrom(credential.data)
        val user = LocalGoogleUser(google.id, google.displayName ?: google.id, google.id, google.profilePictureUri?.toString())
        preferences.edit().putString("id", user.id).putString("name", user.name)
            .putString("email", user.email).putString("picture", user.picture).putString("provider","google").remove("phone").apply()
        return user
    }

    fun registerLocal(name:String,email:String,phone:String):LocalGoogleUser {
        require(name.trim().isNotEmpty()) { "نام و نام خانوادگی الزامی است" }
        val user=LocalGoogleUser("local",name.trim(),email.trim(),null,phone.trim().ifEmpty{null},"local")
        preferences.edit().putString("id",user.id).putString("name",user.name).putString("email",user.email)
            .putString("phone",user.phone).putString("provider","local").remove("picture").apply()
        completeOnboarding()
        return user
    }

    suspend fun signOut() {
        credentials.clearCredentialState(ClearCredentialStateRequest())
        preferences.edit().remove("id").remove("email").remove("phone").remove("provider").apply()
    }

    fun saveProfile(name:String,picture:String?){preferences.edit().putString("name",name.trim()).putString("picture",picture).apply()}
}
