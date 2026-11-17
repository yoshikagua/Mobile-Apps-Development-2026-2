package com.example.androidwebservices

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidwebservices.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val adapter = PuntoPosconsumoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initialPaddingTop = binding.root.paddingTop
        val initialPaddingBottom = binding.root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, initialPaddingTop + bars.top, view.paddingRight,
                initialPaddingBottom + bars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_help) {
                mostrarGuia()
                true
            } else {
                false
            }
        }

        binding.resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.resultsRecyclerView.adapter = adapter
        binding.searchButton.setOnClickListener { consultar() }
        binding.clearButton.setOnClickListener { limpiar() }
        binding.retryButton.setOnClickListener { consultar() }
        binding.welcomeGuideButton.setOnClickListener { mostrarGuia() }
        binding.dismissWelcomeButton.setOnClickListener {
            binding.welcomeCard.visibility = View.GONE
        }
        binding.quickPilasCucuta.setOnClickListener {
            binding.keywordInput.setText("Pilas")
            binding.municipioInput.setText("Cúcuta")
            consultar()
        }
        binding.quickBaterias.setOnClickListener {
            binding.keywordInput.setText("Batería")
            consultar()
        }
        binding.quickAll.setOnClickListener {
            limpiar()
            consultar()
        }
    }

    private fun consultar() {
        val limite = binding.limitInput.text.toString().toIntOrNull()
        if (limite == null || limite !in 1..500) {
            binding.limitInput.error = getString(R.string.limit_error)
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val puntos = WebServiceManager.consultarPuntosPosconsumo(
                    programa = binding.programInput.text.toString(),
                    municipio = binding.municipioInput.text.toString(),
                    busqueda = binding.keywordInput.text.toString(),
                    limite = limite
                )
                adapter.submitList(puntos)
                binding.resultsCount.text = getString(R.string.results_count, puntos.size)
                binding.resultsCount.visibility = View.VISIBLE
                binding.emptyText.visibility = if (puntos.isEmpty()) View.VISIBLE else View.GONE
                binding.errorText.visibility = View.GONE
                binding.retryButton.visibility = View.GONE
            } catch (exception: Exception) {
                adapter.submitList(emptyList())
                binding.resultsCount.visibility = View.GONE
                binding.emptyText.visibility = View.GONE
                binding.errorText.text = getString(
                    R.string.status_error_detail,
                    exception.message ?: getString(R.string.unknown_error)
                )
                binding.errorText.visibility = View.VISIBLE
                binding.retryButton.visibility = View.VISIBLE
            } finally {
                setLoading(false)
            }
        }
    }

    private fun limpiar() {
        binding.programInput.text?.clear()
        binding.municipioInput.text?.clear()
        binding.keywordInput.text?.clear()
        binding.limitInput.setText(getString(R.string.default_limit))
        adapter.submitList(emptyList())
        binding.resultsCount.visibility = View.GONE
        binding.emptyText.visibility = View.GONE
        binding.errorText.visibility = View.GONE
        binding.retryButton.visibility = View.GONE
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.loadingText.visibility = if (loading) View.VISIBLE else View.GONE
        binding.searchButton.isEnabled = !loading
        binding.clearButton.isEnabled = !loading
        binding.retryButton.isEnabled = !loading
    }

    private fun mostrarGuia() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.help_title)
            .setMessage(R.string.help_message)
            .setPositiveButton(R.string.help_close, null)
            .show()
    }
}
