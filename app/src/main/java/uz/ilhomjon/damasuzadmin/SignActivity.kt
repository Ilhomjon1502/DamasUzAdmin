package uz.ilhomjon.damasuzadmin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import uz.ilhomjon.damasuzadmin.databinding.ActivitySignBinding
import uz.ilhomjon.damasuzadmin.models.Admin
import java.util.concurrent.TimeUnit

class SignActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignBinding
    lateinit var auth: FirebaseAuth
    private val TAG = "SignActivity"
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var referenceAdmin: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("uz")

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        firebaseDatabase = FirebaseDatabase.getInstance()
        referenceAdmin = firebaseDatabase.getReference("admin")

        binding.btnSend.setOnClickListener {
            val phoneNumber = binding.edtNumber.text.toString()

            referenceAdmin.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    val admin = Admin()
                    for (child in children) {
                        if (child.key == "name")
                            admin.name = child.value as String
                        if (child.key == "number")
                            admin.number = child.value as String
                    }

                    if (admin.number!=null && admin.name!=null){
                        if (admin.number == phoneNumber){
                            sendVerificationCode(phoneNumber)
                            Toast.makeText(this@SignActivity, "$phoneNumber raqaminga sms jo'natildi", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@SignActivity, "$phoneNumber raqami adminlar ro'yhatidan topilmadi", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this@SignActivity, "Admin null", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SignActivity,
                        "Tarmoqda xatolik! \nInternetni yoqing va qayta urinib ko'ring, bo'lmasa \nMa'lumot uchun +998 99 602 15 02 ga murojaat qiling \n${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // kelgan sms codni avtomatik o'qib olish
        binding.edtCode.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifiyCode()
                val view = currentFocus
                if (view != null) {
                    val imm: InputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
            true
        }

        //codni qo'lda yozish
        binding.edtCode.addTextChangedListener {
            if (it.toString().length == 6) {
                verifiyCode()
            }
        }
    }

    private fun verifiyCode() {
        val code = binding.edtCode.text.toString()
        if (code.length == 6) {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    Toast.makeText(this, "Muvaffaqiyatli", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Muvaffaqiyatsiz!!!", Toast.LENGTH_SHORT).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Kod xato kiritildi", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
            }
    }
}