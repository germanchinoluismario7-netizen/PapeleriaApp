package com.example.papeleriaapp.presentation.products

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.papeleriaapp.core.model.Product
import com.example.papeleriaapp.core.util.FormatUtils
import com.example.papeleriaapp.presentation.component.EmptyGlyph
import com.example.papeleriaapp.presentation.component.EmptyStateIllustration
import com.example.papeleriaapp.presentation.component.StockLevelBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProductsScreen(
    onBack : () -> Unit,
    onAddProduct : () -> Unit,
    onEditProduct : (String) -> Unit
){
    val viewModel : SellerProductsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when(effect){
                is ProductsEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Al volver de la pantalla de formulario se refresca la lista
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(ProductsEvent.OnRefresh)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Mis productos")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddProduct) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar producto"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.storeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                TextButton(onClick = {
                    viewModel.onEvent(ProductsEvent.OnToggleInactive)
                }) {
                    Text(
                        text = if (state.showInactive) "Ocultar eliminados" else "Mostrar eliminados"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Cargando productos...")
                    }
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.visibleProducts.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyStateIllustration(glyph = EmptyGlyph.BOX)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Aún no tienes productos")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toca + para agregar el primero de tu papelería.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.visibleProducts,
                            key = { product -> product.id }
                        ) { product ->
                            ProductRow(
                                product = product,
                                onEdit = { onEditProduct(product.id) },
                                onDelete = { productToDelete = product },
                                onRestore = {
                                    viewModel.onEvent(ProductsEvent.OnRestore(product))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Text(text = "Eliminar producto")
            },
            text = {
                Text(text = "¿Quitar \"${product.name}\" del catálogo? Podrás restaurarlo después.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(ProductsEvent.OnDelete(product))
                        productToDelete = null
                    }
                ) {
                    Text(text = "Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProductRow(
    product : Product,
    onEdit : () -> Unit,
    onDelete : () -> Unit,
    onRestore : () -> Unit
){
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(all = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    if (product.isActive) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar producto"
                            )
                        }

                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar producto",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        IconButton(onClick = onRestore) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restaurar producto",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${FormatUtils.money(product.price)}  •  Stock: ${product.stock}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (product.isActive) "Activo" else "Eliminado",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (product.isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            //Nivel de stock dibujado con Canvas
            StockLevelBar(stock = product.stock)
        }
    }
}
