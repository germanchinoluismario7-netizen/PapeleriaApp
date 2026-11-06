package com.example.papeleriaapp.presentation.registerStore

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.papeleriaapp.R
import com.example.papeleriaapp.core.model.StationeryCategories
import com.example.papeleriaapp.presentation.component.CategoryDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStoreScreen (
    viewModel : RegisterStoreViewModel,
    onBack : () -> Unit,
    onFinishRegisterStore : () -> Unit
){

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when(effect){
                is StoreEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                StoreEffect.NavigateToSellerHome -> {
                    onFinishRegisterStore()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text="Registro de la Papelería")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBack()
                        }
                    ) {
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
        },
    ) {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all = 16.dp)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_store_register),
                contentDescription = "Registrar mi Papelería",
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            //Nombre de la Papeleria
            OutlinedTextField(
                value = state.storeName,
                onValueChange = { value ->
                    viewModel.onEvent(StoreEvent.OnStoreNameChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {Text(text="Nombre de la Papelería")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Icono Nombre de la Papelería"
                )},
                singleLine = true
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Descripcion de la Papeleria
            OutlinedTextField(
                value = state.storeDescription,
                onValueChange = { value ->
                    viewModel.onEvent(StoreEvent.OnStoreDescriptionChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {Text(text="Descripción de la Papelería")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Icono Descripción de la Papelería"
                )},
                minLines = 3,
                maxLines = 4
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Menu desplegable con las categorias de papeleria
            CategoryDropdown(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { category ->
                    viewModel.onEvent(StoreEvent.OnCategoryChange(category))
                },
                categories = StationeryCategories.storeCategories,
                label = "Categoría de la Papelería"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.onEvent(StoreEvent.OnNextClick)
                },
                enabled = !state.isLoading,
                modifier=Modifier.fillMaxWidth()
            ){
                Row{
                    Text(text="Finalizar registro")
                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Icono Siguiente"
                    )
                }
            }
        }
    }
}
