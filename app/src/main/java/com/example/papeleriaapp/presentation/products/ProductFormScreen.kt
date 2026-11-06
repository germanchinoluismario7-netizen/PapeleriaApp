package com.example.papeleriaapp.presentation.products

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.papeleriaapp.core.model.StationeryCategories
import com.example.papeleriaapp.presentation.component.CategoryDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    onBack : () -> Unit,
    onSaved : () -> Unit
){
    val viewModel : ProductFormViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when(effect){
                is ProductFormEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                ProductFormEffect.NavigateBack -> onSaved()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = if (state.isEdit) "Editar producto" else "Agregar producto")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all = 16.dp)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = "Icono Producto",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            //Nombre del producto
            OutlinedTextField(
                value = state.name,
                onValueChange = { value ->
                    viewModel.onEvent(ProductFormEvent.OnNameChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Nombre del producto") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Icono Nombre"
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            //Descripcion
            OutlinedTextField(
                value = state.description,
                onValueChange = { value ->
                    viewModel.onEvent(ProductFormEvent.OnDescriptionChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Descripción") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Icono Descripción"
                    )
                },
                minLines = 3,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            //Categoria
            CategoryDropdown(
                selectedCategory = state.category,
                onCategorySelected = { category ->
                    viewModel.onEvent(ProductFormEvent.OnCategoryChange(category))
                },
                categories = StationeryCategories.productCategories
                    .filter { category -> category != StationeryCategories.productCategories.first() },
                label = "Categoría del producto"
            )

            Spacer(modifier = Modifier.height(12.dp))

            RowPreciosYStock(
                price = state.price,
                stock = state.stock,
                onPriceChange = { value ->
                    viewModel.onEvent(ProductFormEvent.OnPriceChange(value))
                },
                onStockChange = { value ->
                    viewModel.onEvent(ProductFormEvent.OnStockChange(value))
                }
            )

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.onEvent(ProductFormEvent.OnSaveClick)
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Icono Guardar",
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (state.isEdit) "Guardar cambios" else "Agregar producto",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun RowPreciosYStock(
    price : String,
    stock : String,
    onPriceChange : (String) -> Unit,
    onStockChange : (String) -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        //Precio
        OutlinedTextField(
            value = price,
            onValueChange = onPriceChange,
            modifier = Modifier.weight(1f),
            label = { Text(text = "Precio") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Icono Precio"
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        //Stock
        OutlinedTextField(
            value = stock,
            onValueChange = onStockChange,
            modifier = Modifier.weight(1f),
            label = { Text(text = "Stock") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Icono Stock"
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}
