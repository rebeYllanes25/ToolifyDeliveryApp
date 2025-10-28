package com.cibertec.proyectodami.presentation.features.cliente.rastreo

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.widget.TextView
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
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.data.api.PedidosCliente
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.domain.model.dtos.ProductoPedidoDTO
import com.cibertec.proyectodami.presentation.common.adapters.ProductoPedidoAdapter
import com.cibertec.proyectodami.presentation.features.cliente.calificacion.CalificacionClientActivity
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



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
    private var idRepartidor:Int = 0;

    private lateinit var productosAdapter : ProductoPedidoAdapter

    private var yaCalificado: Boolean = false;
    private var verificacionCompletada: Boolean = false
    private lateinit var pedidosClienteApi: PedidosCliente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRastreoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        IniciarApi()
        obtenerDatosIntent()

        val btnBack: ImageView = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }


        llamarTelefonoRepartidor()
        enviarMensajeRepartidorBtn()

        cargarDatosVista()
        cargarRecycleView()
        generarcionDeQr()

        recargarDatosPedido()

        binding.btnReloadPage.setOnClickListener {
            verificacionCompletada = false;
            Toast.makeText(this, "Actualizando la vista", Toast.LENGTH_SHORT).show()
            recargarDatosPedido()
        }

    }

    private fun  IniciarApi(){
        val userPreferences = UserPreferences(this)
        val retrofit = RetrofitInstance.create(userPreferences)
        pedidosClienteApi = retrofit.create(PedidosCliente::class.java)
    }

    private fun obtenerDatosIntent(){

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
        idRepartidor = intent.getIntExtra("ID_REPARTIDOR",0)

        productos = if (!productosJson.isNullOrEmpty()) {
            Gson().fromJson(productosJson, Array<ProductoPedidoDTO>::class.java).toList()
        } else {
            emptyList()
        }
    }
    
    private fun actualizarEstadoApi(){
        val estadoIndex = when(estado){
            "PE" -> 0
            "AS" -> 1
            "EC" -> 2
            "CR" -> 3
            "EN" -> 4
            else -> 0
        }
        Log.d("ESTADO_UI", "🎨 Actualizando UI al estado: $estado (índice: $estadoIndex)")
        actualizarEstado(estadoIndex)
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

        for (i in 0 until iconos.size) {
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

        if(nuevoEstado == 4){
            Log.d("ESTADO_UI", "✅ Pedido entregado, verificando para abrir calificación")
            Handler(Looper.getMainLooper()).postDelayed({
                abrirCalificacion()
            }, 500)
        }
    }

    private fun abrirCalificacion(){
        if (!verificacionCompletada ) {
            Log.d("CALIFICACION", " Esperando verificación...")
            Handler(Looper.getMainLooper()).postDelayed({
                abrirCalificacion()
            }, 500)
            return
        }

        if (estado != "EN"){
            Toast.makeText(this,"Error el pedido debe ser entragado para calificar",Toast.LENGTH_SHORT).show()
            Log.d("CALIFICACION", "Estado no válido. Actual: $estado, Requerido: EN")
            return
        }

        if (yaCalificado){
            Toast.makeText(
                this,
                " Este pedido ya fue calificado anteriormente",
                Toast.LENGTH_LONG
            ).show()
            Log.d("CALIFICACION", " Pedido #$pedidoId ya tiene calificación")
            return
        }
        mostrarDialogConfirmacion()
    }

    private fun mostrarDialogConfirmacion(){
        val dialogView = layoutInflater.inflate(R.layout.mensaje_calificacion_confirmacion,null)

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        val btnCalificarAhora = dialogView.findViewById<Button>(R.id.btnCalificarAhora)
        val btnCalificarDespues = dialogView.findViewById<Button>(R.id.btnCalificarDespues)

        btnCalificarAhora.setOnClickListener {
            dialog.dismiss()

            mostrarVistaCalificacion()
        }

        btnCalificarDespues.setOnClickListener{
            dialog.dismiss()

            Toast.makeText(this, "Puedes calificar luego desde tu historial", Toast.LENGTH_SHORT).show()
        }

        dialog.show()

    }

    private fun mostrarVistaCalificacion(){
        Log.d("CALIFICACION", "Abriendo calificación para pedido #$pedidoId")

        Log.d("CALIFICACION", "Abriendo calificacion con numero pedido #${pedidoId}")
        val intent = Intent(this, CalificacionClientActivity::class.java).apply {
            putExtra("ID_PEDIDO",pedidoIdInt)
            putExtra("NUM_PEDIDO",pedidoId)
            putExtra("TOTAL",total)
            putExtra("NOMBRE_REPARTIDOR",nombreRepartidor)
        }
        startActivity(intent)
    }

    private fun recargarDatosPedido(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("RELOAD", "🔄 Recargando datos del pedido #$pedidoIdInt")
                val datosPedido = pedidosClienteApi.obtenerPedidoPorId(pedidoIdInt)
                val responseCalificacion = pedidosClienteApi.verificarCalificacion(pedidoIdInt)

                withContext(Dispatchers.Main){

                    idRepartidor = datosPedido.idRepartidor
                    nombreRepartidor = datosPedido.nomRepartidor
                    apePaternoRepartidor = datosPedido.apePaternoRepartidor
                    telefonoRepartidor = datosPedido.telefonoRepartidor

                    estado = datosPedido.estado
                    Log.d("RELOAD", "✅ Estado actualizado: $estado")

                    if (responseCalificacion.isSuccessful){
                        yaCalificado = responseCalificacion.body()?.get("yaCalificado") ?: false
                        Log.d("RELOAD", "✅ Ya calificado: $yaCalificado")
                    }

                    verificacionCompletada = true
                    actualizarEstadoApi()
                    cargarDatosVista()
                }

            } catch (e:Exception)
            {
                withContext(Dispatchers.Main) {
                    Log.e("RELOAD", "❌ Error recargando: ${e.message}")
                    Toast.makeText(this@RastreoActivity, "Error al recargar datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cargarDatosVista(){

        val subTotal = productos?.sumOf {it.subTotal  } ?: 0
        val costoEnvio = 15.0
        val propina = calcularPropina(total,10.1)

        val totalPagar:Double = (subTotal.toDouble() + costoEnvio) + propina

        if (idRepartidor == 0 || nombreRepartidor.isNullOrEmpty()){
            Log.d("SIN_REPARTIDOR", "MOSTRANDO CARD SIN REPARTIDOR")
            binding.cdSinRepartidor.visibility = View.VISIBLE
            binding.cdRepartidor.visibility = View.GONE

            binding.tvTituloSinRepartidor.startBlinking()

        }else{
            Log.d("CON_REPARTIDOR", "MOSTRANDO CARD CON REPARTIDOR ASIGNADO")
            binding.cdSinRepartidor.visibility = View.GONE
            binding.cdRepartidor.visibility = View.VISIBLE

            binding.tvNameRepartidor.text =  (nombreRepartidor + " " +  apePaternoRepartidor) ?: "Repartidor"
            binding.tvTiempoCalculate.text = "$tiempoEntrega min"
            binding.tvDistanciaRepartidor.text = "A 2.5 km de distancia"
            binding.tvNumberCalificacion.text = "4.8"
            binding.tvNumEntregas.text = "(120 entregas)"
        }



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

    private fun animarCheckmark(imageView: ImageView) {

        val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 0.8f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 0.8f, 1.2f, 1f)

        scaleX.duration = 300
        scaleY.duration = 300

        scaleX.start()
        scaleY.start()
    }

    private fun llamarTelefonoRepartidor(){
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
    }

    private fun enviarMensaje(numero:String, mensaje:String){
        try {
            val sms = SmsManager.getDefault()
            sms.sendTextMessage(numero,null,mensaje,null,null)
            Toast.makeText(this, "Mensaje enviado Correctamente", Toast.LENGTH_SHORT).show()
        }catch (ex:Exception){
            Toast.makeText(this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarMensajeRepartidorBtn(){
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

    private fun generarcionDeQr(){
        binding.btnGenerarQr.setOnClickListener{
            val bitMapQR = generarQR(stringqr.toString())
            val bottomSheet = QrBottomSheet(stringqr.toString(),bitMapQR)
            bottomSheet.show(supportFragmentManager,"QrBottomSheet")
        }
    }

    fun TextView.startBlinking(){
        val animator = ObjectAnimator.ofFloat(this,"alpha",0f,1f)
        animator.duration = 600
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE
        animator.start()

    }
}