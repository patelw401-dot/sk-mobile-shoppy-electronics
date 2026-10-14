package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseConfig {
    private const val TAG = "FirebaseConfig"

    @Volatile
    private var firestoreInstance: FirebaseFirestore? = null

    fun isConfigured(context: Context): Boolean {
        return try {
            val dbId = context.getString(R.string.firestore_database_id)
            dbId.isNotBlank() && FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun getFirestore(context: Context): FirebaseFirestore {
        return firestoreInstance ?: synchronized(this) {
            firestoreInstance ?: try {
                val dbId = context.getString(R.string.firestore_database_id)
                val app = FirebaseApp.getInstance()
                FirebaseFirestore.getInstance(app, dbId).also {
                    firestoreInstance = it
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize custom Firestore instance, falling back to default", e)
                FirebaseFirestore.getInstance().also {
                    firestoreInstance = it
                }
            }
        }
    }

    fun getAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
