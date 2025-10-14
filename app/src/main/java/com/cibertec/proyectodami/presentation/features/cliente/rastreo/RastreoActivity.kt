package com.cibertec.proyectodami.presentation.features.cliente.rastreo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ActivityRastreoBinding
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

class RastreoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRastreoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityRastreoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pedidoId = intent.getStringExtra("PEDIDO_ID")
        val numeroRepartidor: String = "928845092"
        val mensajePredeterminado:String = "¡Hola! Estan cerca con el pedido?"

        val btnBack: ImageView = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        binding.btnCallRepartidor.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) ==
                PackageManager.PERMISSION_GRANTED) {

                val miIntent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:${numeroRepartidor}")
                )
                startActivity(miIntent)
            }else {
                Toast.makeText( this,"No hay permiso para realizar la llamda", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    123
                )
            }
        }

        fun enviarMensaje(numero:String, mensaje:String){
            try {
                val sms = SmsManager.getDefault()
                sms.sendTextMessage(numero,null,mensaje,null,null)
                Toast.makeText(this, "Mensaje enviado Correctamente", Toast.LENGTH_SHORT).show()
            }catch (ex:Exception){
                Toast.makeText(this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMessageRepartidor.setOnClickListener{

            if(ContextCompat.checkSelfPermission(
                    this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED
            ){
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    1234
                )
            }else{
                enviarMensaje(numeroRepartidor,mensajePredeterminado)
            }
        }

        inicializarEstados()

    }

    private var iconosEstado = listOf(
        R.id.icEstadoAceptado,
        R.id.icEstadoPreparando,
        R.id.icEstadoTransito,
        R.id.icEstadoCerca,
        R.id.icEstadoEntregado
    )

    private var lblEstados = listOf(
        R.id.lblAceptado,
        R.id.lblPreparando,
        R.id.lblEnTransito,
        R.id.lblCerca,
        R.id.lblEntregado

    )

    private fun inicializarEstados(){
        actualizarEstado(2)
    }

    private fun actualizarEstado(estado: Int) {
        val iconos = listOf(
            R.id.icEstadoAceptado,
            R.id.icEstadoPreparando,
            R.id.icEstadoTransito,
            R.id.icEstadoCerca,
            R.id.icEstadoEntregado
        )

        val labels = listOf(
            R.id.lblAceptado,
            R.id.lblPreparando,
            R.id.lblEnTransito,
            R.id.lblCerca,
            R.id.lblEntregado
        )

        for (i in 0..4) {
            val icono = findViewById<ImageView>(iconos[i])
            val label = findViewById<TextView>(labels[i])

            if (i <= estado) {
                icono.setColorFilter(getColor(R.color.white))
                icono.setBackground(getDrawable(R.drawable.ic_border_status))
                label.setTextColor(getColor(R.color.color_principal))
                label.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                icono.setColorFilter(getColor(R.color.white))
                icono.setBackground(getDrawable(R.drawable.ic_border_status_off))
                label.setTextColor(getColor(R.color.color_subtitulos))
                label.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

}