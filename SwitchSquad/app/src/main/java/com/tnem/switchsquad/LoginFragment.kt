package com.tnem.switchsquad

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var view: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false)

        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE) as SharedPreferences
        val value = sharedPreferences.getString("token", "none")

        if (!value.equals("none")) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment);
        }

        view.findViewById<Button>(R.id.signUpBtn).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        view.findViewById<Button>(R.id.loginBtn).setOnClickListener {
            Toast.makeText(context, "Logging In", Toast.LENGTH_SHORT).show()
            post()
        }

        return view
    }

    private fun post() {
        val okHttpClient = OkHttpClient()
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull();
        var json: String;
        val email = view.findViewById<View>(R.id.editTextEmailAddress) as EditText;
        val password = view.findViewById<View>(R.id.editTextPassword) as EditText;
        try {
            json = JSONObject()
                .put("email", email.text)
                .put("password", password.text)
                .toString();
        } catch (e: JSONException) {
            throw RuntimeException(e);
        }
        val requestBody: RequestBody = json.toRequestBody(JSON)
        val URL = "https://" + getString(R.string.node_url) + "/api/v1/users/login"
        val request = Request.Builder().url(URL).post(requestBody).build()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("httpError", "onFailure: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    try {
                        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE) as SharedPreferences
                        val editor = sharedPreferences.edit() as SharedPreferences.Editor

                        assert(response.body != null)
                        val rsp = response.body?.string()

                        var jo: JSONObject = JSONObject(rsp)

                        if (!jo.getString("status").equals("fail")) {
                            editor.putString("token", jo.getString("token"));

                            jo = JSONObject(jo.getString("data"))
                            jo = JSONObject(jo.getString("user"))
                            editor.putString("userId", jo.getString("_id"))
                            editor.putString("name", jo.getString("name"))
                            editor.putString("email", jo.getString("email"))
                            editor.apply()
                            Toast.makeText(context, "Hello 👋🏼 " + jo.getString("name"), Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment);
                        } else {
                            Toast.makeText(context, "Email or password is wrong", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        Toast.makeText(context, "Email or password is wrong", Toast.LENGTH_SHORT).show()
                        Log.d("httpError", "run: $e")
                    }
                    catch (e: JSONException) {
                        Toast.makeText(context, "Email or password is wrong", Toast.LENGTH_SHORT).show()
                        Log.d("httpError", "run: $e")
                    }
                }
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

private fun RequestBody.Companion.create(json: String, json1: MediaType?) {

}
