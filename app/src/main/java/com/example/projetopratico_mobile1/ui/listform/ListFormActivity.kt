package com.example.projetopratico_mobile1.ui.listform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.R
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.databinding.ActivityListFormBinding
import java.util.UUID

/**
 * Activity para criar ou editar uma lista de compras
 */
class ListFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListFormBinding
    private var listaId: String? = null
    private var selectedImageUri: String? = null

    // usando GetContent() com MIME "image/*" conforme solicitado
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // tenta obter persistência de URI (funciona para alguns providers)
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                // alguns providers não suportam persistência, mas ainda podemos usar
            }
            selectedImageUri = it.toString()
            binding.imgLista.setImageURI(it)
            binding.imgLista.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // restaura URI da imagem após rotação
        savedInstanceState?.let {
            selectedImageUri = it.getString(KEY_IMAGE_URI)
        }

        configurarTela()
        configurarEventos()
        carregarDados()

        // aplica preview da imagem salva na rotação
        restaurarPreviewImagem()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // preserva URI da imagem selecionada
        selectedImageUri?.let {
            outState.putString(KEY_IMAGE_URI, it)
        }
    }

    private fun configurarTela() {
        // verifica se é edição
        listaId = intent.getStringExtra("lista_id")
        if (listaId != null) {
            binding.txtTitulo.text = "Editar Lista"
        }

        // Configurar ImageView padrão
        binding.imgLista.setImageResource(R.drawable.ic_cart_blue)
    }

    private fun configurarEventos() {
        binding.btnCancelar.setOnClickListener {
            finish()
        }

        binding.btnSalvar.setOnClickListener {
            salvarLista()
        }

        binding.btnSelecionarImagem.setOnClickListener {
            pickImage.launch("image/*") // GetContent() usa String, não Array
        }
    }

    private fun carregarDados() {
        listaId?.let { id ->
            val lista = InMemoryStore.buscarLista(id)
            lista?.let {
                binding.edtNome.setText(it.titulo)

                // Carregar imagem se existir
                val uri = it.imagemUri
                if (!uri.isNullOrBlank()) {
                    selectedImageUri = uri
                    binding.imgLista.setImageURI(Uri.parse(uri))
                    binding.imgLista.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                } else {
                    binding.imgLista.setImageResource(R.drawable.ic_cart_blue)
                }
            }
        }
    }

    private fun salvarLista() {
        val nome = binding.edtNome.text.toString().trim()

        // valida campos
        if (nome.isEmpty()) {
            binding.edtNome.error = "Informe o nome da lista"
            return
        }

        try {
            if (listaId != null) {
                // edição
                val lista = InMemoryStore.buscarLista(listaId!!)
                lista?.let {
                    val listaAtualizada = it.copy(
                        titulo = nome,
                        imagemUri = selectedImageUri
                    )
                    InMemoryStore.atualizarLista(listaAtualizada)
                    Toast.makeText(this, "Lista atualizada!", Toast.LENGTH_SHORT).show()
                }
            } else {
                // criação
                val novaLista = ShoppingList(
                    id = UUID.randomUUID().toString(),
                    titulo = nome,
                    imagemUri = selectedImageUri
                )
                InMemoryStore.adicionarLista(novaLista)
                Toast.makeText(this, "Lista criada!", Toast.LENGTH_SHORT).show()
            }

            // volta para home
            setResult(Activity.RESULT_OK)
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar lista", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restaurarPreviewImagem() {
        selectedImageUri?.let { uriString ->
            try {
                val uri = Uri.parse(uriString)
                binding.imgLista.setImageURI(uri)
                binding.imgLista.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                // URI inválida, volta ao placeholder
                binding.imgLista.setImageResource(R.drawable.ic_cart_blue)
                selectedImageUri = null
            }
        }
    }

    companion object {
        private const val KEY_IMAGE_URI = "selected_image_uri"
        fun novaLista(activity: Activity): Intent {
            return Intent(activity, ListFormActivity::class.java)
        }

        fun editarLista(activity: Activity, listaId: String): Intent {
            return Intent(activity, ListFormActivity::class.java).apply {
                putExtra("lista_id", listaId)
            }
        }
    }
}
