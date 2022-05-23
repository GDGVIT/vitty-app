package com.dscvit.vitty.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.dscvit.vitty.R
import com.dscvit.vitty.adapter.IntroAdapter
import com.dscvit.vitty.databinding.ActivityAuthBinding
import com.dscvit.vitty.util.Constants.TOKEN
import com.dscvit.vitty.util.Constants.UID
import com.dscvit.vitty.util.Constants.USER_INFO
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    private val SIGNIN: Int = 1
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences

    private val pages = listOf("○", "○", "○")
    private var loginClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)
        firebaseAuth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences(USER_INFO, Context.MODE_PRIVATE)
        configureGoogleSignIn()
        setupUI()
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val intent = Intent(this, InstructionsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
        mGoogleSignInClient.signOut()
    }

    private fun setupUI() {
        val pagerAdapter = IntroAdapter(this)
        binding.introPager.adapter = pagerAdapter

        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.apply {
                    if (position == 0 || position == 1) {
                        loginButton.visibility = View.INVISIBLE
                        nextButton.visibility = View.VISIBLE
                    } else {
                        nextButton.visibility = View.INVISIBLE
                        loginButton.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.introPager.registerOnPageChangeCallback(pageChangeCallback)

        TabLayoutMediator(
            binding.introTabs, binding.introPager
        ) { tab, position -> tab.text = pages[position] }.attach()

        binding.nextButton.setOnClickListener {
            binding.apply {
                if (introPager.currentItem != pages.size - 1)
                    introPager.currentItem++
            }
        }

        binding.loginButton.setOnClickListener {
            login()
            if (loginClick) {
                binding.introPager.isUserInputEnabled = false
                binding.introPager.unregisterOnPageChangeCallback(pageChangeCallback)
            }
        }
    }

    private fun login() {
        binding.loadingView.visibility = View.VISIBLE
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, SIGNIN)
    }

    private fun logoutFailed() {
        Toast.makeText(this, getString(R.string.sign_in_fail), Toast.LENGTH_LONG).show()
        binding.loadingView.visibility = View.GONE
        binding.introPager.currentItem = 0
        loginClick = false
    }

    private fun saveInfo(token: String?, uid: String?) {
        with(sharedPref.edit()) {
            putString("sign_in_method", "Google")
            putString(TOKEN, token)
            putString(UID, uid)
            apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGNIN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                logoutFailed()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                loginClick = true
                val uid = firebaseAuth.currentUser?.uid
                saveInfo(acct.idToken, uid)
                val intent = Intent(this, InstructionsActivity::class.java)
                binding.loadingView.visibility = View.GONE
                startActivity(intent)
                finish()
            } else {
                logoutFailed()
            }
        }
    }

    override fun onBackPressed() {
        binding.apply {
            if (introPager.currentItem == 0 || loginClick) {
                super.onBackPressed()
            } else {
                introPager.currentItem--
            }
        }
    }
}
