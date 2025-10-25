package com.cibertec.proyectodami.presentation.features.cliente.rastreo

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ActivityRastreoBinding
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.domain.model.dtos.ProductoPedidoDTO
import com.cibertec.proyectodami.presentation.common.adapters.ProductoPedidoAdapter
import com.cibertec.proyectodami.presentation.features.cliente.calificacion.CalificacionClientActivity
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class RastreoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRastreoBinding

    private var pedidoIdInt: Int = 0;
    private var pedidoId:String? = null;
    private var estado:String? = null;
    private var nombreRepartidor:String? = null
    private var apePaternoRepartidor:String? = null
    private var telefonoRepartidor:String? = null;
    private var tiempoEntrega: Int  = 0;
    private var productos: List<ProductoPedidoDTO>? = null
    private var total: Double = 0.0
    private var direccion: String? = null
    private var stringqr:String? = null

    private lateinit var productosAdapter : ProductoPedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRastreoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        pedidoId = intent.getStringExtra("PEDIDO_ID")
        pedidoIdInt = intent.getIntExtra("PEDIDO_ID_INT",0)
        estado = intent.getStringExtra("ESTADO")
        nombreRepartidor = intent.getStringExtra("REPARTIDOR")
        apePaternoRepartidor = intent.getStringExtra("APE_PATERNO")
        telefonoRepartidor = intent.getStringExtra("TELEFONO_REPARTIDOR")
        tiempoEntrega = intent.getIntExtra("TIEMPO_ENTREGA",0)
        stringqr = intent.getStringExtra("QR_VERIFICATE_PEDIDO")
        val productosJson = intent.getStringExtra("PRODUCTOS")
        total = intent.getDoubleExtra("TOTAL",0.0)

        productos = if (!productosJson.isNullOrEmpty()) {
            Gson().fromJson(productosJson, Array<ProductoPedidoDTO>::class.java).toList()
        } else {
            emptyList()
        }


        Log.d("RastreoActivity", "Pedido ID: $pedidoId")
        Log.d("RastreoActivity", "Estado: $estado")
        Log.d("RastreoActivity", "Repartidor: $nombreRepartidor")
        Log.d("RastreoActivity", "Teléfono: $telefonoRepartidor")
        Log.d("RastreoActivity", "Tiempo: $tiempoEntrega min")
        Log.d("RastreoActivity", "Monto total: $total min")
        Log.d("RastreoActivity", "APELLIDO PATERNO  $apePaternoRepartidor ")
        Log.d("RastreoActivity", "QR CODIGO  $stringqr ")

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
                    Uri.parse("tel:${telefonoRepartidor}")
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

            val mensajePredeterminado = "¡Hola! ¿Están cerca con el pedido?"
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
                telefonoRepartidor?.let { telefono ->
                    enviarMensaje(telefono, mensajePredeterminado)
                }
            }
        }

        cargarDatosVista()
        cargarRecycleView()
        inicializarEstados()
        simularCambiosEstado()


        binding.btnGenerarQr.setOnClickListener{
        val bitMapQR = generarQR(stringqr.toString())
        val bottomSheet = QrBottomSheet(stringqr.toString(),bitMapQR)

        bottomSheet.show(supportFragmentManager,"QrBottomSheet")
        }

    }

    private fun generarQR(texto:String,ancho:Int = 500, alto:Int=500): Bitmap{
        val bitMatrix:BitMatrix = MultiFormatWriter().encode(
            texto,
            BarcodeFormat.QR_CODE,
            ancho,
            alto
        )

        val bitmap = Bitmap.createBitmap(ancho,alto,Bitmap.Config.RGB_565)

        for (x in 0 until ancho){
                for (y in 0 until alto){
                    bitmap.setPixel(x,y, if (bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                }
        }
        return bitmap
    }

    private fun abrirCalificacion(){
        Log.d("CALIFICACION", "Abriendo calificacion con numero pedido #${pedidoId}")

        val intent = Intent(this, CalificacionClientActivity::class.java).apply {
            putExtra("ID_PEDIDO",pedidoIdInt)
            putExtra("NUM_PEDIDO",pedidoId)
            putExtra("TOTAL",total)
            putExtra("NOMBRE_REPARTIDOR",nombreRepartidor)
        }
        startActivity(intent)
    }

    private fun cargarDatosVista(){

        val subTotal = productos?.sumOf {it.subTotal  } ?: 0
        val costoEnvio = 15.0
        val propina = calcularPropina(total,10.1)

        val totalPagar:Double = (subTotal.toDouble() + costoEnvio) + propina
        binding.tvNameRepartidor.text =  (nombreRepartidor + " " +  apePaternoRepartidor) ?: "Repartidor"
        binding.tvTiempoCalculate.text = "$tiempoEntrega min"
        binding.tvDistanciaRepartidor.text = "A 2.5 km de distancia"
        binding.tvNumberCalificacion.text = "4.8"
        binding.tvNumEntregas.text = "(120 entregas)"

        binding.tvSubtotal.text = getString(R.string.seguimiento_fmt_subtotal,subTotal.toDouble())
        binding.tvCosteEnvio.text = getString(R.string.seguimiento_fmt_coste, costoEnvio)
        binding.tvPropina.text = getString(R.string.seguimiento_fmt_propina, propina)
        binding.tvTotalPagar.text = getString(R.string.seguimiento_fmt_total, totalPagar)
    }

    private fun calcularPropina(montoTotal:Double, distanciaKm:Double): Double {

        val porcentaje = 0.05
        val costoPorKm = 0.50

        return (montoTotal * porcentaje) + (distanciaKm *costoPorKm)
    }

    private fun cargarRecycleView(){
        productos?.let { listaProductos ->

            productosAdapter = ProductoPedidoAdapter(listaProductos)

            binding.rvResumen.apply {
                layoutManager = LinearLayoutManager(this@RastreoActivity)
                adapter = productosAdapter
                isNestedScrollingEnabled = true
                setHasFixedSize(true)
            }
        }
    }

    private fun inicializarEstados() {
        actualizarEstado(2)
    }
    private fun simularCambiosEstado() {
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({ actualizarEstado(0) }, 500)

        handler.postDelayed({ actualizarEstado(1) }, 3000)

        handler.postDelayed({ actualizarEstado(2) }, 6000)

        handler.postDelayed({ actualizarEstado(3) }, 9000)

        handler.postDelayed({ actualizarEstado(4) }, 12000)
    }

    private fun actualizarEstado(nuevoEstado: Int) {
        var estadoActual = nuevoEstado

        val iconos = listOf(
            binding.icEstadoAceptado,
            binding.icEstadoPreparando,
            binding.icEstadoTransito,
            binding.icEstadoCerca,
            binding.icEstadoEntregado
        )

        val labels = listOf(
            binding.lblAceptado,
            binding.lblPreparando,
            binding.lblEnTransito,
            binding.lblCerca,
            binding.lblEntregado
        )

        for (i in 0..4) {
            val icono = (iconos[i])
            val label = (labels[i])

            if (i < nuevoEstado) {
                icono.setColorFilter(getColor(R.color.white))
                icono.setBackgroundResource(R.drawable.ic_border_status)
                label.setTextColor(getColor(R.color.color_principal))
                label.setTypeface(null, android.graphics.Typeface.NORMAL)

                animarCheckmark(icono)

            } else if (i == nuevoEstado) {
                icono.setColorFilter(getColor(R.color.white))
                icono.setBackgroundResource(R.drawable.ic_border_status)
                label.setTextColor(getColor(R.color.color_principal))
                label.setTypeface(null, android.graphics.Typeface.NORMAL)

                animarCheckmark(icono)

            } else {
                icono.setColorFilter(getColor(R.color.white))
                icono.setBackgroundResource(R.drawable.ic_border_status_off)
                label.setTextColor(getColor(R.color.color_subtitulos))
                label.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }

        if(nuevoEstado ==4){
            abrirCalificacion()
        }
    }

    private fun animarCheckmark(imageView: ImageView) {

        val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 0.8f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 0.8f, 1.2f, 1f)

        scaleX.duration = 300
        scaleY.duration = 300

        scaleX.start()
        scaleY.start()
    }

    private fun animarEstadoActual(imageView: ImageView) {

        val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.4f, 1f)
        val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.4f, 1f)

        val rotation = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f)

        scaleX.duration = 600
        scaleY.duration = 600
        rotation.duration = 800

        scaleX.start()
        scaleY.start()
        rotation.start()
    }



}