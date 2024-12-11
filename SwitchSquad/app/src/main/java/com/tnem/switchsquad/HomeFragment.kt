package com.tnem.switchsquad

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
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
    private var websocketListener: WebsocketListener? = null
    private var viewModel: MainViewModel? = null
    private var okHttpClient: OkHttpClient? = null
    private var webSocket: WebSocket? = null

    var connectSwitch: Switch? = null
    var bulb1:ImageView? = null
    var bulb2: ImageView? = null
    var bulb3: ImageView? = null
    var fan1: ImageView? = null
    var text1: ImageView? = null
    var text2: ImageView? = null
    var text3: ImageView? = null
    var text4: ImageView? = null
    var s1: Boolean = false
    var s2: Boolean = false
    var s3: Boolean = false
    var s4: Boolean = false
    var bulbText1:TextView? = null
    var bulbText2: TextView? = null
    var bulbText3: TextView? = null
    var fanText1: TextView? = null
    var loadingDialog: LoadingDialog? = null

    private var jwtToken: String? = null
    private var mcuId: String? = null
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE) as SharedPreferences
        jwtToken = sharedPreferences.getString("token", "none")
        userId = sharedPreferences.getString("userId", "none")
        mcuId = sharedPreferences.getString("mcuId", "none")

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<View>(R.id.logoutBtn).setOnClickListener {
            val editor = sharedPreferences.edit() as SharedPreferences.Editor
            val name = sharedPreferences.getString("name", "none")
            editor.remove("token")
            editor.remove("userId")
            editor.remove("name")
            editor.remove("email")
            editor.remove("mcuId")
            editor.apply()
            Toast.makeText(context, "Meet you soon 👋🏼 $name", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }

        loadingDialog = LoadingDialog(requireActivity())

        connectSwitch = view.findViewById<Switch>(R.id.webSocketBtn)
        bulb1 = view.findViewById<ImageView>(R.id.bulbImg)
        text1 = view.findViewById<ImageView>(R.id.textEdit1)
        bulbText1 = view.findViewById<TextView>(R.id.textView1)
        view.findViewById<TextView>(R.id.textView1).setText(getString(R.string.light_1))

        bulb2 = view.findViewById<ImageView>(R.id.bulbImg2)
        text2 = view.findViewById<ImageView>(R.id.textEdit2)
        bulbText2 = view.findViewById<TextView>(R.id.textView3)
        view.findViewById<TextView>(R.id.textView3).setText("Fan")

        fan1 = view.findViewById<ImageView>(R.id.fanImg)
        text3 = view.findViewById<ImageView>(R.id.textEdit3)
        fanText1 = view.findViewById<TextView>(R.id.textView2)
        view.findViewById<TextView>(R.id.textView2).setText("Light 2")

        bulb3 = view.findViewById<ImageView>(R.id.bulbImg3)
        text4 = view.findViewById<ImageView>(R.id.textEdit4)
        bulbText3 = view.findViewById<TextView>(R.id.textView4)
        view.findViewById<TextView>(R.id.textView4).setText("Light 3")

        okHttpClient = OkHttpClient()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        websocketListener = WebsocketListener(viewModel!!)

        viewModel!!.message.observe(viewLifecycleOwner, object : Observer<String?> {
            override fun onChanged(message: String?) {
                if (message == "Server Busy") {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else if (message == "Server Disconnected, Please wait...") {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    // 65b6675b3289c575c8e7e40b:0?1
                    val command: CharArray? = message?.toCharArray()
                    var i = 0
                    if (command != null) {
                        while (i < command.size) {
                            if (command[i] == ':') {
                                i++
                                if (command[i] != 'r') {
                                    val relay = command[i].toString().toInt()
                                    i += 2
                                    val cmd = command[i].toString().toInt()
                                    if (relay == 0) {
                                        if (cmd == 1) {
                                            s1 = true
                                            bulb1?.setImageResource(R.drawable.bulb_on)
                                        } else {
                                            s1 = false
                                            bulb1?.setImageResource(R.drawable.bulb_off)
                                        }
                                    } else if (relay == 1) {
                                        if (cmd == 1) {
                                            s2 = true
                                            fan1?.setImageResource(R.drawable.fan_on)
                                        } else {
                                            s2 = false
                                            fan1?.setImageResource(R.drawable.fan_off)
                                        }
                                    } else if (relay == 2) {
                                        if (cmd == 1) {
                                            s3 = true
                                            bulb2?.setImageResource(R.drawable.bulb_on)
                                        } else {
                                            s3 = false
                                            bulb2?.setImageResource(R.drawable.bulb_off)
                                        }
                                    } else if (relay == 3) {
                                        if (cmd == 1) {
                                            s4 = true
                                            bulb3?.setImageResource(R.drawable.bulb_on)
                                        } else {
                                            s4 = false
                                            bulb3?.setImageResource(R.drawable.bulb_off)
                                        }
                                    }
                                }
                            }
                            i++
                        }
                    }
                }
            }
        })

        viewModel!!.socketStatus.observe(viewLifecycleOwner, object : Observer<Boolean?> {
            override fun onChanged(isConnected: Boolean?) {
                if (isConnected == true) {
                    loadingDialog?.dismissDialog()
                    bulb1?.setAlpha(1.0f)
                    bulb2?.setAlpha(1.0f)
                    fan1?.setAlpha(1.0f)
                    bulb3?.setAlpha(1.0f)
                    Toast.makeText(context, "Connected!!", Toast.LENGTH_SHORT).show()
                    connectSwitch?.setText("Connected")
                    connectSwitch?.setChecked(true)
                } else {
                    bulb1?.setAlpha(0.6f)
                    bulb2?.setAlpha(0.6f)
                    fan1?.setAlpha(0.6f)
                    bulb3?.setAlpha(0.6f)
                    Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
                    connectSwitch?.setText("Connect W/S")
                    connectSwitch?.setChecked(false)
                }
            }
        })

        get() // Refresh App Switches

        connectSwitch?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                get() // Refresh App Switches
                loadingDialog?.startLoadingDialog(0)
                buttonView.performHapticFeedback(HapticFeedbackConstants.TOGGLE_ON)
                val request = createRequest()
                webSocket = okHttpClient!!.newWebSocket(request, websocketListener!!)
            } else {
//                get() // Refresh App Switches
                loadingDialog?.dismissDialog()
                webSocket!!.close(1000, "")
                // webSocket.close(1000, "Cancelled Manually");
                buttonView.performHapticFeedback(HapticFeedbackConstants.TOGGLE_OFF)
            }
        })

        bulb1?.setOnClickListener(View.OnClickListener { btn ->
            var msg = "$mcuId:0?"
            if (bulb1?.getAlpha() == 1.0f) {
                if (!s1) {
                    msg = msg + "1"
                    bulb1?.setImageResource(R.drawable.bulb_on)
                } else {
                    msg = msg + "0"
                    bulb1?.setImageResource(R.drawable.bulb_off)
                }
                s1 = !s1
                webSocket!!.send(msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                btn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            } else {
                loadingDialog?.startLoadingDialog(0)
                val request = createRequest()
                webSocket = okHttpClient!!.newWebSocket(request, websocketListener!!)
                Toast.makeText(
                    context,
                    "Connecting to the websocket please wait....",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        text1?.setOnClickListener(View.OnClickListener {
            val view1: View =
                LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)
            val editText = view1.findViewById<TextInputEditText>(R.id.editText1)
            val alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Title")
                .setView(view1)
                .setPositiveButton("Ok") { dialog, which ->
                    bulbText1?.setText(editText.text)
                    dialog.dismiss()
                }.setNegativeButton(
                    "Close"
                ) { dialog, which -> dialog.dismiss() }.create()
            alertDialog.show()
        })

        fan1?.setOnClickListener(View.OnClickListener { btn ->
            var msg = "$mcuId:2?"
            if (fan1?.getAlpha() == 1.0f) {
                if (!s2) {
                    msg = msg + "1"
                    fan1?.setImageResource(R.drawable.fan_on)
                } else {
                    msg = msg + "0"
                    fan1?.setImageResource(R.drawable.fan_off)
                }
                s2 = !s2
                webSocket!!.send(msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                btn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            } else {
                loadingDialog?.startLoadingDialog(0)
                val request = createRequest()
                webSocket = okHttpClient!!.newWebSocket(request, websocketListener!!)
                Toast.makeText(
                    context,
                    "Connecting to the websocket please wait....",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        text2?.setOnClickListener(View.OnClickListener {
            val view1 = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, null)
            val editText = view1.findViewById<TextInputEditText>(R.id.editText1)
            val alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Title")
                .setView(view1)
                .setPositiveButton("Ok") { dialog, which ->
                    fanText1?.setText(editText.text)
                    dialog.dismiss()
                }.setNegativeButton(
                    "Close"
                ) { dialog, which -> dialog.dismiss() }.create()
            alertDialog.show()
        })

        bulb2?.setOnClickListener(View.OnClickListener { btn ->
            var msg = "$mcuId:1?"
            if (bulb2?.getAlpha() == 1.0f) {
                if (!s3) {
                    msg = msg + "1"
                    bulb2?.setImageResource(R.drawable.bulb_on)
                } else {
                    msg = msg + "0"
                    bulb2?.setImageResource(R.drawable.bulb_off)
                }
                s3 = !s3
                webSocket!!.send(msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                btn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            } else {
                loadingDialog?.startLoadingDialog(0)
                val request = createRequest()
                webSocket = okHttpClient!!.newWebSocket(request, websocketListener!!)
                Toast.makeText(
                    context,
                    "Connecting to the websocket please wait....",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        text3?.setOnClickListener(View.OnClickListener {
            val view1 = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, null)
            val editText = view1.findViewById<TextInputEditText>(R.id.editText1)
            val alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Title")
                .setView(view1)
                .setPositiveButton("Ok") { dialog, which ->
                    bulbText2?.setText(editText.text)
                    dialog.dismiss()
                }.setNegativeButton(
                    "Close"
                ) { dialog, which -> dialog.dismiss() }.create()
            alertDialog.show()
        })

        bulb3?.setOnClickListener(View.OnClickListener { btn ->
            var msg = "$mcuId:3?"
            if (bulb3?.getAlpha() == 1.0f) {
                if (!s4) {
                    msg = msg + "1"
                    bulb3?.setImageResource(R.drawable.bulb_on)
                } else {
                    msg = msg + "0"
                    bulb3?.setImageResource(R.drawable.bulb_off)
                }
                s4 = !s4
                webSocket!!.send(msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                btn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            } else {
                loadingDialog?.startLoadingDialog(0)
                val request = createRequest()
                webSocket = okHttpClient!!.newWebSocket(request, websocketListener!!)
                Toast.makeText(
                    context,
                    "Connecting to the websocket please wait....",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        text4?.setOnClickListener(View.OnClickListener {
            val view1 = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, null)
            val editText = view1.findViewById<TextInputEditText>(R.id.editText1)
            val alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Title")
                .setView(view1)
                .setPositiveButton("Ok") { dialog, which ->
                    bulbText3?.setText(editText.text)
                    dialog.dismiss()
                }.setNegativeButton(
                    "Close"
                ) { dialog, which -> dialog.dismiss() }.create()
            alertDialog.show()
        })

        return view
    }

    private fun createRequest(): Request {
        val webSocketUrl = "wss://" + getString(R.string.ws_url) + "/ws/$userId"
        return Request.Builder()
            .url(webSocketUrl)
            .build()
    }

    private fun get() {
        if (mcuId.equals("none")) {
            getEsp()
        }
        val rsp = arrayOf("")
        val getURL = "https://" + getString(R.string.node_url) + "/api/v1/esp/switch"
        val request: Request = Request.Builder()
            .url(getURL)
            .header("Authorization", "Bearer $jwtToken")
            .build()
        okHttpClient?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread(Runnable {
                    try {
                        rsp[0] = response.body!!.string()
                        var jo = JSONObject(rsp[0])
                        jo = JSONObject(jo.getString("states"))
                        val jsonArray = jo.getJSONArray("states")
                        //                            textView.setText(jsonArray.toString());
                        if (jsonArray.getInt(0) == 1) {
                            s1 = true
                            bulb1?.setImageResource(R.drawable.bulb_on)
                        } else {
                            s1 = false
                            bulb1?.setImageResource(R.drawable.bulb_off)
                        }
                        if (jsonArray.getInt(1) == 1) {
                            s2 = true
                            fan1?.setImageResource(R.drawable.fan_on)
                        } else {
                            s2 = false
                            fan1?.setImageResource(R.drawable.fan_off)
                        }
                        if (jsonArray.getInt(2) == 1) {
                            s3 = true
                            bulb2?.setImageResource(R.drawable.bulb_on)
                        } else {
                            s3 = false
                            bulb2?.setImageResource(R.drawable.bulb_off)
                        }
                        if (jsonArray.getInt(3) == 1) {
                            s4 = true
                            bulb3?.setImageResource(R.drawable.bulb_on)
                        } else {
                            s4 = false
                            bulb3?.setImageResource(R.drawable.bulb_off)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                })
            }
        })
    }

    private fun getEsp() {
        val getEspURL = "https://" + getString(R.string.node_url) + "/api/v1/esp"
        val rsp = arrayOf("")
        val request: Request = Request.Builder()
            .url(getEspURL)
            .header("Authorization", "Bearer $jwtToken")
            .build()
        Toast.makeText(context, "Getting Switches", Toast.LENGTH_SHORT).show()
        loadingDialog?.startLoadingDialog(1)
        okHttpClient?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread(Runnable {
                    try {
                        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE) as SharedPreferences
                        val editor = sharedPreferences.edit() as SharedPreferences.Editor

                        rsp[0] = response.body!!.string()
                        var jo = JSONObject(rsp[0])
                        jo = JSONObject(jo.getString("data"))
                        val jsonArray = jo.getJSONArray("esps")
                        jo = jsonArray.getJSONObject(0)
                        mcuId = jo.getString("_id")
                        editor.putString("mcuId", mcuId)
                        editor.apply()
                    } catch (e: IOException) {
                        Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show()
                        Log.d("httpError", e.toString())
                    } catch (e: JSONException) {
                        Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show()
                        Log.d("httpError", e.toString())
                    }
                    loadingDialog?.dismissDialog()
                })
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
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}