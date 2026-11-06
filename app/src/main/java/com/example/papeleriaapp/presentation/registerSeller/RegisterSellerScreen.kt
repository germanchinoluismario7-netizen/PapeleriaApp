package com.example.papeleriaapp.presentation.registerSeller


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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSellerScreen(
    viewModel : RegisterSellerViewModel,
    onBack : () -> Unit,
    onFinishRegisterSeller : (String) -> Unit
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when(effect){
                is SellerEffect.ShowMessage -> {
                    Toast.makeText(context, effect.value, Toast.LENGTH_SHORT).show()
                }
                is SellerEffect.NavigateToRegisterScreen -> {
                    onFinishRegisterSeller(effect.sellerUid)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text="Registro del Vendedor")
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
                painter = painterResource(id = R.drawable.ic_seller_register),
                contentDescription = "Registrarme como Vendedor",
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier=Modifier.height(20.dp))

            //Nombre del Vendedor
            OutlinedTextField(
                value = state.firstName,
                onValueChange = { value ->
                    viewModel.onEvent(SellerEvent.OnFirstNameChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {Text(text="Nombre")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono Nombre"
                )},
                singleLine = true
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Apellidos del Vendedor
            OutlinedTextField(
                value = state.lastName,
                onValueChange = { value ->
                    viewModel.onEvent(SellerEvent.OnLastNameChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {Text(text="Apellidos")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono Apellidos"
                )},
                singleLine = true
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Correo Electronico
            OutlinedTextField(
                value = state.email,
                onValueChange = { value ->
                    viewModel.onEvent(SellerEvent.OnEmailChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
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
                value = state.password,
                onValueChange = { value ->
                    viewModel.onEvent(SellerEvent.OnPasswordChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
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
                value = state.confirmPassword,
                onValueChange = { value ->
                    viewModel.onEvent(SellerEvent.OnConfirmPasswordChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
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

            //Telefono
            OutlinedTextField(
                value = state.phone,
                onValueChange = { value ->
                    viewModel.onEvent(SellerEvent.OnPhoneChange(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {Text(text="Telefono")},
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Icono Telefono"
                )},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            Spacer(modifier=Modifier.height(12.dp))

            //Registrar datos del Vendedor
            Button(
                onClick = {
                    viewModel.onEvent(SellerEvent.OnNextClick)
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ){
                Row{
                    Text(text="Siguiente")
                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ir a la siguiente pantalla"
                    )
                }
            }
        }
    }
}
