package com.example.projetopratico_mobile1.ui.listform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.projetopratico_mobile1.R
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.data.repo.InMemoryListRepository
import com.example.projetopratico_mobile1.data.repo.RepoProvider
import com.example.projetopratico_mobile1.databinding.ActivityListFormBinding
import com.example.projetopratico_mobile1.ui.home.ListViewModelFactory
import com.example.projetopratico_mobile1.ui.home.HomeViewModel
import com.example.projetopratico_mobile1.util.LocalImageStore
import java.util.UUID

/**
 * Activity para criar ou editar uma lista de compras
 */
class ListFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListFormBinding
    private var listaId: String? = null
    private var selectedImageUri: String? = null

    // ViewModel para gerenciar operações de lista via repositório
    private val viewModel: HomeViewModel by viewModels {
        val repo = RepoProvider.provideListRepository(this)
        if (repo is InMemoryListRepository) {
            println("DEBUG: ListFormActivity - Usando repositório: ${repo.getInstanceId()}")
        }
        ListViewModelFactory(repo)
    }

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

        // TESTE: Verificar se ViewModel foi criado corretamente
        println("DEBUG: ListFormActivity - ViewModel criado: ${viewModel}")
        println("DEBUG: ListFormActivity - Activity inicializada completamente")

        // TESTE: Adicionar botão de teste para criar lista diretamente
        binding.txtTitulo.setOnLongClickListener {
            println("DEBUG: ListFormActivity - TESTE DIRETO iniciado")
            testCreateListDirect()
            true
        }
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
        println("DEBUG: ListFormActivity - configurarEventos() INICIADO")

        binding.btnCancelar.setOnClickListener {
            println("DEBUG: ListFormActivity - Botão CANCELAR clicado")
            finish()
        }

        binding.btnSalvar.setOnClickListener {
            println("DEBUG: ListFormActivity - Botão SALVAR CLICADO")
            salvarLista()
        }

        binding.btnSelecionarImagem.setOnClickListener {
            println("DEBUG: ListFormActivity - Botão SELECIONAR IMAGEM clicado")
            pickImage.launch("image/*") // GetContent() usa String, não Array
        }

        println("DEBUG: ListFormActivity - configurarEventos() FINALIZADO")
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
        println("DEBUG: ListFormActivity - salvarLista() INICIADO")
        val nome = binding.edtNome.text.toString().trim()
        println("DEBUG: ListFormActivity - Nome digitado: '$nome'")

        // valida campos
        if (nome.isEmpty()) {
            println("DEBUG: ListFormActivity - ERRO: Nome vazio")
            binding.edtNome.error = "Informe o nome da lista"
            return
        }

        println("DEBUG: ListFormActivity - Iniciando coroutine para salvar")
        lifecycleScope.launch {
            try {
                if (listaId != null) {
                    // edição
                    println("DEBUG: ListFormActivity - MODO EDIÇÃO: $listaId")
                    val lista = InMemoryStore.buscarLista(listaId!!)
                    lista?.let {
                        val listaAtualizada = it.copy(
                            titulo = nome,
                            imagemUri = selectedImageUri
                        )
                        println("DEBUG: ListFormActivity - Chamando viewModel.updateList()")
                        viewModel.updateList(listaAtualizada)
                        Toast.makeText(this@ListFormActivity, "Lista atualizada!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // criação
                    println("DEBUG: ListFormActivity - MODO CRIAÇÃO")
                    println("DEBUG: ListFormActivity - selectedImageUri: '$selectedImageUri'")

                    // CORREÇÃO: Criar lista e obter o ID para salvar imagem
                    val newList = viewModel.createListAndReturn(nome, null) // Não passar imageUri ainda

                    if (newList != null) {
                        println("DEBUG: ListFormActivity - Lista criada com ID: ${newList.id}")

                        // CORREÇÃO: Salvar imagem no LocalImageStore se foi selecionada
                        if (!selectedImageUri.isNullOrBlank()) {
                            try {
                                println("DEBUG: ListFormActivity - Salvando imagem para lista ${newList.id}")
                                val success = LocalImageStore.saveFromContentUri(this@ListFormActivity, newList.id, selectedImageUri!!)
                                if (success) {
                                    println("DEBUG: ListFormActivity - Imagem salva com sucesso")
                                } else {
                                    println("DEBUG: ListFormActivity - ERRO: Falha ao salvar imagem")
                                }
                            } catch (e: Exception) {
                                println("DEBUG: ListFormActivity - ERRO ao salvar imagem: ${e.message}")
                                e.printStackTrace()
                            }
                        }

                        Toast.makeText(this@ListFormActivity, "Lista criada!", Toast.LENGTH_SHORT).show()
                    } else {
                        println("DEBUG: ListFormActivity - ERRO: Lista não foi criada")
                        Toast.makeText(this@ListFormActivity, "Erro ao criar lista", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }

                // volta para home
                println("DEBUG: ListFormActivity - Finalizando activity com RESULT_OK")
                setResult(Activity.RESULT_OK)
                finish()

            } catch (e: Exception) {
                println("DEBUG: ListFormActivity - ERRO na criação: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@ListFormActivity, "Erro ao salvar lista: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        println("DEBUG: ListFormActivity - salvarLista() FINALIZADO")
    }

    // Método de teste para criar lista diretamente
    private fun testCreateListDirect() {
        println("DEBUG: ListFormActivity - testCreateListDirect() INICIADO")
        lifecycleScope.launch {
            try {
                val testName = "TESTE-${System.currentTimeMillis()}"
                println("DEBUG: ListFormActivity - Criando lista de teste: $testName")
                viewModel.createList(testName, null)
                Toast.makeText(this@ListFormActivity, "Lista de teste criada: $testName", Toast.LENGTH_LONG).show()
                println("DEBUG: ListFormActivity - Lista de teste criada com sucesso")
            } catch (e: Exception) {
                println("DEBUG: ListFormActivity - ERRO na criação de teste: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@ListFormActivity, "ERRO no teste: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
