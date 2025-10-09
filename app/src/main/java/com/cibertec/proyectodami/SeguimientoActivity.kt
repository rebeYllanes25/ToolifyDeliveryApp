package com.cibertec.proyectodami

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cibertec.proyectodami.databinding.ActivitySeguimientoBinding

class SeguimientoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeguimientoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*Aca seria la recuperacion del numero telefonico del repartidor asociado a un pedido
         en este caso lo haremos ficticio*/

        val numeroRepartidor: String = "908955357"
        val mensajePredeterminado:String = "¡Hola! Estan cerca con el pedido?"

        binding = ActivitySeguimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCallRepartidor.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this,
                //Comprobamos q el dispositivo q esta utilizando tiene los permisos necesarios para realizar la llamada
                Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED) {

                val miIntent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:${numeroRepartidor}")
                )
                startActivity(miIntent)
            }else {
                //Mostramos un mensaje que no hay permisos para realizar la llamada
                Toast.makeText( this,"No hay permiso para realizar la llamda", Toast.LENGTH_SHORT).show()
                //Pedimos permisos para poder realizar la llamda
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

            //Validamos q tenga permisos para mensajes
            if(ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED
            ){
                //Dado el caso q no pedimos q habilite los permisos
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    1234
                )
            }else{
                enviarMensaje(numeroRepartidor,mensajePredeterminado)
            }
        }



    }
}