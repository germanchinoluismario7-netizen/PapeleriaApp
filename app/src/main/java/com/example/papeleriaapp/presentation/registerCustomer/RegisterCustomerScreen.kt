package com.example.papeleriaapp.presentation.registerCustomer

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.papeleriaapp.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterCustomerScreen(
    viewModel: RegisterCustomerViewModel,
    onBack : () -> Unit,
    onNavigateHome : () -> Unit
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when(effect){
                is CustomerEffect.ShowMessage -> {
                    Toast.makeText(
                        context,
                        effect.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is CustomerEffect.NavigateToHome -> {
                    onNavigateHome()
                }
            }
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text="Registro del Cliente")
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
                    MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) {
            paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all = 16.dp)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(id = R.drawable.ic_customer_register),
                contentDescription = "Registrarme como Cliente",
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier=Modifier.height(20.dp))

            //Nombre del Cliente
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                label = {Text(text="Nombre del Cliente")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono Nombre del Cliente"
                )},
                singleLine = true
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Apellidos del Cliente
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                label = {Text(text="Apellidos del Cliente")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono Apellidos del Cliente"
                )},
                singleLine = true
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Correo Electronico
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                label = {Text(text="Correo Electronico")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Icono Correo Electronico"
                )},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Contraseña
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                label = {Text(text="Contraseña")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Icono Contraseña"
                )},
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Confirmar Contraseña
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                label = {Text(text="Confirmar Contraseña")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Icono Confirmar Contraseña"
                )},
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Registrar datos del Cliente
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ){
                Row{
                    Text(text="Siguiente")
                    Spacer(modifier=Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ir a la siguiente pantalla"
                    )
                }
            }
        }
    }

}
